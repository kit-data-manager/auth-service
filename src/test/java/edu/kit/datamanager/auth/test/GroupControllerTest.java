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
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.RemoveOperation;
import edu.kit.datamanager.auth.dao.IGroupDao;
import edu.kit.datamanager.auth.dao.IUserDao;
import edu.kit.datamanager.auth.domain.GroupMembership;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.domain.RepoUserGroup;
import edu.kit.datamanager.entities.RepoUserRole;
import java.util.Arrays;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Assert;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
public class GroupControllerTest{

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private IUserDao userDao;

  @Autowired
  private IGroupDao groupDao;

  private RepoUser adminUser;
  private RepoUser defaultUser;
  private RepoUser otherUser;
  private RepoUser noMemberUser;

  private RepoUserGroup defaultGroup;
  private RepoUserGroup otherGroup;

  @Before
  public void setUp(){
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
    //add user
    RepoUser user = new RepoUser();
    user.setUsername("user");
    user.setActive(true);
    user.setLocked(false);
    user.setPassword(passwordEncoder.encode("user"));
    user.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    user.setEmail("test@mail.org");
    defaultUser = userDao.saveAndFlush(user);
    //add other user
    RepoUser other = new RepoUser();
    other.setUsername("other");
    other.setActive(true);
    other.setLocked(false);
    other.setPassword(passwordEncoder.encode("other"));
    other.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    other.setEmail("test@mail.org");
    otherUser = userDao.saveAndFlush(other);

    //add other user
    RepoUser nomember = new RepoUser();
    nomember.setUsername("nomember");
    nomember.setActive(true);
    nomember.setLocked(false);
    nomember.setPassword(passwordEncoder.encode("nomember"));
    nomember.setRolesAsEnum(Arrays.asList(RepoUserRole.USER));
    nomember.setEmail("test@mail.org");
    noMemberUser = userDao.saveAndFlush(nomember);

    RepoUserGroup group = new RepoUserGroup();
    group.setActive(Boolean.TRUE);
    group.setGroupId("Default Group".toUpperCase());
    group.setGroupname("Default Group");
    group.addOrUpdateMembership(adminUser, RepoUserGroup.GroupRole.GROUP_MANAGER);
    group.addOrUpdateMembership(defaultUser, RepoUserGroup.GroupRole.GROUP_MEMBER);
    defaultGroup = groupDao.saveAndFlush(group);

    RepoUserGroup group2 = new RepoUserGroup();
    group2.setActive(Boolean.TRUE);
    group2.setGroupId("Other Group".toUpperCase());
    group2.setGroupname("Other Group");
    group2.addOrUpdateMembership(defaultUser, RepoUserGroup.GroupRole.GROUP_MANAGER);
    group2.addOrUpdateMembership(other, RepoUserGroup.GroupRole.GROUP_MEMBER);
    otherGroup = groupDao.saveAndFlush(group2);
  }

  @Test
  public void testGetUserGroupListAsAdmin() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/").param("page", "0").param("size", "10").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].groupId").value("Default Group".toUpperCase()));
  }

  @Test
  public void testGetUserGroupListByExampleAsAdmin() throws Exception{
    RepoUserGroup example = new RepoUserGroup();
    example.setGroupId("DEFAULT GROUP");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/groups/search").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(example)).param("page", "0").param("size", "10").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].groupId").value("Default Group".toUpperCase())).andExpect(MockMvcResultMatchers.jsonPath("$[1]").doesNotExist());

    example = new RepoUserGroup();
    example.setGroupname("Default Group");
    this.mockMvc.perform(post("/api/v1/groups/search").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(example)).param("page", "0").param("size", "10").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].groupId").value("Default Group".toUpperCase())).andExpect(MockMvcResultMatchers.jsonPath("$[1]").doesNotExist());

  }

  @Test
  public void testGetUserGroupListByExampleWithPatternAsAdmin() throws Exception{
    RepoUserGroup example = new RepoUserGroup();
    example.setGroupId("%DEFAULT%");
    ObjectMapper mapper = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/groups/search").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(example)).param("page", "0").param("size", "10").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].groupId").value("Default Group".toUpperCase())).andExpect(MockMvcResultMatchers.jsonPath("$[1]").doesNotExist());

    example = new RepoUserGroup();
    example.setGroupname("%Default%");
    this.mockMvc.perform(post("/api/v1/groups/search").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(example)).param("page", "0").param("size", "10").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].groupId").value("Default Group".toUpperCase())).andExpect(MockMvcResultMatchers.jsonPath("$[1]").doesNotExist());

  }

  @Test
  public void testGetUserGroupListWithExceededPageSize() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/").param("page", "0").param("size", "1000").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].groupId").value("Default Group".toUpperCase()));
  }

  @Test
  public void testGetUserGroupListWithExceededPageNumber() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/").param("page", "100").param("size", "100").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotExist());
  }

  @Test
  public void getUserGroupListAsMember() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(MockMvcResultMatchers.jsonPath("$[0].groupId").value("Default Group".toUpperCase()));
  }

  @Test
  public void getUserGroupListAsNoMember() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("nomember:nomember".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotExist());
  }

  @Test
  public void getUserGroupListAnonymous() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/")).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void getUserGroupById() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/" + defaultGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.groupId").value("Default Group".toUpperCase()));
  }

  @Test
  public void getInvalidUserGroupById() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/0").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void getUserGroupByIdAsNoMember() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/" + defaultGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("other:other".getBytes()))).andDo(print()).andExpect(status().isForbidden());
  }

  @Test
  public void getUserGroupByIdAnonymous() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/" + defaultGroup.getId())).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void getUserGroupByIdAsNoMemberButAdmin() throws Exception{
    this.mockMvc.perform(get("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.groupId").value("Other Group".toUpperCase()));
  }

  @Test
  public void testPatchAsAnonymous() throws Exception{
    String patch = "[{\"op\": \"replace\",\"path\": \"/identifier\",\"value\": \"test1\"}]";
    this.mockMvc.perform(patch("/api/v1/groups/" + otherGroup.getId()).contentType("application/json-patch+json").content(patch)).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  public void testPatchOfUnknownGroup() throws Exception{
    String patch = "[{\"op\": \"replace\",\"path\": \"/identifier\",\"value\": \"test1\"}]";
    this.mockMvc.perform(patch("/api/v1/groups/0").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).contentType("application/json-patch+json").content(patch)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testPatchAsMember() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("other:other".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"replace\",\"path\": \"/groupname\",\"value\": \"test1\"}]";
    this.mockMvc.perform(patch("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("other:other".getBytes())).header("If-Match", etag).contentType("application/json-patch+json").content(patch)).andDo(print()).andExpect(status().isForbidden());
  }

  @Test
  public void testPatchAsManager() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"replace\",\"path\": \"/groupname\",\"value\": \"test1\"}]";
    this.mockMvc.perform(patch("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes())).header("If-Match", etag).contentType("application/json-patch+json").content(patch)).andDo(print()).andExpect(status().isNoContent());

    this.mockMvc.perform(get("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.groupname").value("test1")).andExpect(header().exists("ETag"));

  }

  @Test
  public void testPatchForbiddenField() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"replace\",\"path\": \"/id\",\"value\": 4}]";
    this.mockMvc.perform(patch("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-Match", etag).contentType("application/json-patch+json").content(patch)).andDo(print()).andExpect(status().isForbidden());
  }

  @Test
  public void testPatchGroupnameAsAdmin() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"replace\",\"path\": \"/groupname\",\"value\": \"changedName\"}]";
    this.mockMvc.perform(patch("/api/v1/groups/" + otherGroup.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-Match", etag)).andExpect(status().isNoContent());

    this.mockMvc.perform(get("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.groupname").value("changedName")).andExpect(header().exists("ETag"));
  }

  @Test
  public void testApplyInvalidPatchAsAdmin() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"replace\",\"path\": \"/unknownProperty\",\"value\": \"changed\"}]";
    this.mockMvc.perform(patch("/api/v1/groups/" + otherGroup.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-Match", etag)).andExpect(status().isUnprocessableEntity());
  }
  
    @Test
  public void testChangeGroupId() throws Exception{
    String etag = this.mockMvc.perform(get("/api/v1/groups/" + otherGroup.getId()).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"replace\",\"path\": \"/groupId\",\"value\": \"changed\"}]";
    this.mockMvc.perform(patch("/api/v1/groups/" + otherGroup.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-Match", etag)).andExpect(status().isForbidden());
  }
  

  @Test
  public void testPatchGroupWithWrongETag() throws Exception{
    String patch = "[{\"op\": \"replace\",\"path\": \"/groupname\",\"value\": \"changed\"}]";
    this.mockMvc.perform(patch("/api/v1/groups/" + otherGroup.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-Match", "\"invalid\"")).andExpect(status().isPreconditionFailed());
  }

  @Test
  public void testCreateGroupAnonymous() throws Exception{
    RepoUserGroup created = new RepoUserGroup();
    created.setGroupname("created");
    ObjectMapper mapper = new ObjectMapper();

    this.mockMvc.perform(post("/api/v1/groups/").contentType("application/json").content(mapper.writeValueAsString(created))).andExpect(status().isUnauthorized());
  }

  @Test
  public void testCreateGroup() throws Exception{
    RepoUserGroup created = new RepoUserGroup();
    created.setGroupId("Created");
    created.setGroupname("created");
    ObjectMapper mapper = new ObjectMapper();

    String location = this.mockMvc.perform(post("/api/v1/groups/").contentType("application/json-patch+json").content(mapper.writeValueAsString(created)).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");

    Assert.assertNotNull(location);

    long groupId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

    this.mockMvc.perform(get("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.groupname").value("created"));
  }

  @Test
  public void testCreateGroupWithIdentifier() throws Exception{
    RepoUserGroup created = new RepoUserGroup();
    ObjectMapper mapper = new ObjectMapper();

    this.mockMvc.perform(post("/api/v1/groups/").contentType("application/json-patch+json").content(mapper.writeValueAsString(created)).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateGroupAndAddAndRemoveUser() throws Exception{
    RepoUserGroup created = new RepoUserGroup();
    created.setGroupId("newGroup");
    created.setGroupname("newGroup");
    ObjectMapper mapper = new ObjectMapper();

    String location = this.mockMvc.perform(post("/api/v1/groups/").contentType("application/json-patch+json").content(mapper.writeValueAsString(created)).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");

    Assert.assertNotNull(location);

    long groupId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

    String etag = this.mockMvc.perform(get("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.groupname").value("newGroup")).andExpect(MockMvcResultMatchers.jsonPath("$.groupId").value("newGroup".toUpperCase())).andReturn().getResponse().getHeader("ETag");

    //Create new memberships entity
    GroupMembership newMembership = new GroupMembership(otherUser, RepoUserGroup.GroupRole.GROUP_MEMBER);
    //build patch operation
    JsonPatchOperation op = new AddOperation(JsonPointer.of("memberships", "0"), mapper.readTree(mapper.writeValueAsString(newMembership)));
    JsonPatch patch_add = new JsonPatch(Arrays.asList(op));

    //call patch
    this.mockMvc.perform(patch("/api/v1/groups/" + groupId).contentType("application/json-patch+json").content(mapper.writeValueAsString(patch_add)).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-Match", etag)).andExpect(status().isNoContent());

    //check for added membership
    this.mockMvc.perform(get("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.memberships", Matchers.hasSize(2)));
    //update etag
    etag = this.mockMvc.perform(get("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    //try patch
    op = new RemoveOperation(JsonPointer.of("memberships", "0"));
    JsonPatch patch_remove = new JsonPatch(Arrays.asList(op));

    //call patch
    this.mockMvc.perform(patch("/api/v1/groups/" + Long.toString(groupId)).contentType("application/json-patch+json").content(mapper.writeValueAsString(patch_remove)).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-Match", etag)).andExpect(status().isNoContent());

    //check for added membership
    this.mockMvc.perform(get("/api/v1/groups/" + Long.toString(groupId)).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.memberships", Matchers.hasSize(1)));
  }

  @Test
  public void testDeleteGroupAnonymous() throws Exception{
    this.mockMvc.perform(delete("/api/v1/groups/" + defaultGroup.getId())).andExpect(status().isUnauthorized());
  }

  @Test
  public void testDeleteGroupAsManager() throws Exception{
    RepoUserGroup created = new RepoUserGroup();
    String idName = "toDelete " + UUID.randomUUID().toString();
    created.setGroupId(idName);
    created.setGroupname(idName);
    ObjectMapper mapper = new ObjectMapper();

    String location = this.mockMvc.perform(post("/api/v1/groups/").contentType("application/json-patch+json").content(mapper.writeValueAsString(created)).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");

    Assert.assertNotNull(location);

    long groupId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

    String etag = this.mockMvc.perform(get("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    this.mockMvc.perform(delete("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes())).header("If-Match", etag)).andExpect(status().isNoContent());

    this.mockMvc.perform(get("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andExpect(status().isNotFound());

    //now, delete physically as admin
    etag = this.mockMvc.perform(get("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    this.mockMvc.perform(delete("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes())).header("If-Match", etag)).andExpect(status().isNoContent());

    this.mockMvc.perform(get("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andExpect(status().isNotFound());
  }

  @Test
  public void testDeleteGroupAsManagerWithoutEtag() throws Exception{
    RepoUserGroup created = new RepoUserGroup();
    String idName = "toDelete " + UUID.randomUUID().toString();
    created.setGroupId(idName);
    created.setGroupname(idName);
    ObjectMapper mapper = new ObjectMapper();

    String location = this.mockMvc.perform(post("/api/v1/groups/").contentType("application/json-patch+json").content(mapper.writeValueAsString(created)).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");

    Assert.assertNotNull(location);

    long groupId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

    this.mockMvc.perform(delete("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andExpect(status().isPreconditionRequired());
  }

  @Test
  public void testDeleteGroupAsNoMember() throws Exception{
    RepoUserGroup created = new RepoUserGroup();
    String idName = "toDelete " + UUID.randomUUID().toString();
    created.setGroupId(idName);
    created.setGroupname(idName);
    created.addOrUpdateMembership(otherUser, RepoUserGroup.GroupRole.GROUP_MEMBER);
    ObjectMapper mapper = new ObjectMapper();

    String location = this.mockMvc.perform(post("/api/v1/groups/").contentType("application/json-patch+json").content(mapper.writeValueAsString(created)).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");

    Assert.assertNotNull(location);

    long groupId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

    String etag = this.mockMvc.perform(get("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("other:other".getBytes()))).andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");

    this.mockMvc.perform(delete("/api/v1/groups/" + groupId).header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("other:other".getBytes())).header("If-Match", etag)).andExpect(status().isForbidden());
  }

  @Test
  public void testDeleteInvalidGroup() throws Exception{
    this.mockMvc.perform(delete("/api/v1/groups/0").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("user:user".getBytes()))).andExpect(status().isNoContent());
  }

}
