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
import edu.kit.datamanager.exceptions.CustomInternalServerError;
import edu.kit.datamanager.exceptions.UpdateForbiddenException;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.controller.hateoas.event.PaginatedResultsRetrievedEvent;
import edu.kit.datamanager.controller.IGenericResourceController;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.ResourceNotFoundException;
import edu.kit.datamanager.util.AuthenticationHelper;
import edu.kit.datamanager.util.ControllerUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
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

  private final JsonResult json = JsonResult.instance();

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
    RepoUser newUser = userService.create(user, AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue()));

    filterAndAutoReturnRepoUser(newUser);

    return ResponseEntity.created(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this.getClass()).create(newUser, request, response)).toUri()).eTag("\"" + user.getEtag() + "\"").build();
  }

  @ApiOperation(value = "Obtain caller information for the currently authenticated user.",
          notes = "This endpoints can be used to obtain user details for the currently logged in user. If the caller has authenticated as registered user, user details are returned. "
          + "If this endpoint is accessed anonymously, HTTP UNAUTHORIZED (401) is returned.")
  @RequestMapping(value = {"/me"}, method = {RequestMethod.GET})
  @ResponseBody
  public ResponseEntity<RepoUser> me(WebRequest wr, HttpServletResponse hsr){
    ControllerUtils.checkAnonymousAccess();

    String principal = (String) AuthenticationHelper.getAuthentication().getPrincipal();
    RepoUser me = (RepoUser) userService.loadUserByUsername(principal);
    if(me == null){
      //this should acutually never happen as the user has been authenticated before mapping to an existing user
      LOGGER.error("Authenticated user for principal name {} not found. Throwing CustomInternalServerError.");
      throw new CustomInternalServerError("Failed to obtain authenticated repository user '" + principal + "'.");
    }

    filterAndAutoReturnRepoUser(me);

    return ResponseEntity.ok().eTag("\"" + me.getEtag() + "\"").build();
  }

  @Override
  public ResponseEntity<RepoUser> getById(@PathVariable(value = "id") String id, WebRequest request, HttpServletResponse response){
    ControllerUtils.checkAnonymousAccess();

    RepoUser user = userService.findById(ControllerUtils.parseIdToLong(id));

    if(!AuthenticationHelper.isPrincipal(user.getUsername()) && !AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.toString())){
      LOGGER.warn("Caller {} is not allowed to request users by id. Only the user himself or users with ROLE_ADMINISTRATOR are allowed to read user details. Throwing AccessForbiddenException.", user.getUsername());
      throw new AccessForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required to read other users.");
    }

    filterAndAutoReturnRepoUser(user);

    return ResponseEntity.ok().eTag("\"" + user.getEtag() + "\"").build();
  }

  @Override
  public ResponseEntity<List<RepoUser>> findAll(Pageable pgbl, WebRequest wr, HttpServletResponse response, final UriComponentsBuilder uriBuilder){
    return findByExample(null, pgbl, wr, response, uriBuilder);
  }

  @Override
  public ResponseEntity<List<RepoUser>> findByExample(@RequestBody RepoUser example, final Pageable pgbl, final WebRequest wr, final HttpServletResponse response, final UriComponentsBuilder uriBuilder){
    ControllerUtils.checkAnonymousAccess();

    if(!AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())){
      LOGGER.warn("Caller is not allowed to find users by example. ROLE_ADMINISTRATOR is required. Throwing AccessForbiddenException.");
      throw new AccessForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required.");
    }

    PageRequest request = ControllerUtils.checkPaginationInformation(pgbl);

    Page<RepoUser> page = userService.findAll(example, request);

    filterAndAutoReturnRepoUsers(page.getContent());

    eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent<>(RepoUser.class, uriBuilder, response, page.getNumber(), page.getTotalPages(), request.getPageSize()));

    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity patch(@PathVariable(value = "id") String id, @RequestBody JsonPatch patch, WebRequest request, HttpServletResponse hsr){
    ControllerUtils.checkAnonymousAccess();

    RepoUser user = userService.findById(ControllerUtils.parseIdToLong(id));

    if(!AuthenticationHelper.isPrincipal(user.getUsername()) && !AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())){
      throw new AccessForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required to patch other users.");
    }

    ControllerUtils.checkEtag(request, user);

    userService.patch(user, patch, AuthenticationHelper.getAuthentication().getAuthorities());

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity delete(@PathVariable(value = "id") String id, WebRequest request, HttpServletResponse hsr){
    ControllerUtils.checkAnonymousAccess();

    if(!AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())){
      throw new UpdateForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required.");
    }

    try{
      RepoUser user = userService.findById(ControllerUtils.parseIdToLong(id));

      ControllerUtils.checkEtag(request, user);

      userService.delete(user);
    } catch(ResourceNotFoundException ex){
      //ignore
      LOGGER.trace("User with id {} not found in DELETE. Ignoring exception.", id);
    }

    return ResponseEntity.noContent().build();
  }

  private void filterAndAutoReturnRepoUser(RepoUser resource){
    //transform and return JSON representation as next controller result
    json.use(JsonView.with(resource)
            .onClass(RepoUser.class, match().exclude("password")));
  }

  private void filterAndAutoReturnRepoUsers(List<RepoUser> resources){
    //transform and return JSON representation as next controller result
    json.use(JsonView.with(resources)
            .onClass(RepoUser.class, match().exclude("password")));
  }

}
