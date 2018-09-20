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

import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.exceptions.InvalidAuthenticationException;
import edu.kit.datamanager.security.filter.JwtAuthenticationProvider;
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Extended JWTAuthentication provider providing authentication via
 * username/password or JWToken. This provider is intended to be used to provide
 * both, login and token authentication. The login capabilities are used to
 * determine the logged in user while accessing the login endpoint in order to
 * obtain an initial JWToken which is then used for all other accesses.
 *
 * @author jejkal
 */
public class ExtendedJwtAuthenticationProvider extends JwtAuthenticationProvider{

  private Logger LOGGER;

  private final String secretKey;
  private final IUserService userService;
  private final BCryptPasswordEncoder passwordEncoder;

  public ExtendedJwtAuthenticationProvider(String secretKey, IUserService userService, BCryptPasswordEncoder passwordEncoder, Logger logger){
    super(secretKey, logger);
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
    return authentication instanceof JwtAuthenticationToken
            ? getJwtAuthentication(((JwtAuthenticationToken) authentication).getToken())
            : getJwtAuthentication(getUser(authentication));
  }

  private Authentication getJwtAuthentication(RepoUser user) throws AuthenticationException{
    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    String groupId = attr.getRequest().getParameter("groupId");
    if(groupId == null){
      groupId = "USERS";
    }

    Claims claims = new DefaultClaims();
    claims.put("username", user.getUsername());
    claims.put("firstname", user.getFirstname());
    claims.put("lastname", user.getLastname());
    claims.put("email", user.getEmail());
    claims.put("activeGroup", groupId);

    Set<String> rolesAsString = new HashSet<>();
    user.getRolesAsEnum().forEach((r) -> {
      rolesAsString.add(r.toString());
    });
    claims.put("roles", rolesAsString);
    String token = Jwts.builder().setClaims(claims).setExpiration(DateUtils.addHours(new Date(), 1)).signWith(SignatureAlgorithm.HS512, secretKey).compact();

    return new JwtAuthenticationToken(grantedAuthorities(rolesAsString), user.getUsername(), user.getFirstname(), user.getLastname(), user.getEmail(), groupId, token);
  }

  protected RepoUser getUser(Authentication authentication){
    RepoUser theUser = (RepoUser) userService.loadUserByUsername(authentication.getName());
    if(!theUser.isEnabled()){
      LOGGER.warn("User " + theUser.getUsername() + " is disabled. Falling back to anonymous access.");
      throw new InvalidAuthenticationException("Access denied.");
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
