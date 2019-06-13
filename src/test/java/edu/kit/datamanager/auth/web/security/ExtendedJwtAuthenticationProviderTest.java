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

import com.github.fge.jsonpatch.JsonPatch;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.domain.RepoUserGroup;
import edu.kit.datamanager.auth.service.IGroupService;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.InvalidAuthenticationException;
import edu.kit.datamanager.exceptions.PatchApplicationException;
import edu.kit.datamanager.exceptions.ResourceNotFoundException;
import edu.kit.datamanager.exceptions.UpdateForbiddenException;
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *
 * @author jejkal
 */
public class ExtendedJwtAuthenticationProviderTest{

  private final static String USER_PASSWORD = "test";
  private final static RepoUser USER = new RepoUser();
  private final static BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

  private final static IUserService USER_SERVICE = new IUserService(){
    @Override
    public Page<RepoUser> findAll(RepoUser example, Pageable pgbl){
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RepoUser findById(String id){
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RepoUser create(RepoUser entity, boolean isAdminAccess){
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(RepoUser entity){
      USER.setActive(entity.getActive());
      USER.setLocked(entity.getLocked());
      USER.setLockedUntil(entity.getLockedUntil());
      USER.setLoginFailures(entity.getLoginFailures());
    }

    @Override
    public void patch(RepoUser entity, JsonPatch patch, Collection<? extends GrantedAuthority> userGrants){
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(RepoUser entity){
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Health health(){
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
      if("test".equals(username)){
        return USER;
      }
      return null;
    }

    @Override
    public RepoUser put(RepoUser c, RepoUser c1, Collection<? extends GrantedAuthority> clctn) throws UpdateForbiddenException{
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Page<RepoUser> findAll(RepoUser c, Instant instnt, Instant instnt1, Pageable pgbl){
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

  };

  @Autowired
  private IGroupService userGroupService;

  @BeforeClass
  public static void setup(){
    USER.setActive(Boolean.TRUE);
    USER.setActiveGroup("USERS");
    USER.setEmail("test@mail.org");
    USER.setUsername("test");
    USER.setFirstname("Test");
    USER.setLastname("User");
    USER.setOrcid("0000-0001-1111-111X");
    USER.setId(1l);
    USER.setLocked(Boolean.FALSE);
    USER.setLockedUntil(null);
    USER.setLoginFailures(0);
    USER.setPassword(ENCODER.encode(USER_PASSWORD));
    Collection<RepoUserRole> roles = new ArrayList<>();
    CollectionUtils.addAll(roles, new RepoUserRole[]{RepoUserRole.USER});
    USER.setRolesAsEnum(roles);
  }

  @Test
  public void testSuccessfulAuthentication(){
    ExtendedJwtAuthenticationProvider provider = new ExtendedJwtAuthenticationProvider("test123", USER_SERVICE, userGroupService, ENCODER, LoggerFactory.getLogger(ExtendedJwtAuthenticationProviderTest.class));
    Authentication auth = getAuthentication(USER);

    RepoUser authUser = provider.getUser(auth);

    Assert.assertNotNull(authUser);
    Assert.assertEquals(USER, authUser);
  }

  @Test(expected = InvalidAuthenticationException.class)
  public void testInvalidPassword(){
    ExtendedJwtAuthenticationProvider provider = new ExtendedJwtAuthenticationProvider("test123", USER_SERVICE, userGroupService, ENCODER, LoggerFactory.getLogger(ExtendedJwtAuthenticationProviderTest.class));
    Authentication auth = getAuthentication(USER, "invalid");

    RepoUser authUser = provider.getUser(auth);

    Assert.assertNotNull(authUser);
    Assert.assertEquals(USER, authUser);
  }

  @Test(expected = InvalidAuthenticationException.class)
  public void testInactiveUser(){
    ExtendedJwtAuthenticationProvider provider = new ExtendedJwtAuthenticationProvider("test123", USER_SERVICE, userGroupService, ENCODER, LoggerFactory.getLogger(ExtendedJwtAuthenticationProviderTest.class));
    USER.setActive(false);
    Authentication auth = getAuthentication(USER);
    try{
      provider.getUser(auth);
      Assert.fail("Test should already have failed.");
    } finally{
      USER.setActive(true);
    }
  }

  @Test(expected = InvalidAuthenticationException.class)
  public void testLockedUser(){
    ExtendedJwtAuthenticationProvider provider = new ExtendedJwtAuthenticationProvider("test123", USER_SERVICE, userGroupService, ENCODER, LoggerFactory.getLogger(ExtendedJwtAuthenticationProviderTest.class));
    USER.setLocked(true);
    Authentication auth = getAuthentication(USER);
    try{
      provider.getUser(auth);
      Assert.fail("Test should already have failed.");
    } finally{
      USER.setLocked(false);
    }
  }

  @Test
  public void testLocking(){
    ExtendedJwtAuthenticationProvider provider = new ExtendedJwtAuthenticationProvider("test123", USER_SERVICE, userGroupService, ENCODER, LoggerFactory.getLogger(ExtendedJwtAuthenticationProviderTest.class));
    Assert.assertFalse(USER.getLocked());
    Authentication auth = getAuthentication(USER, null);
    try{
      for(int i = 0; i < 3; i++){
        try{
          provider.getUser(auth);
        } catch(InvalidAuthenticationException ex){
          //ignored
        }
      }
      Assert.assertTrue(USER.getLocked());
      Assert.assertEquals(new Integer(3), USER.getLoginFailures());
    } finally{
      USER.setLockedUntil(null);
      USER.setLoginFailures(0);
      USER.setLocked(Boolean.FALSE);
    }
  }

  @Test
  public void testSupports(){
    ExtendedJwtAuthenticationProvider provider = new ExtendedJwtAuthenticationProvider("test123", USER_SERVICE, userGroupService, ENCODER, LoggerFactory.getLogger(ExtendedJwtAuthenticationProviderTest.class));
    Assert.assertTrue(provider.supports(JwtAuthenticationToken.class));
    Assert.assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class));
    Assert.assertFalse(provider.supports(AnonymousAuthenticationToken.class));
  }

  private Authentication getAuthentication(RepoUser pUser){
    return getAuthentication(pUser, USER_PASSWORD);
  }

  private Authentication getAuthentication(RepoUser pUser, String password){
    return new Authentication(){
      @Override
      public Collection<? extends GrantedAuthority> getAuthorities(){
        return pUser.getAuthorities();
      }

      @Override
      public Object getCredentials(){
        return password;
      }

      @Override
      public Object getDetails(){
        return pUser;
      }

      @Override
      public Object getPrincipal(){
        return pUser.getUsername();
      }

      @Override
      public boolean isAuthenticated(){
        return true;
      }

      @Override
      public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException{

      }

      @Override
      public String getName(){
        return pUser.getUsername();
      }
    };
  }

}
