/*
 * Copyright 2018 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.auth.web.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.exceptions.CustomInternalServerError;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.auth.util.JsonMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author jejkal
 */
public class JwtAuthenticationProvider implements AuthenticationProvider, JsonMapper{

  private Logger LOGGER;

  private final String secretKey;
  private final IUserService userService;
  private final BCryptPasswordEncoder passwordEncoder;

  public JwtAuthenticationProvider(String secretKey, IUserService userService, BCryptPasswordEncoder passwordEncoder, Logger logger){
    this.secretKey = secretKey;
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.LOGGER = logger;
  }

  @Override
  public boolean supports(Class<?> authentication){
    return JwtAuthenticationToken.class.isAssignableFrom(authentication) || UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException{
    try{
      return authentication instanceof JwtAuthenticationToken
              ? getJwtAuthentication(((JwtAuthenticationToken) authentication).getToken())
              : getJwtAuthentication(getUser(authentication));
    } catch(RuntimeException | IOException e){
      throw new InvalidAuthenticationException("Access denied", e);
    }
  }

  private Authentication getJwtAuthentication(RepoUser user) throws JsonProcessingException{
    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    String groupId = attr.getRequest().getParameter("groupId");
    if(groupId == null){
      groupId = "USERS";
    }
    user.erasePassword();
    user.setActiveGroup(groupId);
    String token = Jwts.builder().setPayload(mapper.writeValueAsString(user)).signWith(SignatureAlgorithm.HS512, secretKey).compact();
    Set<String> roleStrings = new HashSet<>();
    user.getRolesAsEnum().forEach((r) -> {
      roleStrings.add(r.toString());
    });
    JwtAuthenticationToken result = new JwtAuthenticationToken(grantedAuthorities(roleStrings), Long.toString(user.getId()), user.getUsername(), token);
    return result;
  }

  @SuppressWarnings("unchecked")
  private Authentication getJwtAuthentication(String token){
    Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
    List<String> rolesList = new ArrayList<>();
    String roleClaim = claimsJws.getBody().get("roles", String.class);
    if(roleClaim == null){
      LOGGER.error("No 'roles' claim found in JWT " + claimsJws);
      throw new CustomInternalServerError("Unprocessable authentication token.");
    }

    try{
      final JsonNode jsonNode = new ObjectMapper().readTree(roleClaim);
      if(jsonNode.isArray()){
        for(JsonNode node : jsonNode){
          String role = node.asText();
          rolesList.add(role);
        }
      } else{
        throw new IllegalArgumentException("Roles claim '" + roleClaim + "' seems to be no JSON array.");
      }
    } catch(IOException | IllegalArgumentException ex){
      LOGGER.error("Failed to read user roles from " + this, ex);
      throw new CustomInternalServerError("Failed to read user roles.");
    }

    List<SimpleGrantedAuthority> grantedAuthorities = grantedAuthorities((Set<String>) new HashSet<>(rolesList));
    String username = claimsJws.getBody().get("username", String.class);
    JwtAuthenticationToken jwtToken = new JwtAuthenticationToken(
            grantedAuthorities,
            username,
            username,
            token);
    jwtToken.setGroupId(claimsJws.getBody().get("activeGroup", String.class));
    return jwtToken;
  }

  private List<SimpleGrantedAuthority> grantedAuthorities(Set<String> roles){
    return roles.stream().map(String::toString).map(SimpleGrantedAuthority::new).collect(toList());
  }

  private RepoUser getUser(Authentication authentication) throws IOException{
    RepoUser theUser = (RepoUser) userService.loadUserByUsername(authentication.getName());
    if(!theUser.isEnabled()){
      LOGGER.warn("User " + theUser.getUsername() + " is disabled. Falling back to anonymous access.");
      throw new InvalidAuthenticationException("Access denied");
    }
    String password = theUser.getPassword();
    String providedPassword = (String) authentication.getCredentials();
    if(providedPassword == null || !passwordEncoder.matches(providedPassword, password)){
      theUser.setLoginFailures(Math.min(3, theUser.getLoginFailures() + 1));
      if(theUser.getLoginFailures() == 3){
        theUser.setLocked(true);
        theUser.setLockedUntil(DateUtils.addHours(new Date(), 1));
        LOGGER.warn("Too many failed login attempts for user " + theUser.getUsername() + ". User will be locked until " + theUser.getLockedUntil() + ".");
      }
      userService.update(theUser);
      LOGGER.warn("Wrong password provided for user " + theUser.getUsername() + " (Attempt: " + theUser.getLoginFailures() + ")");
      throw new InvalidAuthenticationException("Access denied");
    }
    LOGGER.debug("Successful login for user " + theUser.getUsername() + ".");
    return theUser;
  }
}
