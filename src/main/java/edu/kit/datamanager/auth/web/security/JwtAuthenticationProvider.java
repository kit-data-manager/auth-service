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
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.auth.service.impl.CustomUserDetailsService;
import edu.kit.datamanager.auth.util.JsonMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import org.apache.commons.lang3.time.DateUtils;
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

  private final String secretKey;
  private final CustomUserDetailsService userDetailsService;
  private final BCryptPasswordEncoder passwordEncoder;

  public JwtAuthenticationProvider(String secretKey, CustomUserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder){
    this.secretKey = secretKey;
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
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
    user.getRoles().forEach((r) -> {
      roleStrings.add(r);
    });
    return new JwtAuthenticationToken(grantedAuthorities(roleStrings), Long.toString(user.getId()), user.getIdentifier(), token);
  }

  @SuppressWarnings("unchecked")
  private Authentication getJwtAuthentication(String token){
    Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

    List<SimpleGrantedAuthority> grantedAuthorities = grantedAuthorities((Set<String>) new HashSet<>((Collection<String>) claimsJws.getBody().get("roles")));
    String identifier = claimsJws.getBody().get("identifier", String.class);
    JwtAuthenticationToken jwtToken = new JwtAuthenticationToken(
            grantedAuthorities,
            identifier,
            identifier,
            token);
    jwtToken.setGroupId(claimsJws.getBody().get("activeGroup", String.class));
    return jwtToken;
  }

  private List<SimpleGrantedAuthority> grantedAuthorities(Set<String> roles){
    return roles.stream().map(String::toString).map(SimpleGrantedAuthority::new).collect(toList());
  }

  private RepoUser getUser(Authentication authentication) throws IOException{
    System.out.println("READ");
    RepoUser theUser = (RepoUser) userDetailsService.loadUserByUsername(authentication.getName());
    System.out.println("USER " + theUser);
    //check if this is needed....
    if(theUser.isEnabled()){
      System.out.println("DENO");
      throw new InvalidAuthenticationException("Access denied");
    }
    String password = theUser.getPassword();
    String providedPassword = (String) authentication.getCredentials();
    System.out.println("COMPARE " + passwordEncoder.matches(providedPassword, password));
    if(providedPassword == null || !passwordEncoder.matches(providedPassword, password)){
      System.out.println("NULL?");
      if(theUser.isEnabled()){
        System.out.println("ENA");
        theUser.setLoginFailures(Math.min(3, theUser.getLoginFailures() + 1));
        if(theUser.getLoginFailures() == 3){
          theUser.setLocked(true);
          theUser.setLockedUntil(DateUtils.addHours(new Date(), 1));
          System.out.println("UPDA");
          userDetailsService.update(theUser);
          System.out.println("DONE");
        }
      }
      System.out.println("DENI");
      throw new InvalidAuthenticationException("Access denied");
    }
    System.out.println("ERA");
    theUser.erasePassword();
    System.out.println("RETUR");
    return theUser;
  }
}
