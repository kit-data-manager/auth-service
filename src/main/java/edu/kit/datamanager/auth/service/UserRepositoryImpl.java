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
package edu.kit.datamanager.auth.service;

import edu.kit.datamanager.entities.Role;
import edu.kit.datamanager.auth.domain.RepoUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *
 * @author jejkal
 */
public class UserRepositoryImpl{

  BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public Optional<RepoUser> loadUser(String username, String credentials){
    RepoUser user1 = new RepoUser();
    user1.setIdentifier(username);
    user1.setActive(true);
    user1.setEmail("thomas.jejkal@kit.edu");
    user1.setRoles(new ArrayList<>(Arrays.asList(Role.USER.name(), Role.ADMINISTRATOR.name())));
    // "thomas.jejkal@kit.edu", "KIT", Arrays.asList("USER", "ADMIN"));
    // user1.activate(passwordEncoder.encode(credentials));
    Optional<RepoUser> userOptional = Optional.of(user1);
    return userOptional;
    //return userOptional.map(user -> user.isActive() ? user : null);
  }
  
  public RepoUser loadUser(String username){
      RepoUser user1 = new RepoUser();
    user1.setIdentifier(username);
    user1.setActive(true);
    user1.setEmail("thomas.jejkal@kit.edu");
    user1.setRoles(new ArrayList<>(Arrays.asList(Role.USER.name(), Role.ADMINISTRATOR.name())));
    // "thomas.jejkal@kit.edu", "KIT", Arrays.asList("USER", "ADMIN"));
    // user1.activate(passwordEncoder.encode(credentials));
    return user1;
  }
}
