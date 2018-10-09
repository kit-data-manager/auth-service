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

import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.CustomInternalServerError;
import java.util.Collection;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

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

  @Test
  public void testAddRole(){
    user = RepoUser.createUser();
    user.setUsername("test");
    user.setRoles("[\"ROLE_GUEST\"]");
    user.convertRolesToEnum();
    user.addRole(RepoUserRole.ADMINISTRATOR);
    Assert.assertEquals(2, user.getRolesAsEnum().size());
  }

  @Test
  public void testEqualsAndHashCode(){
    Date expire = new Date();
    RepoUser user1 = RepoUser.createUser();
    user1.setId(1l);
    user1.setUsername("test");
    user1.setEmail("test@mail.org");
    user1.setFirstname("tester");
    user1.setLastname("user");
    user1.setOrcid("0000-1111-1111-111X");
    user1.setLocked(Boolean.FALSE);
    user1.setLoginFailures(1);
    user1.setLockedUntil(DateUtils.addDays(expire, 1));
    user1.setPassword("test");
    user1.setActive(Boolean.TRUE);
    user1.setActiveGroup("USERS");
    user1.setRoles("[\"ROLE_GUEST\"]");
    user1.convertRolesToEnum();

    RepoUser user2 = RepoUser.createUser();
    user2.setId(1l);
    user2.setUsername("test");
    user2.setEmail("test@mail.org");
    user2.setFirstname("tester");
    user2.setLastname("user");
    user2.setOrcid("0000-1111-1111-111X");
    user2.setLocked(Boolean.FALSE);
    user2.setLoginFailures(1);
    user2.setLockedUntil(DateUtils.addDays(expire, 1));
    user2.setPassword("test");
    user2.setActive(Boolean.TRUE);
    user2.setActiveGroup("USERS");
    user2.setRoles("[\"ROLE_GUEST\"]");
    user2.convertRolesToEnum();

    Assert.assertEquals(user1, user2);
    Assert.assertEquals(user1.hashCode(), user2.hashCode());

  }

}
