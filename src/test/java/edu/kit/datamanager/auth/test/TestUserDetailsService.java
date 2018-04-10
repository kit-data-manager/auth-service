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
import edu.kit.datamanager.auth.service.IUserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author jejkal
 */
//@Service
public class TestUserDetailsService implements IUserService{

  private BCryptPasswordEncoder passwordEncoder;

  private List<RepoUser> userList = new ArrayList<>();

  public TestUserDetailsService(BCryptPasswordEncoder passwordEncoder){
    this.passwordEncoder = passwordEncoder;
    RepoUser admin = new RepoUser();
    admin.setId(1l);
    admin.setUsername("admin");
    admin.setActive(true);
    admin.setLocked(false);
    admin.setPassword(passwordEncoder.encode("admin"));
    admin.setRolesAsEnum(Arrays.asList(RepoUser.UserRole.ADMINISTRATOR));
    admin.setEmail("test@mail.org");
    userList.add(admin);
  }

  @Override
  public UserDetails loadUserByUsername(String string) throws UsernameNotFoundException{
    return IteratorUtils.find(userList.iterator(), (t) -> {
      return t.getUsername().equals(string);
    });
  }

  @Override
  public Page<RepoUser> findAll(RepoUser example, Pageable pgbl){
    return new PageImpl<>(userList.subList(pgbl.getPageNumber() * pgbl.getPageSize(), pgbl.getPageNumber() * pgbl.getPageSize() + pgbl.getPageSize()), pgbl, userList.size());
  }

  @Override
  public Optional<RepoUser> findById(Long id){
    return Optional.of(IteratorUtils.find(userList.iterator(), (t) -> {
      return Long.compare(t.getId(), id) == 0;
    }));
  }

  @Override
  public RepoUser create(RepoUser entity){
    entity.setId((long) userList.size());
    userList.add(entity);
    return entity;
  }

  @Override
  public RepoUser update(RepoUser entity){
    RepoUser user = IteratorUtils.find(userList.iterator(), (t) -> {
      return Long.compare(t.getId(), entity.getId()) == 0;
    });

    userList.remove(user);
    entity.setId(user.getId());
    userList.add(entity);
    return entity;
  }

  @Override
  public void delete(RepoUser entity){
    RepoUser user = IteratorUtils.find(userList.iterator(), (t) -> {
      return Long.compare(t.getId(), entity.getId()) == 0;
    });
    user.setActive(false);
  }

  @Override
  public Health health(){
    return Health.up().withDetail("Users", userList.size()).build();
  }

}
