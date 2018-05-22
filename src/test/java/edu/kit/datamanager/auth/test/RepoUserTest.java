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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.auth.Application;
import edu.kit.datamanager.auth.dao.IGroupDao;
import edu.kit.datamanager.auth.dao.IUserDao;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.service.exceptions.CustomInternalServerError;
import edu.kit.datamanager.util.AuthenticationHelper;
import java.io.IOException;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;

/**
 *
 * @author jejkal
 */
public class RepoUserTest{

  private RepoUser user;

  @Test
  public void testRoleConversion(){
    user = RepoUser.createUser();
    user.setUsername("test");
    user.setRoles("[\"ROLE_USER\", \"ROLE_GUEST\"]");

    //do testing
    user.convertRolesToEnum();
    Assert.assertTrue(user.getRolesAsEnum().contains(RepoUserRole.USER) && user.getRolesAsEnum().contains(RepoUserRole.GUEST));

    user.setRoles("");
    user.convertEnumToRoles();
    Assert.assertTrue(user.getRoles().contains("ROLE_USER") && user.getRoles().contains("ROLE_GUEST"));

    Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

    authorities.stream().filter((a) -> (!a.getAuthority().equals("ROLE_USER") && !a.getAuthority().equals("ROLE_GUEST"))).forEachOrdered((a) -> {
      throw new IllegalStateException("Invalid authority " + a + " found in role list " + user.getRoles() + ". Expected only ROLE_USER and ROLE_GUEST.");
    });
  }

  @Test(expected = CustomInternalServerError.class)
  public void testInvalidRole(){
    user = RepoUser.createUser();
    user.setUsername("test");
    user.setRoles("[\"ROLE_SOME_INVALID_ROLE\", \"ROLE_GUEST\"]");
    user.convertRolesToEnum();
  }
}
