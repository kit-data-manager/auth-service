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
import java.util.Optional;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author jejkal
 */
public interface IGroupService extends HealthIndicator{

  public Page<RepoUserGroup> findByMembershipsUserUsernameEqualsAndMembershipsRoleGreaterThanEqual(String username, RepoUserGroup.GroupRole role, Pageable pgbl);

  public Page<RepoUserGroup> findAll(RepoUserGroup example, Pageable pgbl);

  Optional<RepoUserGroup> findById(final Long id);

  RepoUserGroup create(final RepoUserGroup entity);

  RepoUserGroup update(final RepoUserGroup entity);

  void delete(final RepoUserGroup entity);
}
