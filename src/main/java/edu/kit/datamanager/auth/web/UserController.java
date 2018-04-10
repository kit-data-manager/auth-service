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
import edu.kit.datamanager.auth.domain.Note;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.exceptions.AccessForbiddenException;
import edu.kit.datamanager.auth.exceptions.BadArgumentException;
import edu.kit.datamanager.auth.exceptions.CustomInternalServerError;
import edu.kit.datamanager.auth.exceptions.UnauthorizedAccessException;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.auth.web.hateoas.event.PaginatedResultsRetrievedEvent;
import edu.kit.datamanager.controller.GenericResourceController;
import edu.kit.datamanager.util.AuthenticationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
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
public class UserController extends GenericResourceController<RepoUser>{

  @Autowired
  private Logger LOGGER;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private IUserService userDetailsService;

  public UserController(IUserService userDetailsService){
    super();
    this.userDetailsService = userDetailsService;
  }

  @ApiOperation(value = "Obtain caller information for the currently authenticated user.",
          notes = "This endpoints can be used to obtain user details for the currently logged in user. If the caller has authenticated as registered user, user details are returned. "
          + "If this endpoint is accessed anonymously, HTTP UNAUTHORIZED (401) is returned.")
  @RequestMapping(value = {"/me"}, method = {RequestMethod.GET})
  @ResponseBody
  public ResponseEntity<RepoUser> me(WebRequest wr, HttpServletResponse hsr){
    if(!AuthenticationHelper.isAnonymous()){
      String principal = (String) AuthenticationHelper.getAuthentication().getPrincipal();
      RepoUser me = (RepoUser) userDetailsService.loadUserByUsername(principal);
      if(me == null){
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
  public ResponseEntity<RepoUser> create(@RequestBody RepoUser user, WebRequest request, HttpServletResponse response){
    user.setId(null);
    if(user.getIdentifier() == null){
      user.setIdentifier(UUID.randomUUID().toString());
    }
    if(user.getPassword() == null){
      throw new BadArgumentException("No password assigned to provided user.");
    }

    user.setActive(true);

    RepoUser newUser = userDetailsService.create(user);
    return ResponseEntity.created(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this.getClass()).create(newUser, request, response)).toUri()).build();
  }

  @Override
  public ResponseEntity<List<RepoUser>> findAll(Pageable pgbl, WebRequest wr, HttpServletResponse response, final UriComponentsBuilder uriBuilder){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Anonymous user listing disabled.");
    }

    if(!AuthenticationHelper.hasAuthority(RepoUser.UserRole.ADMINISTRATOR.toString())){
      throw new AccessForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required.");
    }

    int pageSize = pgbl.getPageSize();
    if(pageSize > 100){
      LOGGER.debug("Restricting user-provided page size {} to max. page size 100.", pageSize);
      pageSize = 100;
    }

    LOGGER.debug("Rebuilding page request for page {}, size {} and sort {}.", pgbl.getPageNumber(), pageSize, pgbl.getSort());
    PageRequest request = PageRequest.of(pgbl.getPageNumber(), pageSize, pgbl.getSort());
    Page<RepoUser> page = userDetailsService.findAll(null, request);
    if(pgbl.getPageNumber() > page.getTotalPages()){
      LOGGER.debug("Requested page number {} is too large. Number of pages is: {}. Returning empty list.", pgbl.getPageNumber(), page.getTotalPages());
    }
    eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent<>(Note.class, uriBuilder, response, page.getNumber(), page.getTotalPages(), pageSize));
    //publish listing event??
    return ResponseEntity.ok(page.getContent());
  }

  @Override
  public ResponseEntity<RepoUser> findById(@PathVariable(value = "id") Long id, WebRequest request, HttpServletResponse response){
    if(AuthenticationHelper.isAnonymous()){
      throw new UnauthorizedAccessException("Anonymous user access disabled.");
    }
    Optional<RepoUser> result = userDetailsService.findById(id);
    if(!result.isPresent()){
      return ResponseEntity.notFound().build();
    }

    if(!AuthenticationHelper.hasAuthority(RepoUser.UserRole.ADMINISTRATOR.toString())){
      throw new AccessForbiddenException("Insufficient role. ROLE_ADMINISTRATOR required.");
    }

    RepoUser user = result.get();
    user.erasePassword();
    return ResponseEntity.ok(user);
  }

  @Override
  public ResponseEntity<Resources<RepoUser>> findByExample(final RepoUser c, final Pageable pgbl, final WebRequest wr, final HttpServletResponse hsr, final UriComponentsBuilder uriBuilder){

    return null;
  }

  @Override
  public ResponseEntity patch(Long l, JsonPatch jp, WebRequest wr, HttpServletResponse hsr){
    return null;
  }

  @Override
  public ResponseEntity delete(Long l, WebRequest wr, HttpServletResponse hsr){
    return null;
  }

//  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
//
////  final UserService userService;
////
////  @Autowired
////  public UserController(UserService userService){
////    this.userService = userService;
////  }
//  @ApiOperation(value = "Return user information about user authenticated via JSON Web Token.", notes = "The provided session resource may contain a group identifier for creating a session for a group different from "
//          + "the default group that is obtained from the principal. All other attributes of the provided session resource are automatically assigned.  "
//          + "Furthermore, authentication credentials for the according user must be provided depending on the chosen authenticator, e.g. in the header or as query parameter. "
//          + "The returned session contains the session identifier as well as the expiration timestamp. Furthermore, a cookie is delivered containing the session identifier. "
//          + "This endpoint is not intended to be used for extending or updating an existing session. The only exception is, when the session has expired. In that case, the session is "
//          + "removed from the database and a new session is created. If a valid session already exists for the caller, this endpoint returns HTTP SEE_OTHER and refers to the session "
//          + "resource in the 'Location' header.")
//  @ApiResponses(value = {
//    @ApiResponse(code = 201, message = "Successfully created session.", response = Session.class)
//    ,
//         @ApiResponse(code = 303, message = "Session already exists, see Location header.")
//    ,
//        @ApiResponse(code = 404, message = "User or group not found.")
//    ,
//        @ApiResponse(code = 422, message = "Unprocessable entity, e.g. if userId or groupId do not represent a valid user or group.")})
//  @RequestMapping(path = "/api/v1/me", method = RequestMethod.GET)
//  public ResponseEntity me(){
//    //JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//    User user = new User();//userService.findByName(token.getName());
//    user.erasePassword();
//    user.setActiveGroup("USERS");
//    return ResponseEntity.ok(user);
//  }
//
////  @RequestMapping(path = "users", method = RequestMethod.GET, produces = "application/hal+json")
////  public Resources getUsers(Pageable pgbl){
////    JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
////    Page<User> user = userService.findAll(pgbl);//loadUser(token.getName(), (String) token.getCredentials()).orElseThrow(() -> new InvalidAuthenticationException("Access denied"));
////    //user.erasePassword();
////    // user.setActiveGroup(token.getGroupId());
////    return new Resources(user.getContent());
////  }
////  @POST
////  @Path(value = "/")
////  @ApiOperation(value = "Create a new user.", notes = "The provided user resource must contain at least the user name in the format 'familyName, givenName'. "
////          + "If the resource also contains a user identifier, it also has to provide an identifier scheme which might be used for identifier validation. If no "
////          + "identifier is provided, the system will assign a (locally) unique identifier. "
////          + "In order to be able to create new users, the caller must have write permissions. Thus, a valid session identifier must be provided in the header.")
////  @ApiResponses(value = {
////    @ApiResponse(code = 201, message = "Successfully created user.", response = User.class)
////    ,
////        @ApiResponse(code = 303, message = "See other including location link pointing to an existing user.")
////    ,
////        @ApiResponse(code = 409, message = "A user with the provided name or user identifier already exist.")})
////  @RoleRequired(Role.ADMINISTRATOR)
////  public ResponseEntity createUser(
////          @ApiParam(value = "The JSON representation of the user information.", required = true) User user
////  ){
////    JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
////        String sid = getContext().getHeaderString("Session-Id");
////    User result;
////
////    //check and optionally assign identifier
////    if(user.getIdentifier() == null){
////      //no identifier set, assign custom...as UUIDs should be unique, no additional check is done at this point
////      user.setIdentifier(UUID.randomUUID().toString());
////    } else{
////      //check if user with same id already exists
////      User existingUser;
////      try{
////        existingUser = getResourceService().read(user.getIdentifier());
////      } catch(ServiceException ex){
////        LOGGER.error("Failed to check for existing user for identifier " + user.getIdentifier(), ex);
////        return ex.toResponse();
////      }
////
////      if(existingUser != null){
////        //redirect to existing user
////        URI destination;
////        if(sid != null){
////          destination = ServiceUtil.getResourceUri(getContext(), user.getIdentifier(), sid);
////        } else{
////          //typically, we arrive here only if the sessionId is not provided as header field but as Cookie or query parameter
////          destination = ServiceUtil.getResourceUri(getContext(), user.getIdentifier());
////        }
////
////        LOGGER.trace("User with identifier {} already exist. Redirecting caller to resource {}.", user.getIdentifier(), destination);
////        return Response.status(Response.Status.SEE_OTHER).location(destination).build();
////      }
////    }
////
////    try{
////      ValidationHelper.validate(user);
////    } catch(InvalidResourceException ex){
////      LOGGER.error("Failed to validate provided user " + user, ex);
////      return ex.toResponse();
////    }
////
////    //save user
////    try{
////      result = storeResource(user);
////    } catch(ServiceException ex){
////      LOGGER.error("Failed to save user " + user, ex);
////      return ex.toResponse();
////    }
////    //assign some permissions, memberships???
////
////    //create cache control
////    CacheControl cc = new CacheControl();
////    cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
////    cc.setPrivate(true);
////    //return user with cache control, tag end entity
////    return Response.created(ServiceUtil.getResourceUri(getContext(), user.getIdentifier())).
////            cacheControl(cc).
////            tag(Integer.toString(result.hashCode())).
////            entity(result).
////            build();
////    return ResponseEntity.ok().build();
////  }
////  @GET
////  @Path(value = "/{userId}")
////  @ApiOperation(value = "Get a user by its unique identifier.",
////          notes = "In order to be able to access user information, a valid session identifier must be provided in the header.")
////  @ApiResponses(value = {
////    @ApiResponse(code = 200, message = "Success", response = User.class)
////  })
////  @RoleRequired(Role.GUEST)
////  public ResponseEntity getUser(
////          @ApiParam(value = "The user identifier.", required = true) @PathParam("userId") String userId
////  ){
////    if(!getCallerIdentifier().equals(userId) && !callerIsAdministrator()){
////      return Response.status(Response.Status.FORBIDDEN).entity("Insufficient permissions for obtaining information about user " + userId + ".").type(MediaType.TEXT_PLAIN).build();
////    }
////
////    User result;
////    try{
////      result = getResourceService().read(userId);
////    } catch(ServiceException ex){
////      LOGGER.error("Failed to obtain user for identifier " + userId, ex);
////      return ex.toResponse();
////    }
////
////    if(result == null){
////      return Response.status(Response.Status.NOT_FOUND).entity("No user found for provided identifier.").build();
////    }
////
////    CacheControl cc = new CacheControl();
////    cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
////    cc.setPrivate(true);
////    return Response.
////            ok(result).
////            tag(Integer.toString(result.hashCode())).
////            cacheControl(cc).build();
////  return ResponseEntity.ok ().build();
////  }
////  @GET
////        @Path(value = "/")
////        @ApiOperation(value = "Get a list of users based on the provided query.",
////                notes = "In order to be able to retrieve user information, a valid session identifier must be provided in the header.")
////        @ApiResponses(value = {
////  @ApiResponse(code = 200, message = "Success", response = User[].class)
////})
////        @RoleRequired(Role.USER)
////        public ResponseEntity getUsers(
////          @ApiParam(value = "The query refining the result set.", required = false)
////        @QueryParam("query") String query,
////          @ApiParam(value = "The JSON object containing the query variables, e.g. {\"v1\":\"test\",\"v2\":2,\"v3\":[\"test\",\"test1\"]}", required = false)
////        @QueryParam("variables") String variables,
////          @ApiParam(value = "The page to show starting with page 1.", required = false)
////        @QueryParam("page")
////        @DefaultValue("1") Integer page,
////          @ApiParam(value = "The max. number of results per page.", required = false)
////        @QueryParam("resultsPerPage")
////        @DefaultValue("10") Integer resultsPerPage,
////          @ApiParam(value = "The field by which the result will be sorted.", required = false)
////        @QueryParam("sortField") String sortField,
////          @ApiParam(value = "The sort order, which is either ASC (default) or DESC.", required = false)
////        @QueryParam("sortOrder")
////        @DefaultValue("ASC") IBaseDao.SORT_ORDER sortOrder
////  ){
////    String sessionId = ServiceUtil.getSessionIdFromResource(this);
////    Map<String, Object> variableMap = null;
////    if(variables != null){
////      try{
////        variableMap = new ObjectMapper().readValue(variables, HashMap.class);
////      } catch(IOException ex){
////        LOGGER.error("Invalid format of variables argument with value '" + variables + "'.", ex);
////        return Response.status(Response.Status.BAD_REQUEST).entity("Bad variables map format.").build();
////      }
////    }
////    QueryResult<User> results;
////    try{
////      results = getResourceService().read(query, variableMap, page, resultsPerPage, sortField, sortOrder);
////    } catch(ServiceException ex){
////      LOGGER.error("Failed to perform query for users.", ex);
////      return ex.toResponse();
////    }
////    if(results == null || results.isEmpty()){
////      return Response.status(Response.Status.NOT_FOUND).entity("No users found for provided query.").build();
////    }
////
////    CacheControl cc = new CacheControl();
////    cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
////    cc.setPrivate(true);
////    return Response.ok(results.getResults().toArray(new User[]{})).
////            cacheControl(cc).
////            links(
////                    Link.fromUri(ServiceUtil.getNextPageLink(getContext(), 1, resultsPerPage, sortField, sortOrder, sessionId)).param("rel", "next").build(),
////                    Link.fromUri(ServiceUtil.getLastPageLink(getContext(), results.getOverallResults(), resultsPerPage, sortField, sortOrder, sessionId)).param("rel", "last").build()
////            )
////            .build();
////    return ResponseEntity.ok().build();
////  }
////
////  @PUT
////        @Path(value = "/{userId}")
////        @ApiOperation(value = "Update a user.", notes = "The provided user resource must contain at least the user name in the format 'familyName, givenName'. "
////                + "The userId is not affected by the update and any userId provided in the user resource will be overwritten by the path parameter."
////                + "In order to be able to create new users, the caller must be the user himself or must have the ADMINISTRATOR role. "
////                + "Thus, a valid session identifier must be provided in the header.")
////        @ApiResponses(value = {
////  @ApiResponse(code = 200, message = "Successfully updated user.", response = User.class)
////  ,
////        @ApiResponse(code = 404, message = "A user for the provided identifier was not found.")})
////        @RoleRequired(Role.USER)
////        public ResponseEntity updateUser(
////          @ApiParam(value = "The user identifier.", required = true)
////        @PathParam("userId") String userId,
////          @ApiParam(value = "The JSON representation of the user information.", required = true) User user
////  ){
////    if(!getCallerIdentifier().equals(userId) && !callerIsAdministrator()){
////      LOGGER.error("Insufficient privileges of caller {} to update user {}.", getCallerIdentifier(), userId);
////      return Response.status(Response.Status.FORBIDDEN).entity("Caller is not allowed to update user.").type(MediaType.TEXT_PLAIN).build();
////    }
////
////    User result;
////
////    //check and optionally assign identifier
////    //check if user with same id already exists
////    User existingUser;
////    try{
////      existingUser = getResourceService().read(userId);
////    } catch(ServiceException ex){
////      LOGGER.error("Failed to obtain existing user for identifier " + user.getIdentifier(), ex);
////      return ex.toResponse();
////    }
////
////    if(existingUser == null){
////      LOGGER.trace("User with identifier {} does not exist. Aborting update with HTTP 404.", userId);
////      return Response.status(Response.Status.NOT_FOUND).entity("User not found.").type(MediaType.TEXT_PLAIN).build();
////    }
////
////    //setting identifier in order to update the correct user
////    user.setIdentifier(userId);
////
////    try{
////      ValidationHelper.validate(user);
////    } catch(InvalidResourceException ex){
////      LOGGER.error("Failed to validate updated user " + user, ex);
////      return ex.toResponse();
////    }
////
////    //save user
////    try{
////      result = storeResource(user);
////    } catch(ServiceException ex){
////      LOGGER.error("Failed to store updated user " + user, ex);
////      return ex.toResponse();
////    }
////
////    //create cache control
////    CacheControl cc = new CacheControl();
////    cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
////    cc.setPrivate(true);
////    //return user with cache control, tag end entity
////    return Response.ok(result).
////            cacheControl(cc).
////            tag(Integer.toString(result.hashCode())).
////            entity(result).
////            build();
////    return ResponseEntity.ok().build();
////  }
////
////  @PATCH
////        @Path(value = "/{userId}")
////        @ApiOperation(value = "Update the user with the provided user identifier.",
////                notes = "This endpoint can be used e.g. to change the user name or affiliation. It is not possible to change the user identifier or the identifier scheme. "
////                + "In order to be able to modify user information, the caller must have write permissions or the user with the provided userId. Thus, a valid session identifier must "
////                + "be provided in the header.")
////        @ApiResponses(value = {
////  @ApiResponse(code = 204, message = "No Content.")
////  ,
////        @ApiResponse(code = 404, message = "Resource not found, e.g. invalid user identifier.")
////  ,
////        @ApiResponse(code = 409, message = "Conflict, e.g. resource has changed.")
////  ,
////        @ApiResponse(code = 415, message = "Unsupported Media Type, e.g. unsupported patch document format.")
////
////})
////        @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
////        @RoleRequired(Role.GUEST)
////        public ResponseEntity patchUser(
////          @ApiParam(value = "The user identifier.", required = true)
////        @PathParam("userId") String userId,
////          @ApiParam(value = "A JSON Patch document according to IETF RFC 6901.", example = "[\n"
////                + "  { \"op\": \"replace\", \"path\": \"/givenName\", \"value\": \"John\" }\n"
////                + "]", required = true) String jsonPatchDocument,
////          @ApiParam(value = "The ETag of the resource.", hidden = true)
////        @HeaderParam(HttpHeaders.IF_NONE_MATCH) String etag
////  ){
////    //obtain user entity
////    User user;
////    try{
////      user = getResourceService().read(userId);
////    } catch(ServiceException ex){
////      LOGGER.error("Failed to obtain user with id " + userId + " for update.", ex);
////      return ex.toResponse();
////    }
////
////    if(!callerIsAdministrator() && !userId.equals(getCallerIdentifier())){
////      LOGGER.error("Insufficient permissions of caller {} to patch user with identifier {}.", getCallerIdentifier(), userId);
////      return Response.status(Response.Status.FORBIDDEN).entity("Insufficient permissions to patch user.").type(MediaType.TEXT_PLAIN).build();
////    }
////
////    //check etag to avoid conflicts
////    ServiceUtil.checkETag(getContext().getRequest(), user);
////    //collect fields that can be changed only if the caller has write permissions...in that case, these fields are ignore, otherwise they are part of the validation hash
////    //there are currently no locked fields as only administrators are allowed to access this operation
////    String forbiddenFields = null;
//////                (!callerIsAdministrator())
//////                ? user.getRoles().hashCode()
//////                + ((user.getExpiresAt() != null) ? new SimpleDateFormat().format(user.getExpiresAt()) : null) : null;
////
////    //hash 'locked' fields, e.g. identifier, identifier scheme and scheme URI
////    String hashBefore = ResourceUtils.hashElements(user.getIdentifier(),
////            (user.getIdentifierScheme() != null) ? user.getIdentifierScheme().getSchemeId() : null,
////            (user.getIdentifierScheme() != null) ? user.getIdentifierScheme().getSchemeUri() : null,
////            forbiddenFields);
////    try{
////      //apply patch to resource
////      user = ServiceUtil.applyPatch(user, jsonPatchDocument, User.class);
////    } catch(IOException ex){
////      LOGGER.error("Failed to apply patch\n" + jsonPatchDocument + " to resource\n" + user, ex);
////      return Response.status(Response.Status.BAD_REQUEST).entity("Failed to apply JSON patch.").type(MediaType.TEXT_PLAIN).build();
////    }
////
////    //there are currently no locked fields as only administrators are allowed to access this operation
////    forbiddenFields = null;
////
////    String hashAfter = ResourceUtils.hashElements(
////            user.getIdentifier(),
////            (user.getIdentifierScheme() != null) ? user.getIdentifierScheme().getSchemeId() : null,
////            (user.getIdentifierScheme() != null) ? user.getIdentifierScheme().getSchemeUri() : null,
////            forbiddenFields);
////    //compare hashes
////    if(!hashBefore.equals(hashAfter)){
////      //hashes not equal, invalid modification
////      LOGGER.error("Failed to apply patch\n" + jsonPatchDocument + " to resource\n" + user);
////      //return HTTP UNPROCESSABLE_ENTITY
////      return Response.status(422).entity("Failed to apply JSON patch. Locked fields were affected.").type(MediaType.TEXT_PLAIN).build();
////    }
////    //Update successful, store the updated user and return.
////    try{
////      user = getResourceService().update(user);
////    } catch(ServiceException ex){
////      LOGGER.error("Failed to save user " + user, ex);
////      return ex.toResponse();
////    }
////    return Response.
////            status(Response.Status.NO_CONTENT).
////            tag(Integer.toString(user.hashCode())).
////            build();
////    return ResponseEntity.ok().build();
////  }
////  @DELETE
////        @Path(value = "/{userId}")
////        @ApiOperation(value = "Delete a user by its unique identifier.",
////                notes = "In order to be able to delete user information, the caller must have write permissions. Thus, a valid session identifier must be provided in the header. "
////                + "Deleting users might be forbidden or only allowed for expired users.")
////        @ApiResponses(value = {
////  @ApiResponse(code = 204, message = "No content.")
////})
////        @RoleRequired(Role.ADMINISTRATOR)
////        public ResponseEntity deleteUser(
////          @ApiParam(value = "The user identifier.", required = true)
////        @PathParam("userId") String userId
////  ){
////
//////    try{
//////      getResourceService().delete(userId);
//////    } catch(ServiceException ex){
//////      LOGGER.warn("Failed to delete user " + userId, ex);
//////    }
//////
//////    return Response.status(Response.Status.NO_CONTENT).build();
////    return ResponseEntity.ok().build();
////  }
}
