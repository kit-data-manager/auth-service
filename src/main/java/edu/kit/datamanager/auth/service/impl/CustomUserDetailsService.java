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
import edu.kit.datamanager.auth.dao.IUserDao;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.service.IUserService;
import java.util.Arrays;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jejkal
 */
@Service
@Transactional
public class CustomUserDetailsService implements IUserService{

  @Autowired
  private IUserDao dao;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @PersistenceContext
  private EntityManager em;

  public CustomUserDetailsService(){
    super();
  }

  @Override
  @Transactional(readOnly = true)
  public RepoUser loadUserByUsername(String name){
    RepoUser user = getDao().findByUsername(name);
    if(user == null){
      return null;
    }
    if(!user.isEnabled()){
      user.setRolesAsEnum(Arrays.asList(RepoUser.UserRole.INACTIVE));
    }
    return user;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RepoUser> findById(Long id){
    return getDao().findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<RepoUser> findAll(RepoUser example, Pageable pgbl){
    if(example != null){
      Specification<RepoUser> spec = Specification.where(new ByExampleSpecification(em).byExample(example));
      return getDao().findAll(spec, pgbl);
    } else{
      return getDao().findAll(pgbl);
    }
  }

  @Override
  public RepoUser create(RepoUser entity){
    entity.setPassword(passwordEncoder.encode(entity.getPassword()));
    System.out.println("ID " + entity.getId());
    RepoUser result = getDao().save(entity);
    System.out.println("RESULT " + result);
    return result;
  }

  @Override
  public RepoUser update(RepoUser entity){
    return getDao().save(entity);
  }

  @Override
  public void delete(RepoUser entity){
    getDao().delete(entity);
  }

  protected IUserDao getDao(){
    return dao;
  }

  @Override
  public Health health(){
    return Health.up().withDetail("Users", dao.count()).build();
  }
}
