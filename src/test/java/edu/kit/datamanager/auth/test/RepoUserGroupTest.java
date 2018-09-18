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

import edu.kit.datamanager.auth.domain.GroupMembership;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.domain.RepoUserGroup;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jejkal
 */
public class RepoUserGroupTest{

  private static final RepoUser USER = RepoUser.createUser();

  @BeforeClass
  public static void setup(){
    Date expire = new Date();
    USER.setId(1l);
    USER.setUsername("test");
    USER.setEmail("test@mail.org");
    USER.setFirstname("tester");
    USER.setLastname("user");
    USER.setOrcid("0000-1111-1111-111X");
    USER.setLocked(Boolean.FALSE);
    USER.setLoginFailures(1);
    USER.setLockedUntil(DateUtils.addDays(expire, 1));
    USER.setPassword("test");
    USER.setActive(Boolean.TRUE);
    USER.setActiveGroup("USERS");
    USER.setRoles("[\"ROLE_GUEST\"]");
    USER.convertRolesToEnum();
  }

  @Test
  public void testMembershipCreation(){
    RepoUserGroup group = new RepoUserGroup();
    group.setActive(Boolean.TRUE);
    group.setGroupname("TEST_GROUP");
    group.setId(1l);

    group.addOrUpdateMembership(USER, RepoUserGroup.GroupRole.GROUP_MEMBER);
    Assert.assertEquals(1, group.getMemberships().size());
    Assert.assertEquals(USER, ((GroupMembership) group.getMemberships().toArray()[0]).getUser());
    Assert.assertEquals(RepoUserGroup.GroupRole.GROUP_MEMBER, ((GroupMembership) group.getMemberships().toArray()[0]).getRole());

    group.addOrUpdateMembership(USER, RepoUserGroup.GroupRole.GROUP_MANAGER);
    Assert.assertEquals(RepoUserGroup.GroupRole.GROUP_MANAGER, ((GroupMembership) group.getMemberships().toArray()[0]).getRole());
  }

  @Test
  public void testEqualsAndHashCode(){
    RepoUserGroup group1 = new RepoUserGroup();
    group1.setActive(Boolean.TRUE);
    group1.setGroupname("TEST_GROUP");
    group1.setId(1l);
    group1.addOrUpdateMembership(USER, RepoUserGroup.GroupRole.GROUP_MEMBER);

    RepoUserGroup group2 = new RepoUserGroup();
    group2.setActive(Boolean.TRUE);
    group2.setGroupname("TEST_GROUP");
    group2.setId(1l);
    group2.addOrUpdateMembership(USER, RepoUserGroup.GroupRole.GROUP_MEMBER);

    Assert.assertEquals(group1, group2);
    Assert.assertEquals(group1.hashCode(), group2.hashCode());

  }

  @Test
  public void testGroupRole(){
    Assert.assertEquals(RepoUserGroup.GroupRole.GROUP_MANAGER, RepoUserGroup.GroupRole.fromValue("ROLE_GROUP_MANAGER"));
    Assert.assertEquals(RepoUserGroup.GroupRole.GROUP_MEMBER, RepoUserGroup.GroupRole.fromValue("ROLE_GROUP_MEMBER"));
    Assert.assertEquals(RepoUserGroup.GroupRole.NO_MEMBER, RepoUserGroup.GroupRole.fromValue("ROLE_NO_MEMBER"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidGroupRole(){
    RepoUserGroup.GroupRole.fromValue("ROLE_NOT_EXIST");
  }

}
