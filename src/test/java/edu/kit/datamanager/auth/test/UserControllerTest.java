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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.auth.dao.IUserDao;
import edu.kit.datamanager.auth.domain.RepoUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class UserControllerTest{

  @Autowired
  private MockMvc mockMvc;
  private final List<RepoUser> userList = new ArrayList<>();

  @Autowired
  private IUserDao userDao;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  private RepoUser adminUser;
  private RepoUser defaultUser;
  private RepoUser inactiveUser;

  @Before
  public void setUp(){
    //clean database
    userDao.deleteAll();

    //add admin
    RepoUser admin = new RepoUser();
    admin.setUsername("admin");
    admin.setActive(true);
    admin.setLocked(false);
    admin.setPassword(passwordEncoder.encode("admin"));
    admin.setRolesAsEnum(Arrays.asList(RepoUser.UserRole.ADMINISTRATOR));
    admin.setEmail("test@mail.org");
    adminUser = userDao.saveAndFlush(admin);

    //add user
    RepoUser user = new RepoUser();
    user.setUsername("user");
    user.setActive(true);
    user.setLocked(false);
    user.setPassword(passwordEncoder.encode("user"));
    user.setRolesAsEnum(Arrays.asList(RepoUser.UserRole.USER));
    user.setEmail("test@mail.org");
    defaultUser = userDao.saveAndFlush(user);

    //add inactive user
    RepoUser inactive = new RepoUser();
    inactive.setUsername("inactive");
    inactive.setActive(false);
    inactive.setLocked(false);
    inactive.setPassword(passwordEncoder.encode("inactive"));
    inactive.setRolesAsEnum(Arrays.asList(RepoUser.UserRole.USER));
    inactive.setEmail("test@mail.org");
    inactiveUser = userDao.saveAndFlush(inactive);
    System.out.println("INAVN " + inactiveUser);
  }

  @Test
  public void testGetUserListAsAdmin() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/").param("page", "0").param("size", "10").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("admin"));
  }

  @Test
  public void testGetUserListWithExceededPageSize() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/").param("page", "0").param("size", "1000").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("admin"));
  }

  @Test
  public void getUserListAsUser() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(status().isForbidden());
  }

  @Test
  public void getUserListAsAnonymous() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/")).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void getUserListAsInactive() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("inactive:inactive".getBytes()))).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testGetAdminUserByAdmin() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/1").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").value("admin"));
  }

  @Test
  public void testGetAdminUserCheckPasswordReset() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/" + adminUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.password").isEmpty());
  }

  @Test
  public void testGetInvalidUserByAdmin() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/0").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testGetInactiveUserDetails() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/" + inactiveUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("inactive:inactive".getBytes()))).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testGetAdminUserByUser() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/" + adminUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(status().isForbidden());
  }

  @Test
  public void testGetAdminUserByAnonymous() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/" + adminUser.getId())).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testRegisterUser() throws Exception{
    RepoUser created = new RepoUser();
    created.setUsername("created");
    created.setPassword("created");
    created.setRolesAsEnum(Arrays.asList(RepoUser.UserRole.USER));
    created.setEmail("test@mail.org");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(created))).andDo(print()).andExpect(status().isCreated());//.andExpect(MockMvcResultMatchers.jsonPath("$.username").value("created"));
    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("created:created".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").value("created"));
  }

  @Test
  public void testRegisterUserWithoutPassword() throws Exception{
    RepoUser created = new RepoUser();
    created.setUsername("created");
    created.setRolesAsEnum(Arrays.asList(RepoUser.UserRole.USER));
    created.setEmail("test@mail.org");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(created))).andDo(print()).andExpect(status().isBadRequest());
  }

}
