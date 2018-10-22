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
package edu.kit.datamanager.auth.dao;

import edu.kit.datamanager.auth.domain.RepoUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author jejkal
 */
public interface IUserDao extends JpaRepository<RepoUser, Long>, JpaSpecificationExecutor<RepoUser>{

  /**
   * Find a user by username.
   *
   * @param username The username.
   *
   * @return An optional of type RepoUser.
   */
  public Optional<RepoUser> findByUsername(String username);

}
