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

import edu.kit.datamanager.auth.domain.RepoUserGroup;
import edu.kit.datamanager.service.IGenericService;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author jejkal
 */
public interface IGroupService extends IGenericService<RepoUserGroup>, HealthIndicator{

  public Page<RepoUserGroup> findByMembershipsUserUsernameEqualsAndMembershipsRoleGreaterThanEqualAndActiveTrue(String username, RepoUserGroup.GroupRole role, Pageable pgbl);

  /**
   * Create a new user group using the provided template. This template should contain
   * at least the group name. In addition, a list of members can be
   * provided. 
   *
   * Furthermore, the implementation must deal with duplicate usernames in an
   * appropriate way, e.g. by throwing an according runtime exception mapped to
   * a response code HTTP_CONFLICT.
   *
   * @param entity The user to create.
   *
   * @return The new resource with an id assigned.
   *
   * @throws BadArgumentException if a mandatory field is missing or has an
   * invalid value.
   */
  RepoUserGroup create(final RepoUserGroup entity, String caller);

  Page<RepoUserGroup> findAll(RepoUserGroup example, Pageable pgbl, boolean callerIsAdmin);
          
  RepoUserGroup update(final RepoUserGroup entity);

}
