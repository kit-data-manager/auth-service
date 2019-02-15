/*
 * Copyright 2019 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.auth.test.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kit.datamanager.auth.Application;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.domain.RepoUserGroup;
import edu.kit.datamanager.entities.RepoUserRole;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.JUnitRestDocumentation;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author jejkal
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("doc")
public class AuthServiceDocumentationTest{

  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private FilterChainProxy springSecurityFilterChain;
//  @Autowired
//  private IDataResourceDao dataResourceDao;
//  @Autowired
//  private IDataResourceService dataResourceService;
  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

  @Before
  public void setUp() throws JsonProcessingException{
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .addFilters(springSecurityFilterChain)
            .apply(documentationConfiguration(this.restDocumentation))
            .build();
  }

  @Test
  public void documentBasicAccess() throws Exception{
    RepoUser user = RepoUser.createUser();
    user.setFirstname("John");
    user.setLastname("Doe");
    user.setEmail("john.doe@example.com");
    user.setUsername("jdoe");
    user.setPassword("john123");
    user.setLoginFailures(null);
    user.setRolesAsEnum(null);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    String userDocument = this.mockMvc.perform(post("/api/v1/users/").contentType("application/json").content(mapper.writeValueAsString(user))).
            andExpect(status().isCreated()).
            andDo(document("create-user", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn().getResponse().getContentAsString();

    Assert.assertNotNull(userDocument);

    user = mapper.readValue(userDocument, RepoUser.class);
    Assert.assertNotNull(user);

    String jwt = this.mockMvc.perform(post("/api/v1/login/").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("jdoe:john123".getBytes()))).
            andExpect(status().isOk()).
            andDo(document("login", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn().getResponse().getContentAsString();

    String etag = this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt)).
            andExpect(status().isOk()).
            andDo(document("get-me", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn().getResponse().getHeader("ETag");

    String patch = "[{\"op\": \"add\",\"path\": \"/orcid\",\"value\": \"0000-0003-2804-688X\"}]";

    this.mockMvc.perform(patch("/api/v1/users/" + user.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt).header("If-Match", etag)).
            andExpect(status().isNoContent()).
            andDo(document("patch-me", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn();

    RepoUserGroup group = new RepoUserGroup();
    group.setGroupId("my_team");
    group.setGroupname("My Research Team");
    group.setActive(null);
    group.setMemberships(null);

    String groupDocument = this.mockMvc.perform(post("/api/v1/groups/").contentType("application/json").content(mapper.writeValueAsString(group)).header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt)).
            andExpect(status().isCreated()).
            andDo(document("create-group", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn().getResponse().getContentAsString();

    Assert.assertNotNull(groupDocument);

    group = mapper.readValue(groupDocument, RepoUserGroup.class);
    Assert.assertNotNull(group);

    etag = this.mockMvc.perform(get("/api/v1/groups/" + group.getId()).contentType("application/json").header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt)).
            andExpect(status().isOk()).
            andDo(document("get-group", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn().getResponse().getHeader("ETag");

    RepoUser user2 = RepoUser.createUser();
    user2.setFirstname("Jane");
    user2.setLastname("Doe");
    user2.setEmail("jane.doe@example.com");
    user2.setUsername("janedoe");
    user2.setPassword("jane123");
    user2.setLoginFailures(null);
    user2.setRolesAsEnum(null);

    String user2Document = this.mockMvc.perform(post("/api/v1/users/").contentType("application/json").content(mapper.writeValueAsString(user2))).
            andExpect(status().isCreated()).
            andDo(document("create-user2", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn().getResponse().getContentAsString();

    Assert.assertNotNull(user2Document);

    user2 = mapper.readValue(user2Document, RepoUser.class);
    Assert.assertNotNull(user2);

    patch = "[{\"op\": \"add\",\"path\": \"/memberships/1\",\"value\": {\"user\":{\"id\":" + user2.getId() + "}, \"role\":\"GROUP_MEMBER\"}}]";
    this.mockMvc.perform(patch("/api/v1/groups/" + group.getId()).contentType("application/json-patch+json").content(patch).header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt).header("If-Match", etag)).
            andExpect(status().isNoContent()).
            andDo(document("patch-group", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn();

    etag = this.mockMvc.perform(get("/api/v1/groups/" + group.getId()).contentType("application/json").header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt)).
            andExpect(status().isOk()).
            andDo(document("get-patched-group", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn().getResponse().getHeader("ETag");

    this.mockMvc.perform(delete("/api/v1/groups/" + group.getId()).contentType("application/json").header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt).header("If-Match", etag)).
            andExpect(status().isNoContent()).
            andDo(document("delete-group", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn();

    this.mockMvc.perform(get("/api/v1/groups/" + group.getId()).contentType("application/json").header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt)).
            andExpect(status().isOk()).
            andDo(document("get-deleted-group", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn();
  }

}
