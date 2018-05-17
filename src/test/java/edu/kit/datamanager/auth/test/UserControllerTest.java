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
import edu.kit.datamanager.entities.RepoUserRole;
import java.util.Arrays;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
public class UserControllerTest{

  @Autowired
  private MockMvc mockMvc;

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
    admin.setRolesAsEnum(Arrays.asList(RepoUserRole.ADMINISTRATOR));
    admin.setEmail("test@mail.org");
    adminUser = userDao.saveAndFlush(admin);
    //add user
    RepoUser user = new RepoUser();
    user.setUsername("user");
    user.setActive(true);
    user.setLocked(false);
    user.setPassword(passwordEncoder.encode("user"));
    user.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    user.setEmail("test@mail.org");
    defaultUser = userDao.saveAndFlush(user);

    //add inactive user
    RepoUser inactive = new RepoUser();
    inactive.setUsername("inactive");
    inactive.setActive(false);
    inactive.setLocked(false);
    inactive.setPassword(passwordEncoder.encode("inactive"));
    inactive.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    inactive.setEmail("test@mail.org");
    inactiveUser = userDao.saveAndFlush(inactive);

    //add some dummy users in order to test for the Link header
    for(int i = 0; i < 10; i++){
      RepoUser dummyUser = new RepoUser();
      dummyUser.setUsername("dummy" + i);
      dummyUser.setActive(false);
      dummyUser.setLocked(false);
      dummyUser.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
      dummyUser.setEmail("dummy@mail.org");
      userDao.saveAndFlush(dummyUser);
    }
  }

  @Test
  public void testGetUserListAsAdmin() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/").param("page", "0").param("size", "10").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("admin")).andExpect(header().exists("Link"));
  }

  @Test
  public void testGetUserListByExampleAsAdmin() throws Exception{
    RepoUser example = new RepoUser();
    example.setUsername("user");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/search").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(example)).param("page", "0").param("size", "10").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("user")).andExpect(MockMvcResultMatchers.jsonPath("$[1]").doesNotExist());
  }

  @Test
  public void testGetUserListByExampleWithPatternAsAdmin() throws Exception{
    RepoUser example = new RepoUser();
    example.setEmail("%mail.org%");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/search").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(example)).param("page", "0").param("size", "10").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("test@mail.org")).andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value("test@mail.org"));
  }

  @Test
  public void testGetUserListWithExceededPageSize() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/").param("page", "0").param("size", "1000").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("admin"));
  }

  @Test
  public void testGetUserListWithExceededPageNumber() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/").param("page", "100").param("size", "100").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotExist());
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
    this.mockMvc.perform(get("/api/v1/users/" + adminUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").value("admin"));
  }

  @Test
  public void testGetAdminUserCheckPasswordReset() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/" + adminUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.password").doesNotExist());
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
  public void testGetMeAsAnonymous() throws Exception{
    this.mockMvc.perform(get("/api/v1/users/me")).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testPatchAsAnonymous() throws Exception{
    String patch = "[{\"op\": \"replace\",\"path\": \"/identifier\",\"value\": \"test1\"}]";
    this.mockMvc.perform(patch("/api/v1/users/" + adminUser.getId()).contentType("application/json-patch+json").content(patch)).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testPatchOfUnknownUser() throws Exception{
    String patch = "[{\"op\": \"replace\",\"path\": \"/identifier\",\"value\": \"test1\"}]";
    this.mockMvc.perform(patch("/api/v1/users/0").contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testPatchForbiddenField() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/users/" + adminUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");
    String patch = "[{\"op\": \"replace\",\"path\": \"/id\",\"value\": 2}]";
    this.mockMvc.perform(patch("/api/v1/users/" + adminUser.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-None-Match", etag)).andDo(print()).andExpect(status().isForbidden());
  }

  @Test
  public void testPatchOtherUserWithoutAdminRole() throws Exception{
    String patch = "[{\"op\": \"replace\",\"path\": \"/email\",\"value\": \"changed\"}]";
    this.mockMvc.perform(patch("/api/v1/users/" + inactiveUser.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(status().isForbidden());
  }

  @Test
  public void testPatchUsernameWithoutAdminRole() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/users/" + defaultUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"replace\",\"path\": \"/username\",\"value\": \"changed\"}]";
    this.mockMvc.perform(patch("/api/v1/users/" + defaultUser.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes())).header("If-None-Match", etag)).andDo(print()).andExpect(status().isForbidden());
  }

  @Test
  public void testPatchUsernameAsAdmin() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/users/" + inactiveUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"replace\",\"path\": \"/username\",\"value\": \"changed\"}]";
    //patching username is also not allowed for admin
    this.mockMvc.perform(patch("/api/v1/users/" + inactiveUser.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-None-Match", etag)).andExpect(status().isForbidden());
  }

  @Test
  public void testPatchWithoutEtag() throws Exception{
    String patch = "[{\"op\": \"replace\",\"path\": \"/username\",\"value\": \"changed\"}]";
    this.mockMvc.perform(patch("/api/v1/users/" + inactiveUser.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isPreconditionFailed());
  }

  @Test
  public void testPatchWithWrongEtag() throws Exception{
    String patch = "[{\"op\": \"replace\",\"path\": \"/username\",\"value\": \"changed\"}]";
    this.mockMvc.perform(patch("/api/v1/users/" + inactiveUser.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-None-Match", "fail")).andExpect(status().isPreconditionFailed());
  }

  @Test
  public void testPatchUsernameAsUser() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/users/" + defaultUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");
    String patch = "[{\"op\": \"replace\",\"path\": \"/username\",\"value\": \"changed\"}]";
    this.mockMvc.perform(patch("/api/v1/users/" + defaultUser.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes())).header("If-None-Match", etag)).andDo(print()).andExpect(status().isForbidden());

    this.mockMvc.perform(get("/api/v1/users/" + defaultUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").value("user"));
  }

  @Test
  public void testApplyInvalidPatch() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/users/" + inactiveUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"replace\",\"path\": \"/unknownProperty\",\"value\": \"changed\"}]";
    this.mockMvc.perform(patch("/api/v1/users/" + inactiveUser.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-None-Match", etag)).andDo(print()).andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void testDeleteUserWithoutAdminRole() throws Exception{
    this.mockMvc.perform(delete("/api/v1/users/" + inactiveUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(status().isForbidden());
  }

  @Test
  public void testDeleteUserAsAdminRole() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/users/" + inactiveUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");
    this.mockMvc.perform(delete("/api/v1/users/" + inactiveUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-None-Match", etag)).andDo(print()).andExpect(status().isNoContent());
    this.mockMvc.perform(get("/api/v1/users/" + inactiveUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.active").value("false"));
  }

  @Test
  public void testDeleteUserWithoutEtag() throws Exception{
    this.mockMvc.perform(delete("/api/v1/users/" + inactiveUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isPreconditionFailed());
  }

  @Test
  public void testDeleteUserWithWrong() throws Exception{
    this.mockMvc.perform(delete("/api/v1/users/" + inactiveUser.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-None-Match", "fail")).andDo(print()).andExpect(status().isPreconditionFailed());
  }

  @Test
  public void testDeleteInvalidUserAsAdminRole() throws Exception{
    this.mockMvc.perform(delete("/api/v1/users/0").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  public void testDeleteAsAnonymous() throws Exception{
    this.mockMvc.perform(delete("/api/v1/users/" + inactiveUser.getId())).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testRegisterUser() throws Exception{
    RepoUser created = new RepoUser();
    created.setUsername("created");
    created.setPassword("created");
    created.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    created.setEmail("test@mail.org");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(created))).andDo(print()).andExpect(status().isCreated());
    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("created:created".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").value("created"));
  }

  @Test
  public void testRegisterUserTwice() throws Exception{
    RepoUser noduplicates = new RepoUser();
    noduplicates.setUsername("noduplicates");
    noduplicates.setPassword("noduplicates");
    noduplicates.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    noduplicates.setEmail("test@mail.org");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(noduplicates))).andDo(print()).andExpect(status().isCreated());
    this.mockMvc.perform(post("/api/v1/users/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(noduplicates))).andDo(print()).andExpect(status().isConflict());
  }

  @Test
  public void testRegisterUserWithoutUsername() throws Exception{
    RepoUser created = new RepoUser();
    created.setPassword("created");
    created.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    created.setEmail("test@mail.org");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(created))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testRegisterUserWithoutPassword() throws Exception{
    RepoUser created = new RepoUser();
    created.setUsername("created");
    created.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    created.setEmail("test@mail.org");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(created))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testRegisterUserAsAdministrator() throws Exception{
    RepoUser created = new RepoUser();
    created.setUsername("noAdmin");
    created.setPassword("noAdmin");
    created.setRolesAsEnum(Arrays.asList(RepoUserRole.USER, RepoUserRole.ADMINISTRATOR));
    created.setEmail("test@mail.org");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(created))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testRegisterFirstUserAsAdministrator() throws Exception{
    userDao.deleteAll();
    RepoUser created = new RepoUser();
    created.setUsername("admin");
    created.setPassword("admin");
    created.setRolesAsEnum(Arrays.asList(RepoUserRole.USER, RepoUserRole.ADMINISTRATOR));
    created.setEmail("test@mail.org");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(created))).andDo(print()).andExpect(status().isCreated());
    setUp();
  }

  @Test
  public void testRegisterOtherAdministratorAsAdministrator() throws Exception{
    RepoUser created = new RepoUser();
    created.setUsername("admin2");
    created.setPassword("admin2");
    created.setRolesAsEnum(Arrays.asList(RepoUserRole.USER, RepoUserRole.ADMINISTRATOR));
    created.setEmail("test@mail.org");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/users/").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(created))).andDo(print()).andExpect(status().isCreated());
  }
}
