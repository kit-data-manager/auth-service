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

import edu.kit.datamanager.exceptions.UnauthorizedAccessException;
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import edu.kit.datamanager.util.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author jejkal
 */
@Schema(description = "Login Controller")
@RestController
public class LoginController{

  @Autowired
  private Logger LOGGER;

  @Operation(summary = "Perform user login.",
          description = "The caller authenticates via HTTP Basic and will receive a JSON Web Token in the response body. "
          + "This token must then be provided in subsequent calls to other services within the Authentication header as Bearer token.")
  @PostMapping("/api/v1/login")
  public String login(
          @Parameter(description = "The group id the returned token will associated with. The caller has to be member of the particular group.") @RequestParam(name = "groupId", required = false) String groupId){
    if(!(AuthenticationHelper.getAuthentication() instanceof JwtAuthenticationToken)){
      throw new UnauthorizedAccessException("Access denied");
    }

    JwtAuthenticationToken token = ((JwtAuthenticationToken) AuthenticationHelper.getAuthentication());
    LOGGER.debug("Successfully logged in as user {}.", token.getName());
    return token.getToken();
  }

}
