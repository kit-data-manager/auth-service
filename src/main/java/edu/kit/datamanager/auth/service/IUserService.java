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

import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.exceptions.BadArgumentException;
import edu.kit.datamanager.service.IGenericService;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 *
 * @author jejkal
 */
public interface IUserService extends IGenericService<RepoUser>, HealthIndicator, UserDetailsService{

  /**
   * Create a new user using the provided template. This template should contain
   * at least username and password. In addition, a list of roles can be
   * provided. Implementations should decide, which roles can be provided by the
   * caller. Typically, privileged roles, e.g. ROLE_ADMINISTRATOR, should not be
   * allowed to be assigned during self-registration.
   *
   * Furthermore, the implementation must deal with duplicate usernames in an
   * appropriate way, e.g. by throwing an according runtime exception mapped to
   * a response code HTTP_CONFLICT.
   *
   * @param entity The user to create.
   * @param callerIsAdmin TRUE if the caller is authenticated as ADMINISTRATOR,
   * FALSE otherwise.
   *
   * @return The new resource with an id assigned.
   *
   * @throws BadArgumentException if a mandatory field is missing or has an
   * invalid value.
   */
  RepoUser create(final RepoUser entity, boolean callerIsAdmin) throws BadArgumentException;

  /**
   * Update the provided user in the data backend. This method can be used to
   * perform a transparent update operation in the underlying database. It may
   * or may not perform additional checks to the provided resource and should
   * therefore be used internally only.
   *
   * @param user The user resource to update.
   */
  void update(RepoUser user);

}
