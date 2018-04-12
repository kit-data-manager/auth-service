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

import edu.kit.datamanager.auth.dao.IUserDao;
import edu.kit.datamanager.auth.domain.RepoUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
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
  private BCryptPasswordEncoder passwordEncoder;

  @Value("${secret.key}")
  private String secretKey;

  private RepoUser adminUser;

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
  }

  @Test
  public void testLogin() throws Exception{
    //authenticate and login
    MvcResult result = this.mockMvc.perform(post("/api/v1/login/").header(HttpHeaders.AUTHORIZATION,
            "Basic " + Base64Utils.encodeToString("admin:admin".getBytes()))).andDo(print()).andExpect(status().isOk()).andReturn();
    String jwt = result.getResponse().getContentAsString();

    //check token (validate signature and check username)
    Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
    Assert.assertEquals(adminUser.getUsername(), claimsJws.getBody().get("username", String.class));

    //use token to obtain own user information
    this.mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION,
            "Bearer " + jwt)).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").value("admin"));
  }

}
