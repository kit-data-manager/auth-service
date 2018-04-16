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
package edu.kit.datamanager.auth.service.impl;

import edu.kit.datamanager.auth.dao.ByExampleSpecification;
import edu.kit.datamanager.auth.dao.IGroupDao;
import edu.kit.datamanager.auth.domain.RepoUserGroup;
import edu.kit.datamanager.auth.service.IGroupService;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jejkal
 */
@Service
@Transactional
public class RepoUserGroupService implements IGroupService{

  @Autowired
  private IGroupDao dao;

  @PersistenceContext
  private EntityManager em;

  public RepoUserGroupService(){
    super();
  }

  @Override
  public Page<RepoUserGroup> findByMembershipsUserUsernameEqualsAndMembershipsRoleGreaterThanEqual(String username, RepoUserGroup.GroupRole role, Pageable pgbl){
    return getDao().findByMembershipsUserUsernameEqualsAndMembershipsRoleGreaterThanEqual(username, role, pgbl);
  }

  @Override
  public Page<RepoUserGroup> findAll(RepoUserGroup example, Pageable pgbl){
    if(example != null){
      Specification<RepoUserGroup> spec = Specification.where(new ByExampleSpecification(em).byExample(example));
      return getDao().findAll(spec, pgbl);
    } else{
      return getDao().findAll(pgbl);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RepoUserGroup> findById(Long id){
    return getDao().findById(id);
  }

  @Override
  public RepoUserGroup create(RepoUserGroup entity){
    return getDao().save(entity);
  }

  @Override
  public RepoUserGroup update(RepoUserGroup entity){
    return getDao().save(entity);
  }

  @Override
  public void delete(RepoUserGroup entity){
    getDao().delete(entity);
  }

  @Override
  public Health health(){
    return Health.up().withDetail("Groups", dao.count()).build();
  }

  protected IGroupDao getDao(){
    return dao;
  }
}
