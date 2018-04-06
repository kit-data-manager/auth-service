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

import edu.kit.datamanager.auth.web.security.JwtAuthenticationToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author jejkal
 */
@Api(value = "Login Controller")
@ApiResponses(
        value = {
          @ApiResponse(code = 401, message = "Unauthorized")
          ,
        @ApiResponse(code = 403, message = "Forbidden")
          ,
        @ApiResponse(code = 500, message = "Internal server error")
        })
@RestController
public class LoginController{

  private Logger LOGGER;

  @ApiOperation(value = "Perform user login.",
          notes = "The caller authenticates via HTTP Basic and will receive a JSON Web Token in the response body. "
          + "This token must then be provided in subsequent calls to other services within the Authentication header as Bearer token.")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Login successful")})
  @PostMapping("/v1/login")
  public String login(@RequestParam("groupId") String groupId, Authentication authentication){
    JwtAuthenticationToken token = ((JwtAuthenticationToken) authentication);
    LOGGER.debug("Successfully logged in as user {}.", token.getUserId());
    return token.getToken();
  }

}
