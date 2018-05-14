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
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.exceptions.AccessForbiddenException;
import edu.kit.datamanager.exceptions.BadArgumentException;
import edu.kit.datamanager.service.exceptions.CustomInternalServerError;
import edu.kit.datamanager.exceptions.EtagMismatchException;
import edu.kit.datamanager.exceptions.UnauthorizedAccessException;
import edu.kit.datamanager.exceptions.UpdateForbiddenException;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.util.PatchUtil;
import edu.kit.datamanager.controller.hateoas.event.PaginatedResultsRetrievedEvent;
import edu.kit.datamanager.controller.IGenericResourceController;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.util.AuthenticationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
@Controller
@RequestMapping(value = "/api/v1/users")
@Api(value = "User Management")
public class UserController implements IGenericResourceController<RepoUser>{

  private JsonResult json = JsonResult.instance();

  @Autowired
  private Logger LOGGER;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private final IUserService userService;

  public UserController(IUserService userService){
    super();
    this.userService = userService;
  }

  @Override
  public ResponseEntity<RepoUser> create(@RequestBody RepoUser user, WebRequest request, HttpServletResponse response){
    user.setId(null);

    if(user.getUsername() == null){
      throw new BadArgumentException("No username assigned to provided user.");
    } else{
      //enforce lowercase username
      user.setUsername(user.getUsername());
    }
    if(user.getPassword() == null){
      throw new BadArgumentException("No password assigned to provided user.");
    }

    if(userService.count() == 0){
      //first user, add ADMINISTRATOR role
      user.getRolesAsEnum().add(RepoUserRole.ADMINISTRATOR);
    } else{
      if(user.getRolesAsEnum().contains(RepoUserRole.ADMINISTRATOR) && (AuthenticationHelper.isAnonymous() || !AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue()))){
        throw new BadArgumentException("Self-registration with role ADMINISTRATOR not allowed.");
      }
    }

    user.setActive(Boolean.TRUE);
    user.setLocked(Boolean.FALSE);
    RepoUser newUser = userService.create(user);
    String etag = Integer.toString(user.hashCode());
    user.erasePassword();
    return ResponseEntity.created(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this.getClass()).create(newUser, request, response)).toUri()).eTag("\"" + etag + "\"").body(user);
  }

  @ApiOperation(value = "Obtain caller information for the currently authenticated user.",
          notes = "This endpoints can be used to obtain user details for the currently logged in user. If the caller has authenticated as registered user, user details are returned. "
          + "If this endpoint is accessed anonymously, HTTP UNAUTHORIZED (401) is returned.")
  @RequestMapping(value = {"/me"}, method = {RequestMethod.GET})
  @ResponseBody
  public ResponseEntity<RepoUser> me(WebRequest wr, HttpServletResponse hsr){
    if(!AuthenticationHelper.isAnonymous()){
      String principal = (String) AuthenticationHelper.getAuthentication().getPrincipal();
      RepoUser me = (RepoUser) userService.loadUserByUsername(principal);
      if(me == null){
        //this should acutually never happen as the user has been authenticated before mapping to an existing user
        LOGGER.error("Authenticated user for principal name {} not found. Returning HTTP INTERNAL_SERVER_ERROR.");
        throw new CustomInternalServerError("Failed to obtain authenticated repository user '" + principal + "'.");
      }

      me.erasePassword();
      return ResponseEntity.ok(me);
    } else{
      throw new UnauthorizedAccessException("You are not logged in.");
    }
  }

  @Override
  public ResponseEntity<List<RepoUser>> findAll(Pageable pgbl, WebRequest wr, HttpServletResponse response, final UriComponentsBuilder uriBuilder){
    return findByExample(null, pgbl, wr, response, uriBuilder);
  }

  @Override
  public ResponseEntity<RepoUser> getById(@PathVariable(value = "id") Long id, WebRequest request, HttpServletResponse response){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Anonymous user access disabled.");
    }
    Optional<RepoUser> result = userService.findById(id);
    if(!result.isPresent()){
      return ResponseEntity.notFound().build();
    }

    RepoUser user = result.get();

    if(!AuthenticationHelper.isUser(user.getUsername()) && !AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.toString())){
      throw new AccessForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required to read other users.");
    }

    String etag = Integer.toString(user.hashCode());
    user.erasePassword();
    return ResponseEntity.ok().eTag("\"" + etag + "\"").body(user);
  }

  @Override
  public ResponseEntity<List<RepoUser>> findByExample(@RequestBody RepoUser example, final Pageable pgbl, final WebRequest wr, final HttpServletResponse response, final UriComponentsBuilder uriBuilder){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Please login in order to be able to list resources.");
    }

    if(!AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())){
      throw new AccessForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required.");
    }

    int pageSize = pgbl.getPageSize();
    if(pageSize > 100){
      LOGGER.debug("Restricting user-provided page size {} to max. page size 100.", pageSize);
      pageSize = 100;
    }
    LOGGER.debug("Rebuilding page request for page {}, size {} and sort {}.", pgbl.getPageNumber(), pageSize, pgbl.getSort());
    PageRequest request = PageRequest.of(pgbl.getPageNumber(), pageSize, pgbl.getSort());
    Page<RepoUser> page = userService.findAll(example, request);
    if(pgbl.getPageNumber() > page.getTotalPages()){
      LOGGER.debug("Requested page number {} is too large. Number of pages is: {}. Returning empty list.", pgbl.getPageNumber(), page.getTotalPages());
    }

    List<RepoUser> modUsers = json.use(JsonView.with(page.getContent())
            .onClass(RepoUser.class, match().exclude("password")))
            .returnValue();

    eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent<>(RepoUser.class, uriBuilder, response, page.getNumber(), page.getTotalPages(), pageSize));
    //publish listing event??
    return ResponseEntity.ok(modUsers);
  }

  @Override
  public ResponseEntity patch(@PathVariable(value = "id") Long id, @RequestBody JsonPatch patch, WebRequest request, HttpServletResponse hsr
  ){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Please login in order to be able to modify resources.");
    }
    Optional<RepoUser> result = userService.findById(id);
    if(!result.isPresent()){
      return ResponseEntity.notFound().build();
    }
    RepoUser user = result.get();

    if(!AuthenticationHelper.isUser(user.getUsername()) && !AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.toString())){
      throw new AccessForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required to patch other users.");
    }

    if(!request.checkNotModified(Integer.toString(user.hashCode()))){
      throw new EtagMismatchException("ETag not matching, resource has changed.");
    }

    RepoUser updated = PatchUtil.applyPatch(user, patch, RepoUser.class, AuthenticationHelper.getAuthentication().getAuthorities());

    LOGGER.info("Persisting patched user.");
    userService.update(updated);
    LOGGER.info("User successfully persisted.");
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity delete(@PathVariable(value = "id") Long id, WebRequest request, HttpServletResponse hsr
  ){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Please login in order to be able to modify resources.");
    }

    if(!AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())){
      throw new UpdateForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required.");
    }

    Optional<RepoUser> result = userService.findById(id);
    if(result.isPresent()){
      //user was found and caller has ADMIN role
      RepoUser user = result.get();
      if(!request.checkNotModified(Integer.toString(user.hashCode()))){
        throw new EtagMismatchException("ETag not matching, resource has changed.");
      }
      user.setActive(Boolean.FALSE);
      userService.update(user);
    }

    return ResponseEntity.noContent().build();
  }
}
