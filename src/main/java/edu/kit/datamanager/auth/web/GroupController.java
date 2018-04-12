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
import edu.kit.datamanager.auth.domain.RepoUserGroup;
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
import java.util.List;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
@Controller
@RequestMapping(value = "/api/v1/groups")
@Api(value = "Group Management")
public class GroupController extends GenericResourceController<RepoUserGroup>{

  @Autowired
  private Logger LOGGER;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Override
  public ResponseEntity<RepoUserGroup> create(@RequestBody RepoUserGroup group, WebRequest request, final HttpServletResponse response){
    return null;
  }

  @Override
  public ResponseEntity<List<RepoUserGroup>> findAll(Pageable pgbl, WebRequest request, final HttpServletResponse response, final UriComponentsBuilder uriBuilder){
    System.out.println("HERE");
    return null;
  }

  @Override
  public ResponseEntity<RepoUserGroup> getById(@PathVariable("id") final Long id, WebRequest request, final HttpServletResponse response){
    System.out.println("BYID");
    return null;
  }

  @Override
  public ResponseEntity<List<RepoUserGroup>> findByExample(@RequestBody RepoUserGroup example, Pageable pgbl, WebRequest request, final HttpServletResponse response, final UriComponentsBuilder uriBuilder){
    System.out.println("EXAMPLE");
    return null;
  }

  @Override
  public ResponseEntity patch(@PathVariable("id") final Long id, @RequestBody JsonPatch patch, WebRequest request, final HttpServletResponse response){
    return null;
  }

  @Override
  public ResponseEntity delete(@PathVariable("id") final Long id, WebRequest request, final HttpServletResponse response){
    return null;
  }

//    @POST
//    @Path(value = "/")
//    @ApiOperation(value = "Create a new user group.",
//            notes = "The provided user group resource should at least contain a groupId. It may also contain a description and an identifier or initial members. "
//            + "If no identifier is provided, the system will assign a (locally) unique identifier. "
//            + "In order to be able to create a group, a valid session identifier must be provided in the header. The owner of the session is assumed to be the group manager "
//            + "and will be set as first group member.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 201, message = "Successfully created group.", response = Group.class)
//        ,
//        @ApiResponse(code = 303, message = "See other including location link pointing to an existing group.")
//
//    })
//    @RoleRequired(Role.USER)
//    public Response createGroup(
//            @ApiParam(value = "The JSON representation of the user group.", required = true) Group group) {
//        String sid = getContext().getHeaderString("Session-Id");
//        Group result;
//
//        //check and optionally assign identifier
//        if (group.getIdentifier() == null) {
//            //no identifier set, assign custom...as UUIDs should be unique, no additional check is done at this point
//            group.setIdentifier(UUID.randomUUID().toString());
//        } else {
//            //check for duplicate group id if the user has provided an identifier
//            Group existingGroup;
//            try {
//                existingGroup = getResourceService().read(group.getIdentifier());
//            } catch (ServiceException ex) {
//                LOGGER.error("Failed to check for existing group with identifier " + group.getIdentifier(), ex);
//                return ex.toResponse();
//            }
//            if (existingGroup != null) {
//                //redirect to existing group
//                URI destination;
//                if (sid != null) {
//                    destination = ServiceUtil.getResourceUri(getContext(), existingGroup.getIdentifier(), sid);
//                } else {
//                    //typically, we arrive here only if the sessionId is not provided as header field but as Cookie or query parameter
//                    destination = ServiceUtil.getResourceUri(getContext(), existingGroup.getIdentifier());
//                }
//
//                LOGGER.trace("Group with identifier {} already exist. Redirecting caller to resource {}.", existingGroup.getIdentifier(), destination);
//                return Response.status(Response.Status.SEE_OTHER).location(destination).build();
//            }
//        }
//
//        try {
//            group.validate();
//        } catch (InvalidResourceException ex) {
//            LOGGER.error("Failed to validate provided group " + group + ".", ex);
//            return ex.toResponse();
//        }
//
//        UserPrincipal principal = (UserPrincipal) getContext().getSecurityContext().getUserPrincipal();
//        String founderUserId = principal.getPrincipalUser().getIdentifier();
//
//        if (!group.getMemberIdentifiers().contains(founderUserId)) {
//            LOGGER.trace("New group does not contain founder with id '{}'. Adding userId to group members.", founderUserId);
//            group.getMemberIdentifiers().add(founderUserId);
//        } else {
//            LOGGER.trace("Founder with id '{}' is already in group.", founderUserId);
//        }
//
//        //save group
//        try {
//            result = storeResource(group);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to save group " + group, ex);
//            return ex.toResponse();
//        }
//        //create cache control
//        CacheControl cc = new CacheControl();
//        cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
//        cc.setPrivate(true);
//        //return group with cache control, tag end entity
//        return Response.created(ServiceUtil.getResourceUri(getContext(), result.getIdentifier())).
//                cacheControl(cc).
//                tag(Integer.toString(result.hashCode())).
//                entity(result).
//                build();
//    }
//
//    @GET
//    @Path(value = "/")
//    @ApiOperation(value = "Get a list of groups based on the provided query.",
//            notes = "In order to be able to retrieve group information, a valid session identifier must be provided in the header.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Success", response = Group[].class)
//    })
//    @RoleRequired(Role.GUEST)
//    public Response getGroups(
//            @ApiParam(value = "The query refining the result set.", required = false) @QueryParam("query") String query,
//            @ApiParam(value = "The JSON object containing the query variables, e.g. {\"v1\":\"test\",\"v2\":2,\"v3\":[\"test\",\"test1\"]}", required = false) @QueryParam("variables") String variables,
//            @ApiParam(value = "The page to show starting with page 1.", required = false) @QueryParam("page") @DefaultValue("1") Integer page,
//            @ApiParam(value = "The max. number of results per page.", required = false) @QueryParam("resultsPerPage") @DefaultValue("10") Integer resultsPerPage,
//            @ApiParam(value = "The field by which the result will be sorted.", required = false) @QueryParam("sortField") String sortField,
//            @ApiParam(value = "The sort order, which is either ASC (default) or DESC.", required = false) @QueryParam("sortOrder") @DefaultValue("ASC") IBaseDao.SORT_ORDER sortOrder
//    ) {
//        String sessionId = ServiceUtil.getSessionIdFromResource(this);
//
//        Map<String, Object> variableMap = null;
//        if (variables != null) {
//            try {
//                variableMap = new ObjectMapper().readValue(variables, HashMap.class);
//            } catch (IOException ex) {
//                LOGGER.error("Invalid format of variables argument with value '" + variables + "'.", ex);
//                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid format of 'variables' argument.").build();
//            }
//        }
//
//        QueryResult<Group> results;
//        try {
//            results = getResourceService().read(query, variableMap, page, resultsPerPage, sortField, sortOrder);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to perform query " + query, ex);
//            return ex.toResponse();
//        }
//
//        if (results == null || results.isEmpty()) {
//            return Response.status(Response.Status.NOT_FOUND).entity("Query returned no result.").build();
//        }
//
//        CacheControl cc = new CacheControl();
//        cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
//        cc.setPrivate(true);
//        return Response.ok(results.getResults().toArray(new Group[]{})).
//                cacheControl(cc).
//                links(
//                        Link.fromUri(ServiceUtil.getNextPageLink(getContext(), 1, resultsPerPage, sortField, sortOrder, sessionId)).param("rel", "next").build(),
//                        Link.fromUri(ServiceUtil.getLastPageLink(getContext(), results.getOverallResults(), resultsPerPage, sortField, sortOrder, sessionId)).param("rel", "last").build()
//                ) // header("Link", "<http://localhost:8080/api/v1/dataResources?page=2&resultsPerPage=10&sort=sortField&order=order>; rel=\"next\", "
//                //         + " <http://localhost:8080/api/v1/dataResources?page=10&resultsPerPage=10&sort=sortField&order=order>; rel=\"last\"")
//                .build();
//    }
//
//    @GET
//    @Path(value = "/{groupId}")
//    @ApiOperation(value = "Get a user group by its unique identifier.",
//            notes = "In order to be able to retrieve group information, a valid session identifier must be provided in the header.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Success", response = Group.class)
//    })
//    @RoleRequired(Role.GUEST)
//    public Response getGroup(
//            @ApiParam(value = "The group identifier.", required = true) @PathParam("groupId") String groupId
//    ) {
//        Group result;
//        try {
//            result = getResourceService().read(groupId);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to obtain group for id " + groupId, ex);
//            return ex.toResponse();
//        }
//        if (result == null) {
//            return Response.status(Response.Status.NOT_FOUND).entity("Group for id " + groupId + " not found.").build();
//        }
//        CacheControl cc = new CacheControl();
//        cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
//        cc.setPrivate(true);
//        return Response.ok(result).tag(Integer.toString(result.hashCode())).cacheControl(cc).build();
//    }
//
//    @GET
//    @Path(value = "/{groupId}/members")
//    @ApiOperation(value = "Get all members of the group with the provided group identifier.",
//            notes = "In order to be able to retrieve group information, a valid session identifier must be provided in the header. Furthermore, the caller must be group member.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Success", response = User[].class)
//    })
//    @RoleRequired(Role.GUEST)
//    public Response getGroupMembers(
//            @ApiParam(value = "The group identifier.", required = true) @PathParam("groupId") String groupId) {
//
//        Group result;
//        try {
//            result = getResourceService().read(groupId);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to obtain group for id " + groupId, ex);
//            return ex.toResponse();
//        }
//
//        if (result == null) {
//            return Response.status(Response.Status.NOT_FOUND).entity("Group for id " + groupId + " not found.").build();
//        }
//
//        String callerId = ((UserPrincipal) getContext().getSecurityContext().getUserPrincipal()).getPrincipalUser().getIdentifier();
//
//        if (!result.getMemberIdentifiers().contains(callerId) && !callerIsAdministrator()) {
//            LOGGER.error("Caller " + getCallerIdentifier() + " has no permission for listing group memberships. Returning HTTP 403.");
//            return Response.status(Response.Status.FORBIDDEN).entity("Insufficient permissions to list group members.").build();
//        }
//
////
////        String query = "FOR b IN groups \n"
////                + " FILTER b.`identifier` == @groupId\n"
////                + "   LET a = (FOR x IN b.`memberIdentifiers`\n"
////                + "      FOR a IN users FILTER x == a.`identifier` RETURN a)\n"
////                + " RETURN a";
////
////        List<ArrayList> members;
//        QueryResult<User> members;
//        try {
////            QueryResult<Group> memberList = getResourceService().query(query, ArangoUtils.VariableMapBuilder.create().addVariable("groupId", result.getIdentifier()).build(), 0, 100, null, IBaseDao.SORT_ORDER.ASC);
////                    //performInternalQuery(query, ArangoUtils.VariableMapBuilder.create().addVariable("groupId", result.getIdentifier()).build(), ArrayList.class);
//            Map<String, Object> arguments = new HashMap<>();
//            arguments.put("memberIds", result.getMemberIdentifiers());
//            members = ServiceUtil.getService(IUserServiceAdapter.class).read("FILTER e.userId IN @memberIds", arguments, 1, result.getMemberIdentifiers().size(), null, null);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed obtain members list of group " + groupId, ex);
//            return ex.toResponse();
//        }
//        return Response.ok(members.getResults().toArray(new User[]{})).build();
//    }
//
//    @PUT
//    @Path(value = "/{groupId}")
//    @ApiOperation(value = "Update a user group.",
//            notes = "The provided user group resource should at least contain a groupId. It may also contain a description and an identifier or initial members. "
//            + "If no identifier is provided, the system will assign a (locally) unique identifier. "
//            + "In order to be able to create a group, a valid session identifier must be provided in the header. The owner of the session is assumed to be the group manager "
//            + "and will be set as first group member.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 201, message = "Successfully created group.", response = Group.class)
//        ,
//        @ApiResponse(code = 404, message = "A group for the provided identifier was not found.")
//    })
//    @RoleRequired(Role.USER)
//    public Response updateGroup(
//            @ApiParam(value = "The group identifier.", required = true) @PathParam("groupId") String groupId,
//            @ApiParam(value = "The JSON representation of the user group.", required = true) Group group) {
//        Group result;
//
//        //check for group id
//        Group existingGroup;
//        try {
//            existingGroup = getResourceService().read(groupId);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to check for existing group with identifier " + group.getIdentifier(), ex);
//            return ex.toResponse();
//        }
//        if (existingGroup == null) {
//            LOGGER.trace("Existing group with identifier {} not found. Returning HTTP 404.", groupId);
//            return Response.status(Response.Status.NOT_FOUND).entity("Group not found.").type(MediaType.TEXT_PLAIN).build();
//        }
//
//        UserPrincipal principal = (UserPrincipal) getContext().getSecurityContext().getUserPrincipal();
//        String callerUserId = principal.getPrincipalUser().getIdentifier();
//
//        if (!existingGroup.getMemberIdentifiers().contains(callerUserId) && !callerIsAdministrator()) {
//            LOGGER.error("Insufficient privileges of caller {} to update group {}.", getCallerIdentifier(), groupId);
//            return Response.status(Response.Status.FORBIDDEN).entity("Caller is not allowed to update group.").type(MediaType.TEXT_PLAIN).build();
//        }
//
//        //setting group id
//        group.setIdentifier(groupId);
//
//        try {
//            group.validate();
//        } catch (InvalidResourceException ex) {
//            LOGGER.error("Failed to validate updated group " + group + ".", ex);
//            return ex.toResponse();
//        }
//
//        if (!group.getMemberIdentifiers().contains(callerUserId)) {
//            LOGGER.trace("Updated group does not contain caller with id '{}'. Adding userId to group members.", callerUserId);
//            group.getMemberIdentifiers().add(callerUserId);
//        } else {
//            LOGGER.trace("Caller with id '{}' is already in group.", callerUserId);
//        }
//
//        //save group
//        try {
//            result = storeResource(group);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to store updated group " + group, ex);
//            return ex.toResponse();
//        }
//        //create cache control
//        CacheControl cc = new CacheControl();
//        cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
//        cc.setPrivate(true);
//        //return group with cache control, tag end entity
//        return Response.ok(result).
//                cacheControl(cc).
//                tag(Integer.toString(result.hashCode())).
//                entity(result).
//                build();
//    }
//
//    @PATCH
//    @Path(value = "/{groupId}")
//    @ApiOperation(value = "Update the group with the provided group identifier.",
//            notes = "This endpoint can be used e.g. to change the group name or description. It is not possible to change the group identifier. "
//            + "In order to be able to modify group information, the caller must have write permissions. Thus, a valid session identifier must be provided in the header."
//    )
//    @ApiResponses(value = {
//        @ApiResponse(code = 204, message = "No Content.")
//        ,
//        @ApiResponse(code = 404, message = "Resource not found, e.g. invalid group identifier.")
//        ,
//        @ApiResponse(code = 409, message = "Conflict, e.g. resource has changed.")
//        ,
//        @ApiResponse(code = 415, message = "Unsupported Media Type, e.g. unsupported patch document format.")
//
//    })
//    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
//    @RoleRequired(Role.USER)
//    public Response patchGroup(
//            @ApiParam(value = "The group identifier.", required = true) @PathParam("groupId") String groupId,
//            @ApiParam(value = "A JSON Patch document according to IETF RFC 6901.", example = "[\n"
//                    + "  { \"op\": \"replace\", \"path\": \"/identifier/value\", \"value\": \"NewValue\" }\n"
//                    + "]", required = true) String jsonPatchDocument,
//            @ApiParam(value = "The ETag of the resource.", hidden = true) @HeaderParam(HttpHeaders.IF_NONE_MATCH) String etag
//    ) {
//        //obtain group entity
//        Group group;
//        try {
//            group = getResourceService().read(groupId);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to obtain group for id " + groupId, ex);
//            return ex.toResponse();
//        }
//        //check etag to avoid conflicts
//        ServiceUtil.checkETag(getContext().getRequest(), group);
//        //collect fields that can be changed only if the caller has write permissions...in that case, these fields are ignore, otherwise they are part of the validation hash
//        //there are currently no locked fields as only administrators are allowed to access this operation
//        String forbiddenFields = null;
////                (!callerIsAdministrator())
////                ? Boolean.toString(group.isDisabled())
////                + ((group.getExpiresAt() != null) ? new SimpleDateFormat().format(group.getExpiresAt()) : null) : null;
//
//        //hash 'locked' fields, e.g. identifier, identifier scheme and scheme URI
//        String hashBefore = ResourceUtils.hashElements(group.getIdentifier(),
//                (group.getIdentifierScheme() != null) ? group.getIdentifierScheme().getSchemeId() : null,
//                (group.getIdentifierScheme() != null) ? group.getIdentifierScheme().getSchemeUri() : null, forbiddenFields);
//        try {
//            //apply patch to resource
//            group = ServiceUtil.applyPatch(group, jsonPatchDocument, Group.class);
//        } catch (IOException ex) {
//            LOGGER.error("Failed to apply patch\n" + jsonPatchDocument + " to resource\n" + group, ex);
//            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to apply JSON patch.").build();
//        }
//        //there are currently no locked fields as only administrators are allowed to access this operation
//        forbiddenFields = null;
////        (!callerHasWritePermission())
////                ? Boolean.toString(group.isDisabled())
////                + ((group.getExpiresAt() != null) ? new SimpleDateFormat().format(group.getExpiresAt()) : null) : null;
//
//        //create second hash of 'locked' fields
//        String hashAfter = ResourceUtils.hashElements(
//                group.getIdentifier(),
//                (group.getIdentifierScheme() != null) ? group.getIdentifierScheme().getSchemeId() : null,
//                (group.getIdentifierScheme() != null) ? group.getIdentifierScheme().getSchemeUri() : null, forbiddenFields);
//        //compare hashes
//        if (!hashBefore.equals(hashAfter)) {
//            //hashes not equal, invalid modification
//            LOGGER.error("Failed to apply patch\n" + jsonPatchDocument + " to resource\n" + group);
//            //return HTTP UNPROCESSABLE_ENTITY
//            return Response.status(422).entity("Failed to apply JSON patch. Locked fields were affected.").build();
//        }
//        //Update successful, store the updated group and return.
//        try {
//            getResourceService().update(group);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to update group " + group, ex);
//            return ex.toResponse();
//        }
//        return Response.
//                status(Response.Status.NO_CONTENT).
//                tag(Integer.toString(group.hashCode())).
//                build();
//    }
//
//    @DELETE
//    @Path(value = "/{groupId}")
//    @ApiOperation(value = "Delete a group by its unique identifier.",
//            notes = "In order to be able to remove group information, the caller must be a privileged user. "
//            + "Thus, a valid session identifier must be provided in the header.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 204, message = "No content.")
//    })
//    @RoleRequired(Role.ADMINISTRATOR)
//    public Response deleteGroup(
//            @ApiParam(value = "The group identifier.", required = true) @PathParam("groupId") String groupId
//    ) {
//        LOGGER.trace("Removing group with identifier {}.", groupId);
//        try {
//            getResourceService().delete(groupId);
//        } catch (ServiceException ex) {
//            LOGGER.warn("Failed to delete group " + groupId, ex);
//        }
//
//        return Response.status(Response.Status.NO_CONTENT).build();
//    }
//
//    @Override
//    public IGroupServiceAdapter
//            getResourceService() {
//        return ServiceUtil.getService(IGroupServiceAdapter.class
//        );
//    }
}
