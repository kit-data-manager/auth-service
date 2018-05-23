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

import edu.kit.datamanager.auth.dao.IGroupDao;
import edu.kit.datamanager.auth.domain.RepoUserGroup;
import edu.kit.datamanager.auth.service.IGroupService;
import edu.kit.datamanager.dao.ByExampleSpecification;
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
  // private final Javers javers;

  public RepoUserGroupService(){//Javers javers){
    super();
    // this.javers = javers;
  }

  @Override
  public Page<RepoUserGroup> findByMembershipsUserUsernameEqualsAndMembershipsRoleGreaterThanEqualAndActiveTrue(String username, RepoUserGroup.GroupRole role, Pageable pgbl){
    return getDao().findByMembershipsUserUsernameEqualsAndMembershipsRoleGreaterThanEqualAndActiveTrue(username, role, pgbl);
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
    //QueryBuilder jqlQuery = QueryBuilder.byClass(RepoUserGroup.class).byInstanceId(id, RepoUserGroup.class).withScopeDeepPlus().withVersion(5);
//
    //List<Shadow<RepoUserGroup>> shadows = javers.findShadows(jqlQuery.build());
//    System.out.println(" SIZE " + shadows.size());
//    shadows.forEach((shadow) -> {
//      System.out.println("Shadow: " + shadow.getCommitMetadata() + ": " + shadow.get());
//      System.out.println("Members: " + shadow.get().getMemberships().size());
//    });
//    return Optional.of(shadows.get(0).get());
    return getDao().findById(id);
  }

  @Override
  public RepoUserGroup create(RepoUserGroup entity){
    RepoUserGroup group = getDao().saveAndFlush(entity);
    // javers.commit(AuthenticationHelper.getPrincipal(), group);
    return group;
  }

  @Override
  public RepoUserGroup update(RepoUserGroup entity){
    RepoUserGroup group = getDao().saveAndFlush(entity);
    // javers.commit(AuthenticationHelper.getPrincipal(), group);
    return group;
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
