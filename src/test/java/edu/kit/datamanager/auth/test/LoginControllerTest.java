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
package edu.kit.datamanager.auth.test;

import edu.kit.datamanager.auth.configuration.ApplicationProperties;
import edu.kit.datamanager.auth.dao.IGroupDao;
import edu.kit.datamanager.auth.dao.IUserDao;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.entities.RepoUserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.util.Base64Utils;

/**
 *
 * @author jejkal
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
  DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class,
  TransactionalTestExecutionListener.class,
  WithSecurityContextTestExecutionListener.class})
@ActiveProfiles("test")
public class LoginControllerTest{

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private IUserDao userDao;
  @Autowired
  private IGroupDao groupDao;
  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private ApplicationProperties applicationProperties;

  private RepoUser adminUser;
  private RepoUser defaultUser;
  private RepoUser inactiveUser;

  @Before
  public void setUp(){
    //clean database
    groupDao.deleteAll();
    userDao.deleteAll();

    //add admin
    RepoUser admin = new RepoUser();
    admin.setUsername("admin");
    admin.setActive(true);
    admin.setLocked(false);
    admin.setPassword(passwordEncoder.encode("admin"));
    admin.setRolesAsEnum(Arrays.asList(RepoUserRole.ADMINISTRATOR));
    admin.setEmail("test@mail.org");
    adminUser = userDao.saveAndFlush(admin);

    //add defaultUser
    RepoUser user = new RepoUser();
    user.setUsername("user");
    user.setActive(true);
    user.setLocked(false);
    user.setPassword(passwordEncoder.encode("user"));
    user.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    user.setEmail("test@mail.org");
    defaultUser = userDao.saveAndFlush(user);

    //add inactiveUser
    RepoUser inactive = new RepoUser();
    inactive.setUsername("inactive");
    inactive.setActive(false);
    inactive.setLocked(false);
    inactive.setPassword(passwordEncoder.encode("inactive"));
    inactive.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    inactive.setEmail("test@mail.org");
    inactiveUser = userDao.saveAndFlush(inactive);
  }

  @Test
  public void testLogin() throws Exception{
    //authenticate and login
    MvcResult result = this.mockMvc.perform(post("/api/v1/login/").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andReturn();
    String jwt = result.getResponse().getContentAsString();

    //check token (validate signature and check username)
    Jws<Claims> claimsJws = Jwts.parser().setSigningKey(applicationProperties.getJwtSecret()).parseClaimsJws(jwt);
    Assert.assertEquals(adminUser.getUsername(), claimsJws.getBody().get("username", String.class));

    //use token to obtain own user information
    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt)).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").value("admin"));
  }

  @Test
  public void testLoginAsInactive() throws Exception{
    this.mockMvc.perform(post("/api/v1/login/").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("inactive:inactive".getBytes()))).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testResourceAccessAsDisabledUser() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("inactive:inactive".getBytes()))).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testDisableUserAfterFailedLogins() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(status().isOk());

    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:wrongPassword".getBytes()))).andDo(print()).andExpect(status().isUnauthorized());
    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:wrongPassword".getBytes()))).andDo(print()).andExpect(status().isUnauthorized());
    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:wrongPassword".getBytes()))).andDo(print()).andExpect(status().isUnauthorized());

    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testExpiredToken() throws Exception{
    Claims claims = new DefaultClaims();
    claims.put("username", adminUser.getUsername());
    claims.put("firstname", adminUser.getFirstname());
    claims.put("lastname", adminUser.getLastname());
    claims.put("email", adminUser.getEmail());
    claims.put("activeGroup", adminUser.getActiveGroup());
    claims.put("roles", adminUser.getRolesAsEnum());
    String token = Jwts.builder().setClaims(claims).setExpiration(DateUtils.addMilliseconds(new Date(), 1)).signWith(SignatureAlgorithm.HS512, applicationProperties.getJwtSecret()).compact();

    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Bearer " + token)).andDo(print()).andExpect(status().isUnauthorized());

  }

}
