/*
 * Copyright 2016 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.auth.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.monitorjbl.json.JsonResult;
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;
import edu.kit.datamanager.auth.domain.Note;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.domain.RepoUserGroup;
import edu.kit.datamanager.auth.domain.RepoUserGroup.GroupRole;
import edu.kit.datamanager.auth.exceptions.AccessForbiddenException;
import edu.kit.datamanager.auth.exceptions.CustomInternalServerError;
import edu.kit.datamanager.auth.exceptions.EtagMismatchException;
import edu.kit.datamanager.auth.exceptions.PatchApplicationException;
import edu.kit.datamanager.auth.exceptions.UnauthorizedAccessException;
import edu.kit.datamanager.auth.exceptions.UpdateForbiddenException;
import edu.kit.datamanager.auth.service.IGroupService;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.auth.util.PatchUtil;
import edu.kit.datamanager.auth.web.hateoas.event.PaginatedResultsRetrievedEvent;
import io.swagger.annotations.Api;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import edu.kit.datamanager.controller.GenericResourceController;
import edu.kit.datamanager.util.AuthenticationHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
@Controller
@RequestMapping(value = "/api/v1/groups")
@Api(value = "Group Management")
public class GroupController extends GenericResourceController<RepoUserGroup>{

  private JsonResult json = JsonResult.instance();

  @Autowired
  private Logger LOGGER;
  @Autowired
  private final IGroupService userGroupService;
  @Autowired
  private final IUserService userService;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  public GroupController(IGroupService userGroupService, IUserService userService){
    super();
    this.userGroupService = userGroupService;
    this.userService = userService;
  }

  @Override
  public ResponseEntity<RepoUserGroup> create(@RequestBody RepoUserGroup group, WebRequest request, final HttpServletResponse response){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Anonymous group creation disabled.");
    }
    group.setId(null);
    if(group.getIdentifier() == null){
      group.setIdentifier(UUID.randomUUID().toString());
    }

    //assign caller membership
    String caller = (String) AuthenticationHelper.getAuthentication().getPrincipal();
    RepoUser theUser = (RepoUser) userService.loadUserByUsername(caller);
    if(theUser == null){
      //this should acutually never happen as the user has been authenticated before mapping to an existing user
      LOGGER.error("Authenticated user for principal name {} not found. Returning HTTP INTERNAL_SERVER_ERROR.");
      throw new CustomInternalServerError("Failed to obtain authenticated repository user '" + caller + "'.");
    }
    group.addOrUpdateMembership(theUser, GroupRole.GROUP_MANAGER);

    RepoUserGroup newGroup = userGroupService.create(group);

    return ResponseEntity.created(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this.getClass()).getById(newGroup.getId(), request, response)).toUri()).build();
  }

  @Override
  public ResponseEntity<RepoUserGroup> getById(@PathVariable("id") final Long id, WebRequest request, final HttpServletResponse response){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Anonymous group access disabled.");
    }
    Optional<RepoUserGroup> result = userGroupService.findById(id);
    if(!result.isPresent()){
      return ResponseEntity.notFound().build();
    }

    RepoUserGroup group = result.get();

    if(!group.getActive() && !AuthenticationHelper.hasAuthority(RepoUser.UserRole.ADMINISTRATOR.toString())){
      return ResponseEntity.notFound().build();
    }

    RepoUserGroup.GroupRole role = group.getUserRole((String) AuthenticationHelper.getAuthentication().getPrincipal());
    if(GroupRole.NO_MEMBER.equals(role)){
      //no member, check for admin access
      if(!AuthenticationHelper.hasAuthority(RepoUser.UserRole.ADMINISTRATOR.toString())){
        throw new AccessForbiddenException("Group access only allowed for group members.");
      }
    }

    RepoUserGroup modGroup = json.use(JsonView.with(group)
            .onClass(RepoUser.class, match().exclude("*").include("username").include("id")))
            .returnValue();

    String etag = Integer.toString(group.hashCode());
    return ResponseEntity.ok().eTag("\"" + etag + "\"").body(modGroup);
  }

  @Override
  public ResponseEntity<List<RepoUserGroup>> findAll(Pageable pgbl, WebRequest request, final HttpServletResponse response, final UriComponentsBuilder uriBuilder){
    return findByExample(null, pgbl, request, response, uriBuilder);
  }

  @Override
  public ResponseEntity<List<RepoUserGroup>> findByExample(@RequestBody RepoUserGroup example, Pageable pgbl, WebRequest req, final HttpServletResponse response, final UriComponentsBuilder uriBuilder){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Please login in order to be able to list resources.");
    }

    int pageSize = pgbl.getPageSize();
    if(pageSize > 100){
      LOGGER.debug("Restricting user-provided page size {} to max. page size 100.", pageSize);
      pageSize = 100;
    }
    LOGGER.debug("Rebuilding page request for page {}, size {} and sort {}.", pgbl.getPageNumber(), pageSize, pgbl.getSort());
    PageRequest request = PageRequest.of(pgbl.getPageNumber(), pageSize, pgbl.getSort());
    Page<RepoUserGroup> page;
    if(AuthenticationHelper.hasAuthority(RepoUser.UserRole.ADMINISTRATOR.toString())){
      //do find all
      page = userGroupService.findAll(example, request);
    } else{
      //query based on membership
      page = userGroupService.findByMembershipsUserUsernameEqualsAndMembershipsRoleGreaterThanEqual((String) AuthenticationHelper.getAuthentication().getPrincipal(), GroupRole.GROUP_MEMBER, request);
    }

    if(pgbl.getPageNumber() > page.getTotalPages()){
      LOGGER.debug("Requested page number {} is too large. Number of pages is: {}. Returning empty list.", pgbl.getPageNumber(), page.getTotalPages());
    }
    eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent<>(Note.class, uriBuilder, response, page.getNumber(), page.getTotalPages(), pageSize));
    //publish listing event??

    List<RepoUserGroup> modGroups = json.use(JsonView.with(page.getContent())
            .onClass(RepoUser.class, match().exclude("*").include("username").include("id")))
            .returnValue();

    return ResponseEntity.ok(modGroups);
  }

  @Override
  public ResponseEntity patch(@PathVariable("id") final Long id, @RequestBody JsonPatch patch, WebRequest request, final HttpServletResponse response){
    System.out.println("PATCH " + patch);
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Please login in order to be able to modify resources.");
    }
    Optional<RepoUserGroup> result = userGroupService.findById(id);
    if(!result.isPresent()){
      return ResponseEntity.notFound().build();
    }
    RepoUserGroup group = result.get();
    RepoUserGroup.GroupRole role = group.getUserRole((String) AuthenticationHelper.getAuthentication().getPrincipal());
    boolean adminAccess = false;
    if(!GroupRole.GROUP_MANAGER.equals(role)){
      if(!AuthenticationHelper.hasAuthority(RepoUser.UserRole.ADMINISTRATOR.toString())){
        throw new AccessForbiddenException("Insufficient role. ROLE_GROUP_MANAGER or ROLE_ADMINISTRATOR required to patch group.");
      } else{
        adminAccess = true;
      }
    }
    if(!request.checkNotModified(Integer.toString(group.hashCode()))){
      throw new EtagMismatchException("ETag not matching, resource has changed.");
    }
    ObjectMapper tmpObjectMapper = new ObjectMapper();
    JsonNode resourceAsNode = tmpObjectMapper.convertValue(group, JsonNode.class);
    RepoUserGroup updated;
    try{
      // Apply the patch
      JsonNode patchedDataResourceAsNode = patch.apply(resourceAsNode);
      //convert resource back to POJO
      updated = tmpObjectMapper.treeToValue(patchedDataResourceAsNode, RepoUserGroup.class);
    } catch(JsonPatchException | JsonProcessingException ex){
      LOGGER.error("Failed to apply patch '" + patch.toString() + " to group resource " + group, ex);
      throw new PatchApplicationException("Failed to apply patch to resource.");
    }

    Collection<GrantedAuthority> userGrants = new ArrayList<>();
    userGrants.add(new SimpleGrantedAuthority(role.toString()));

    if(adminAccess){
      LOGGER.debug("Admin access detected. Adding ADMINISTRATOR role to granted authorities.");
      userGrants.add(new SimpleGrantedAuthority(RepoUser.UserRole.ADMINISTRATOR.toString()));
    }
    if(!PatchUtil.canUpdate(group, updated, userGrants)){
      throw new UpdateForbiddenException("Patch not applicable.");
    }

    LOGGER.info("Persisting patched group.");
    userGroupService.update(updated);
    LOGGER.info("Group successfully persisted.");
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity delete(@PathVariable("id") final Long id, WebRequest request, final HttpServletResponse response){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Please login in order to be able to modify resources.");
    }

    Optional<RepoUserGroup> result = userGroupService.findById(id);
    if(result.isPresent()){
      //user was found and caller has ADMIN role
      RepoUserGroup group = result.get();

      RepoUserGroup.GroupRole role = group.getUserRole((String) AuthenticationHelper.getAuthentication().getPrincipal());
      if(!GroupRole.GROUP_MANAGER.equals(role)){
        //check admin access
        if(!AuthenticationHelper.hasAuthority(RepoUser.UserRole.ADMINISTRATOR.toString())){
          throw new UpdateForbiddenException("Insufficient role. ROLE_GROUP_MANAGER or ROLE_ADMINISTRATOR required.");
        }
      }

      if(!request.checkNotModified(Integer.toString(group.hashCode()))){
        throw new EtagMismatchException("ETag not matching, resource has changed.");
      }

      group.setActive(Boolean.FALSE);
      userGroupService.update(group);
    }

    return ResponseEntity.noContent().build();
  }
}
