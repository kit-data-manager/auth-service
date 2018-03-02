/*
 * Copyright 2017 Karlsruhe Institute of Technology.
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

import edu.kit.datamanager.auth.domain.Note;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author jejkal
 */
//@Path("/acl")
@Api(value = "Access Control")
@ApiResponses(
        value = {
          @ApiResponse(code = 401, message = "Unauthorized")
          ,
        @ApiResponse(code = 403, message = "Forbidden")
          ,
        @ApiResponse(code = 500, message = "Internal server error")
        })
//@Produces("application/json")
@RestController
public class AclController{//extends AbstractBaseResource<ResourceAcl> {

  final AclService aclService;

  @Autowired
  public AclController(AclService aclService){
    this.aclService = aclService;
  }

  @ApiOperation(value = "Login and receive a JSON Web Token.", notes = "The caller is authenticated via HTTP Basic authentication. The resulting JSON Web Token is returned directly in the response and can be provided to subsequent calls "
          + "via X-AUTH-TOKEN header.")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Login successful")})
  @GetMapping("/api/v1/acl")
  public ResponseEntity getAcl(){
    Acl acl = aclService.readAclById(new ObjectIdentityImpl(Note.class, new Long(1)));
    return ResponseEntity.ok(acl);
  }

//    private static final Logger LOGGER = LoggerFactory.getLogger(AclController.class);
//
//    @Context
//    ContainerRequestContext context;
//
//    @Override
//    public ContainerRequestContext getContext() {
//        return context;
//    }
//
//    @POST
//    @Path(value = "/")
//    @ApiOperation(value = "Register a new access control list for a specific resource.", notes = "")
//    @ApiResponses(value = {
//        @ApiResponse(code = 201, message = "Successfully created access control list.", response = ResourceAcl.class)
//        ,
//            @ApiResponse(code = 303, message = "See other including location link pointing to an existing resource ACL.")
//        ,
//        @ApiResponse(code = 409, message = "Conflict due to existing ACL not accessible by caller.")})
//    @RoleRequired(Role.USER)
//    public Response registerResourceAcl(
//            @ApiParam(value = "The JSON representation of the access control list.", required = true) ResourceAcl acl
//    ) {
//        String sid = ServiceUtil.getSessionIdFromRequest(getContext());
//        try {
//            ValidationHelper.validate(acl);
//        } catch (InvalidResourceException ex) {
//            LOGGER.error("Failed to validate resource " + acl, ex);
//            return ex.toResponse();
//        }
//
//        ResourceAcl existingAcl;
//        try {
//            existingAcl = getResourceService().read(acl.getResourceId());
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to obtain existing ACL for resource id " + acl.getResourceId(), ex);
//            return ex.toResponse();
//        }
//
//        if (existingAcl != null) {
//            UserPrincipal principal = (UserPrincipal) getContext().getSecurityContext().getUserPrincipal();
//            Permission principalPermission = ResourceAclHelper.getPermission(principal, existingAcl);
//
//            //Check if principal has read permissions to resource. If not, there is also no access to the ACL granted and FORBIDDEN is returned. 
//            if (Permission.NONE.eq(principalPermission)) {
//                LOGGER.debug("Access to resource with identifier '{}' is not granted to principal '{}'. Returning HTTP CONFLICT..", acl.getResourceId(), principal);
//                //result = ResourceAcl.createNoAccessAcl(acl.getResourceId());
//                return Response.status(Status.CONFLICT).entity("Conflict with existing ACL.").type(MediaType.TEXT_PLAIN).build();
//            } else {
//                //redirect user
//                URI destination;
//                if (sid != null) {
//                    destination = ServiceUtil.getResourceUri(getContext(), acl.getResourceId(), sid);
//                } else {
//                    //typically, we arrive here only if the sessionId is not provided as header field but as Cookie or query parameter
//                    destination = ServiceUtil.getResourceUri(getContext(), acl.getResourceId());
//                }
//
//                LOGGER.trace("ACL for resource with identifier '{}' already exist. Redirecting caller to resource {}.", acl.getResourceId(), destination);
//                return Response.status(Response.Status.SEE_OTHER).location(destination).build();
//            }
//        }
//
//        ResourceAcl result;
//        try {
//            result = getResourceService().create(acl);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to save acl " + acl, ex);
//            return ex.toResponse();
//        }
//
//        //create cache control
//        CacheControl cc = new CacheControl();
//        cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
//        cc.setPrivate(true);
//        //return group with cache control, tag end entity
//        return Response.created(ServiceUtil.getResourceUri(getContext(), result.getResourceId())).
//                cacheControl(cc).
//                tag(Integer.toString(result.hashCode())).
//                entity(result).
//                build();
//    }
//
//    @GET
//    @Path(value = "/{resourceIdentifier}")
//    @ApiOperation(value = "Get the access control list for the resource identified by its unique identifier.",
//            notes = "In order to be able to access Acl information, a valid session identifier must be provided in the header.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Success", response = ResourceAcl.class)
//    })
//    @RoleRequired(Role.GUEST)
//    public Response getResourceAcl(
//            @ApiParam(value = "The resource identifier.", required = true) @PathParam("resourceIdentifier") String resourceIdentifier
//    ) {
//        ResourceAcl acl;
//        try {
//            acl = getResourceService().read(resourceIdentifier);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to obtain ACL for resource identifier " + resourceIdentifier, ex);
//            return ex.toResponse();
//        }
//
//        if (acl == null) {
//            LOGGER.trace("No ACL for resource identifier '{}' found. Returning public read ACL.", resourceIdentifier);
//            acl = ResourceAcl.createPublicReadAcl(resourceIdentifier);
//        }
//
//        UserPrincipal principal = (UserPrincipal) getContext().getSecurityContext().getUserPrincipal();
//        Permission principalPermission = ResourceAclHelper.getPermission(principal, acl);
//        //Check if principal has read permissions to resource. If not, a no-access ACL is returned. 
//        if (Permission.NONE.eq(principalPermission)) {
//            LOGGER.debug("Access to resource with identifier '{}' is not granted to principal '{}'. Returning no-access ACL.", resourceIdentifier, principal);
//            acl = ResourceAcl.createNoAccessAcl(resourceIdentifier);
//        }
//        CacheControl cc = new CacheControl();
//        cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
//        cc.setPrivate(true);
//        return Response.
//                ok(acl).
//                tag(Integer.toString(acl.hashCode())).
//                cacheControl(cc).build();
//    }
//
//    @GET
//    @Path(value = "/")
//    @ApiOperation(value = "Get a list of access control lists matching the provided query.",
//            notes = "In order to be able to access Acl information, a valid session identifier must be provided in the header.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Success", response = ResourceAcl[].class)
//    })
//    @RoleRequired(Role.ADMINISTRATOR)
//    public Response queryResourceAcl(@ApiParam(value = "The query refining the result set.", required = false) @QueryParam("query") String query,
//            @ApiParam(value = "The JSON object containing the query variables, e.g. {\"v1\":\"test\",\"v2\":2,\"v3\":[\"test\",\"test1\"]}", required = false) @QueryParam("variables") String variables,
//            @ApiParam(value = "The page to show.", required = false) @QueryParam("page") @DefaultValue("1") Integer page,
//            @ApiParam(value = "The max. number of results per page.", required = false) @QueryParam("resultsPerPage") @DefaultValue("10") Integer resultsPerPage,
//            @ApiParam(value = "The field name by which the result set is sorted.", required = false) @QueryParam("sort") String sortField,
//            @ApiParam(value = "The result sort order (asc or desc).", required = false) @QueryParam("order") @DefaultValue("ASC") IBaseDao.SORT_ORDER order
//    ) {
//        Map<String, Object> variableMap = null;
//        if (variables != null) {
//            try {
//                variableMap = new ObjectMapper().readValue(variables, HashMap.class);
//            } catch (IOException ex) {
//                LOGGER.error("Invalid format of variables argument with value '" + variables + "'.", ex);
//                return Response.status(Response.Status.BAD_REQUEST).entity("Bad variables map format.").build();
//            }
//        }
//        QueryResult<ResourceAcl> result;
//
//        try {
//            result = getResourceService().read(query, variableMap, page, resultsPerPage, sortField, order);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to perform query '" + query + "' for existing ACLs.", ex);
//            return ex.toResponse();
//        }
//        if (result.isEmpty()) {
//            LOGGER.trace("No ACL for query '{}' found. Returning HTTP NOT FOUND.", query);
//            return Response.status(Status.NOT_FOUND).entity("No ACL found for provided query.").build();
//        }
//        String sessionId = ServiceUtil.getSessionIdFromResource(this);
//
//        //correct resources according to principal permissions
//        UserPrincipal principal = (UserPrincipal) getContext().getSecurityContext().getUserPrincipal();
//        LOGGER.trace("Checking obtained ACLs for access by principal {}.", principal);
//        result.getResults().forEach((entry) -> {
//            Permission principalPermission = ResourceAclHelper.getPermission(principal, entry);
//            if (Permission.NONE.eq(principalPermission)) {
//                LOGGER.debug("Access to resource with identifier '{}' is not granted to principal '{}'. Returning no-access ACL.", entry.getResourceId(), principal);
//                entry.getGroupAcl().clear();
//                entry.getUserAcl().clear();
//                entry.getUserAcl().add(new PermissionMapping(".*", Permission.NONE));
//            }
//        });
//
//        CacheControl cc = new CacheControl();
//        cc.setMaxAge((int) DateUtils.MILLIS_PER_HOUR);
//        cc.setPrivate(true);
//
//        return Response.ok(result.getResults().toArray(new ResourceAcl[]{})).
//                cacheControl(cc).
//                links(
//                        Link.fromUri(ServiceUtil.getNextPageLink(getContext(), 1, resultsPerPage, sortField, order, sessionId)).param("rel", "next").build(),
//                        Link.fromUri(ServiceUtil.getLastPageLink(getContext(), result.getOverallResults(), resultsPerPage, sortField, order, sessionId)).param("rel", "last").build()
//                ).build();
//    }
//
//    @PUT
//    @Path(value = "/{resourceIdentifier}")
//    @ApiOperation(value = "Update an access control list for a specific resource.", notes = "")
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Successfully updated access control list.", response = ResourceAcl.class)
//        ,
//            @ApiResponse(code = 404, message = "Existing resource access control list not found.")
//    })
//    @RoleRequired(Role.USER)
//    public Response updateResourceAcl(
//            @ApiParam(value = "The resource identifier.", required = true) @PathParam("resourceIdentifier") String resourceIdentifier,
//            @ApiParam(value = "The JSON representation of the access control list.", required = true) ResourceAcl acl
//    ) {
//
////        String sid = ServiceUtil.getSessionIdFromRequest(getContext());
////        try {
////            ValidationHelper.validate(acl);
////        } catch (InvalidResourceException ex) {
////            LOGGER.error("Failed to validate resource " + acl, ex);
////            return ex.toResponse();
////        }
//        ResourceAcl existingAcl;
//        try {
//            existingAcl = getResourceService().read(acl.getResourceId());
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to obtain existing ACL for resource id " + acl.getResourceId(), ex);
//            return ex.toResponse();
//        }
//
//        if (existingAcl == null) {
//            LOGGER.trace("Existing access control list for resource identifier {} not found. Returning HTTP 404.", resourceIdentifier);
//            return Response.status(Response.Status.NOT_FOUND).entity("Access control list not found.").type(MediaType.TEXT_PLAIN).build();
//
////            UserPrincipal principal = (UserPrincipal) getContext().getSecurityContext().getUserPrincipal();
////            Permission principalPermission = ResourceAclHelper.getPermission(principal, existingAcl);
////
////            //Check if principal has read permissions to resource. If not, there is also no access to the ACL granted and FORBIDDEN is returned. 
////            if (Permission.NONE.eq(principalPermission)) {
////                LOGGER.debug("Access to resource with identifier '{}' is not granted to principal '{}'. Returning HTTP CONFLICT..", acl.getResourceId(), principal);
////                //result = ResourceAcl.createNoAccessAcl(acl.getResourceId());
////                return Response.status(Status.CONFLICT).entity("Conflict with existing ACL.").type(MediaType.TEXT_PLAIN).build();
////            } else {
////                //redirect user
////                URI destination;
////                if (sid != null) {
////                    destination = ServiceUtil.getResourceUri(getContext(), acl.getResourceId(), sid);
////                } else {
////                    //typically, we arrive here only if the sessionId is not provided as header field but as Cookie or query parameter
////                    destination = ServiceUtil.getResourceUri(getContext(), acl.getResourceId());
////                }
////
////                LOGGER.trace("ACL for resource with identifier '{}' already exist. Redirecting caller to resource {}.", acl.getResourceId(), destination);
////                return Response.status(Response.Status.SEE_OTHER).location(destination).build();
////            }
//        }
//
//        UserPrincipal principal = (UserPrincipal) getContext().getSecurityContext().getUserPrincipal();
//        Permission principalPermission = ResourceAclHelper.getPermission(principal, existingAcl);
//
//        if (!Permission.WRITE.eq(principalPermission) && !callerIsAdministrator()) {
//            LOGGER.error("Insufficient privileges of caller {} to update access control list for resource {}.", getCallerIdentifier(), resourceIdentifier);
//            return Response.status(Response.Status.FORBIDDEN).entity("Caller is not allowed to update access control list.").type(MediaType.TEXT_PLAIN).build();
//        }
//
//        //setting resource identifier
//        acl.setResourceId(resourceIdentifier);
//
//        //validate result
//        try {
//            ValidationHelper.validate(acl);
//        } catch (InvalidResourceException ex) {
//            LOGGER.error("Failed to validate updated access control list " + acl, ex);
//            return ex.toResponse();
//        }
//
//        //persist result
//        ResourceAcl result;
//        try {
//            result = getResourceService().create(acl);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to save acl " + acl, ex);
//            return ex.toResponse();
//        }
//
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
//    @Path(value = "/{resourceIdentifier}")
//    @ApiOperation(value = "Update the access control list for the resource with the provided resource identifier.",
//            notes = "In order to be able to access ACL information, a valid session identifier must be provided in the header. "
//            + "This endpoint can be used e.g. to add or remove elements from the user/group ACLs.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 204, message = "No Content.")
//        ,
//        @ApiResponse(code = 404, message = "Resource not found, e.g. invalid resource identifier.")
//        ,
//        @ApiResponse(code = 409, message = "Conflict, e.g. resource has changed.")
//        ,
//        @ApiResponse(code = 415, message = "Unsupported Media Type, e.g. unsupported patch document format.")
//
//    })
//    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
//    @RoleRequired(Role.USER)
//    public Response patchAcl(
//            @ApiParam(value = "The resource identifier.", required = true) @PathParam("resourceIdentifier") String resourceIdentifier,
//            @ApiParam(value = "A JSON Patch document according to IETF RFC 6901.", example = "[\n"
//                    + "  { \"op\": \"replace\", \"path\": \"/groupAcl/0/permission\", \"value\": \"NONE\" }\n"
//                    + "]", required = true) String jsonPatchDocument,
//            @ApiParam(value = "The ETag of the resource.", hidden = true) @HeaderParam(HttpHeaders.IF_NONE_MATCH) String etag) {
//        //obtain user principal, validity check is implicitly done
//        UserPrincipal principal = (UserPrincipal) getContext().getSecurityContext().getUserPrincipal();
//        if (!ServiceUtil.isPatchApplicable(resourceIdentifier, principal, jsonPatchDocument)) {
//            LOGGER.trace("Principal '{}' is not allowed to apply patch to resource '{}'. Returning HTTP FORBIDDEN.", principal, resourceIdentifier);
//            return Response.status(Response.Status.FORBIDDEN).entity("Caller " + getCallerIdentifier() + " is not allowed to patch ACLs.").build();
//        }
//
//        ResourceAcl acl;
//        try {
//            acl = getResourceService().read(resourceIdentifier);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to obtain existing acl for resource identifier " + resourceIdentifier, ex);
//            return ex.toResponse();
//        }
//
//        //check ETag and fail if acl has changed between calls
//        ServiceUtil.checkETag(getContext().getRequest(), acl);
//        //create hash of unchangable fields
//        String hashBefore = ResourceUtils.hashElements(acl.getResourceId());
//        try {
//            //apply patch to acl
//            acl = ServiceUtil.applyPatch(acl, jsonPatchDocument, ResourceAcl.class);
//        } catch (IOException ex) {
//            LOGGER.error("Failed to apply patch\n" + jsonPatchDocument + " to resource\n" + acl, ex);
//            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to apply JSON patch.").build();
//        }
//        //create hash of unchangable fields
//        String hashAfter = ResourceUtils.hashElements(acl.getResourceId());
//        //compare hashes (should be equal)
//        if (!hashBefore.equals(hashAfter)) {
//            LOGGER.error("Failed to apply patch\n" + jsonPatchDocument + " to resource\n" + acl);
//            //return HTTP UNPROCESSABLE_ENTITY
//            return Response.status(422).build();
//        }
//        //Finally, persist updated acl and return
//        try {
//            updateResource(acl);
//        } catch (ServiceException ex) {
//            LOGGER.error("Failed to save acl " + acl, ex);
//            return ex.toResponse();
//        }
//        return Response.
//                status(Response.Status.NO_CONTENT).
//                tag(Integer.toString(acl.hashCode())).
//                build();
//    }
//
//    @DELETE
//    @Path(value = "/{resourceIdentifier}")
//    @ApiOperation(value = "Delete the Acl for the resource with the provided resource identifier.",
//            notes = "In order to be able to access Acl information, a valid session identifier must be provided in the header. "
//            + "Removing the Acl for a resource makes the resource publicly accessible.")
//    @ApiResponses(value = {
//        @ApiResponse(code = 204, message = "No content.")
//    })
//    @RoleRequired(Role.USER)
//    public Response deleteAcl(@ApiParam(value = "The resource identifier.", required = true) @PathParam("resourceIdentifier") String resourceIdentifier) {
//        //obtain user principal, validity check is implicitly done
//        UserPrincipal principal = (UserPrincipal) getContext().getSecurityContext().getUserPrincipal();
//
//        ResourceAcl acl = null;
//        try {
//            acl = getResourceService().read(resourceIdentifier);
//        } catch (ServiceException ex) {
//            LOGGER.info("Failed to obtain existing acl for resource identifier " + resourceIdentifier + ". Aborting delete operation.", ex);
//        }
//
//        if (acl != null) {
//            Permission principalPermission = ResourceAclHelper.getPermission(principal, acl);
//
//            //Check if principal has WRITE permissions to resource. If not, changing the ACL is FORBIDDEN. 
//            if (!Permission.WRITE.eq(principalPermission)) {
//                LOGGER.debug("Write access to resource with identifier '{}' is not granted to principal '{}'. Returning HTTP FORBIDDEN.", resourceIdentifier, principal);
//                return Response.status(Response.Status.FORBIDDEN).build();
//            }
//            try {
//                deleteResource(acl);
//            } catch (ServiceException ex) {
//                LOGGER.error("Failed to delete acl " + acl + ". Please delete acl manually.", ex);
//            }
//        }
//
//        return Response.status(Response.Status.NO_CONTENT).build();
//    }
//
//    @Override
//    public IAccessControlListServiceAdapter getResourceService() {
//        return ServiceUtil.getService(IAccessControlListServiceAdapter.class);
//    }
}
