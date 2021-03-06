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

import com.github.fge.jsonpatch.JsonPatch;
import com.monitorjbl.json.JsonResult;
import edu.kit.datamanager.auth.domain.RepoUserGroup;
import edu.kit.datamanager.auth.domain.RepoUserGroup.GroupRole;
import edu.kit.datamanager.exceptions.AccessForbiddenException;
import edu.kit.datamanager.exceptions.UpdateForbiddenException;
import edu.kit.datamanager.auth.service.IGroupService;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.controller.hateoas.event.PaginatedResultsRetrievedEvent;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import edu.kit.datamanager.controller.IGenericResourceController;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.FeatureNotImplementedException;
import edu.kit.datamanager.exceptions.ResourceNotFoundException;
import edu.kit.datamanager.util.AuthenticationHelper;
import edu.kit.datamanager.util.ControllerUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
@Controller
@RequestMapping(value = "/api/v1/groups")
@Schema(description = "Group Management")
public class GroupController implements IGenericResourceController<RepoUserGroup>{

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
  public ResponseEntity<RepoUserGroup> create(
          @RequestBody RepoUserGroup group,
          final WebRequest request,
          final HttpServletResponse response){
    ControllerUtils.checkAnonymousAccess();

    RepoUserGroup newGroup = userGroupService.create(group, (String) AuthenticationHelper.getPrincipal());

    return ResponseEntity.created(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getById(Long.toString(newGroup.getId()), null, request, response)).toUri()).eTag("\"" + newGroup.getEtag() + "\"").body(filterUserGroup(newGroup));
  }

  @Override
  public ResponseEntity<RepoUserGroup> getById(
          @PathVariable("id") final String id,
          @RequestParam(value = "version", required = false) Long version,
          final WebRequest request,
          final HttpServletResponse response){
    ControllerUtils.checkAnonymousAccess();

    RepoUserGroup group = userGroupService.findById(id);
    if(!group.getActive() && !AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())){
      LOGGER.warn("Access to inactive group with id {} requested by principal {} w/o ADMINISTRATOR privileges. Throwing ResourceNotFoundException.", id, AuthenticationHelper.getPrincipal());
      throw new ResourceNotFoundException("Group with id " + id + " was not found or is disabled.");
    }

    RepoUserGroup.GroupRole role = group.getUserRole((String) AuthenticationHelper.getAuthentication().getPrincipal());
    if(GroupRole.NO_MEMBER.equals(role)){
      //no member, check for admin access
      if(!AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())){
        LOGGER.warn("Access to group with id {} requested by principal {} w/o membership or ADMINISTRATOR privileges. Throwing AccessForbiddenException.", id, AuthenticationHelper.getPrincipal());
        throw new AccessForbiddenException("Group access only allowed for group members.");
      }
    }

    return ResponseEntity.ok().eTag("\"" + group.getEtag() + "\"").body(filterUserGroup(group));
  }

  @Override
  public ResponseEntity<List<RepoUserGroup>> findAll(
          @RequestParam(name = "from", required = false) Instant lastUpdateFrom,
          @RequestParam(name = "until", required = false) Instant lastUpdateUntil,
          final Pageable pgbl,
          final WebRequest request,
          final HttpServletResponse response,
          final UriComponentsBuilder uriBuilder){
    return findByExample(null, lastUpdateFrom, lastUpdateUntil, pgbl, request, response, uriBuilder);
  }

  @Override
  public ResponseEntity<List<RepoUserGroup>> findByExample(
          @RequestBody RepoUserGroup example,
          @RequestParam(name = "from", required = false) Instant lastUpdateFrom,
          @RequestParam(name = "until", required = false) Instant lastUpdateUntil,
          final Pageable pgbl,
          final WebRequest req,
          final HttpServletResponse response,
          final UriComponentsBuilder uriBuilder){
    ControllerUtils.checkAnonymousAccess();

    PageRequest request = ControllerUtils.checkPaginationInformation(pgbl);

    Page<RepoUserGroup> page = userGroupService.findAll(example, request, AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue()));

    eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent<>(RepoUserGroup.class, uriBuilder, response, page.getNumber(), page.getTotalPages(), request.getPageSize()));
    //publish listing event??

    return ResponseEntity.ok().body(filterUserGroups(page.getContent()));
  }

  @Override
  public ResponseEntity patch(
          @PathVariable("id") final String id,
          @RequestBody JsonPatch patch,
          final WebRequest request,
          final HttpServletResponse response){
    ControllerUtils.checkAnonymousAccess();

    RepoUserGroup group = userGroupService.findById(id);

    RepoUserGroup.GroupRole role = group.getUserRole((String) AuthenticationHelper.getAuthentication().getPrincipal());
    boolean adminAccess = false;
    boolean managerAccess = GroupRole.GROUP_MANAGER.equals(role);
    if(!managerAccess || !group.getActive()){
      if(!AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())){
        LOGGER.warn("Caller with principal {} has neither ADMINISTRATOR nor GROUP_MANAGER permissions. Patching not allowed. Throwing  AccessForbiddenException.", AuthenticationHelper.getPrincipal());
        throw new AccessForbiddenException("Insufficient role. ROLE_GROUP_MANAGER or ROLE_ADMINISTRATOR required to patch group.");
      } else{
        adminAccess = true;
      }
    }

    ControllerUtils.checkEtag(request, group);

    Collection<GrantedAuthority> userGrants = new ArrayList<>();
    userGrants.add(new SimpleGrantedAuthority(role.getValue()));

    if(adminAccess){
      LOGGER.debug("Admin access detected. Adding ADMINISTRATOR role to granted authorities.");
      userGrants.add(new SimpleGrantedAuthority(RepoUserRole.ADMINISTRATOR.getValue()));
    }

    userGroupService.patch(group, patch, userGrants);

//    //check is caller has revoked its GROUP_MANAGER status
//    role = group.getUserRole((String) AuthenticationHelper.getAuthentication().getPrincipal());
//    if(managerAccess && !GroupRole.GROUP_MANAGER.equals(role)){
//      throw new UpdateForbiddenException("You cannot revoke your GROUP_MANAGER status by youself.");
//    }
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity delete(
          @PathVariable("id") final String id,
          final WebRequest request,
          final HttpServletResponse response
  ){
    ControllerUtils.checkAnonymousAccess();

    try{
      RepoUserGroup group = userGroupService.findById(id);
      RepoUserGroup.GroupRole role = group.getUserRole((String) AuthenticationHelper.getAuthentication().getPrincipal());
      if(!GroupRole.GROUP_MANAGER.equals(role)){
        //check admin access
        if(!AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())){
          throw new UpdateForbiddenException("Insufficient role. ROLE_GROUP_MANAGER or ROLE_ADMINISTRATOR required.");
        }
      }

      ControllerUtils.checkEtag(request, group);

      userGroupService.delete(group);

    } catch(ResourceNotFoundException ex){
      //ignore
      LOGGER.trace("Group with id {} not found in DELETE. Ignoring exception.", id);
    }

    return ResponseEntity.noContent().build();
  }

  private RepoUserGroup filterUserGroup(RepoUserGroup group){
    group.getMemberships().forEach((membership) -> {
      membership.getUser().clean();
    });

    return group;
  }

  private List<RepoUserGroup> filterUserGroups(List<RepoUserGroup> groups){
    groups.forEach((group) -> {
      group.getMemberships().forEach((membership) -> {
        membership.getUser().clean();
      });
    });
    return groups;
  }

  @Override
  public ResponseEntity put(String string, RepoUserGroup c, WebRequest wr, HttpServletResponse hsr){
    throw new FeatureNotImplementedException("PUT is not supported for group resouces.");
  }
}
