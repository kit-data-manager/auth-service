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

import com.github.fge.jsonpatch.JsonPatch;
import edu.kit.datamanager.auth.dao.IGroupDao;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.domain.RepoUserGroup;
import edu.kit.datamanager.auth.service.IGroupService;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.dao.ByExampleSpecification;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.AccessForbiddenException;
import edu.kit.datamanager.exceptions.BadArgumentException;
import edu.kit.datamanager.exceptions.CustomInternalServerError;
import edu.kit.datamanager.exceptions.PatchApplicationException;
import edu.kit.datamanager.exceptions.ResourceNotFoundException;
import edu.kit.datamanager.exceptions.UpdateForbiddenException;
import edu.kit.datamanager.util.AuthenticationHelper;
import edu.kit.datamanager.util.PatchUtil;
import java.util.Collection;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
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
  @Autowired
  private IUserService userService;

  @Autowired
  private Logger logger;
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
    logger.trace("Performing findAll({}, {}).", example, pgbl);
    if(example != null){
      logger.trace("Example provided, using example spec and calling findAll(spec, pgbl)");
      Specification<RepoUserGroup> spec = Specification.where(new ByExampleSpecification(em).byExample(example));
      return getDao().findAll(spec, pgbl);
    } else{
      logger.trace("No example provided, using no spec and calling findAll(pgbl).");
      return getDao().findAll(pgbl);
    }
  }

  @Override
  public Page<RepoUserGroup> findAll(RepoUserGroup example, Pageable pgbl, boolean callerIsAdmin){
    Page<RepoUserGroup> page;
    if(callerIsAdmin){
      //do find all
      page = findAll(example, pgbl);
    } else{
      //query based on membership
      page = findByMembershipsUserUsernameEqualsAndMembershipsRoleGreaterThanEqualAndActiveTrue((String) AuthenticationHelper.getAuthentication().getPrincipal(), RepoUserGroup.GroupRole.GROUP_MEMBER, pgbl);
    }
    return page;
  }

  @Override
  @Transactional(readOnly = true)
  public RepoUserGroup findById(Long id){
    logger.trace("Performing findById({}).", id);
    Optional<RepoUserGroup> result = getDao().findById(id);

    if(!result.isPresent()){
      logger.error("No user group found for identifier {}. Throwing ResourceNotFoundException.", id);
      throw new ResourceNotFoundException("Group with id " + id + " was not found.");
    }

    return result.get();
  }

  @Override
  public RepoUserGroup create(RepoUserGroup group, String caller){
    logger.trace("Performing create({}, {}).", group, caller);
    group.setId(null);
    logger.trace("Checking group name.");
    if(group.getGroupname() == null){
      logger.error("Group name not provided. Throwing BadArgumentException.");
      throw new BadArgumentException("No groupname assigned to provided user.");
    } else{
      //enforce uppercase groupname
      logger.trace("Enforcing lowercase groupname.");
      group.setGroupname(group.getGroupname());
    }

    logger.trace("Checking membership information for caller with principal {}.", caller);
    RepoUser theUser = (RepoUser) userService.loadUserByUsername(caller);
    if(theUser == null){
      //this should only happen if the caller is no 'real' user, e.g. while creating groups using a service token.
      //@TODO check if this scenario makes sense.
      logger.error("No user found for principal {}, probably this is an attempt to create groups using a service token. Throwing AccessForbiddenException.");
      throw new AccessForbiddenException("Creating group with caller principal '" + caller + "' is not allowed.");
    }
    logger.trace("Calling group.addOrUpdateMembership({}, {}).", "RepoUser#" + theUser.getId(), RepoUserGroup.GroupRole.GROUP_MANAGER);
    group.addOrUpdateMembership(theUser, RepoUserGroup.GroupRole.GROUP_MANAGER);
    logger.trace("Persisting and returning group {}.", group);
    return getDao().save(group);
  }

  @Override
  public RepoUserGroup update(RepoUserGroup entity){
    RepoUserGroup group = getDao().saveAndFlush(entity);
    // javers.commit(AuthenticationHelper.getPrincipal(), group);
    return group;
  }

  @Override
  public void delete(RepoUserGroup group){
    logger.trace("Performing delete({}).", "RepoUserGroup#" + group.getId());
    if(group.getActive()){
      logger.debug("Deactivating group {}.", group.getGroupname());
      group.setActive(Boolean.FALSE);
      logger.trace("Persisting deactivated group.");
      getDao().save(group);
      logger.trace("Resource successfully persisted.");
    } else{
      logger.trace("Group {} is deactivated. Removing group.", group.getGroupname());
      getDao().delete(group);
      logger.trace("Resource successfully removed.");
    }
  }

  @Override
  public Health health(){
    return Health.up().withDetail("Groups", dao.count()).build();
  }

  protected IGroupDao getDao(){
    return dao;
  }

  @Override
  public void patch(RepoUserGroup entity, JsonPatch patch, Collection<? extends GrantedAuthority> userGrants) throws PatchApplicationException, UpdateForbiddenException{
    logger.trace("Performing patch({}, {}, {}).", "RepoUserGroup#" + entity.getId(), patch, userGrants);
    RepoUserGroup updated = PatchUtil.applyPatch(entity, patch, RepoUserGroup.class, userGrants);
    logger.trace("Patch successfully applied. Persisting patched resource.");
    getDao().save(updated);
    logger.trace("Resource successfully persisted.");
  }
}
