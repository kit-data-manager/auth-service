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
import edu.kit.datamanager.dao.ByExampleSpecification;
import edu.kit.datamanager.auth.dao.IUserDao;
import edu.kit.datamanager.auth.domain.RepoUser;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.BadArgumentException;
import edu.kit.datamanager.exceptions.FeatureNotImplementedException;
import edu.kit.datamanager.exceptions.ResourceNotFoundException;
import edu.kit.datamanager.exceptions.UpdateForbiddenException;
import edu.kit.datamanager.util.ControllerUtils;
import edu.kit.datamanager.util.PatchUtil;
import java.util.Arrays;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jejkal
 */
@Service
@Transactional
public class RepoUserService implements IUserService{

  @Autowired
  private IUserDao dao;
  @Autowired
  private Logger logger;
  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @PersistenceContext
  private EntityManager em;

  public RepoUserService(){
    super();
  }

  @Override
  public RepoUser create(RepoUser user, boolean callerIsAdmin){
    logger.trace("Performing create({}, {}).", user, callerIsAdmin);
    user.setId(null);

    if(user.getUsername() == null){
      logger.error("No username provided. Throwing BadArgumentException.");
      throw new BadArgumentException("No username assigned to provided user.");
    }

    if("SELF".equals(user.getUsername())){
      logger.error("Invalid username 'SELF' provided. Throwing BadArgumentException.");
      throw new BadArgumentException("Username 'SELF' is not allowed.");
    }

    if(user.getPassword() == null){
      logger.error("No password provided. Throwing BadArgumentException.");
      throw new BadArgumentException("No password assigned to provided user.");
    }

    //enforce lowercase username
    logger.trace("Enforcing lowercase username.");
    user.setUsername(user.getUsername());

    if(dao.count() == 0){
      logger.info("First user detected. Adding role {}.", RepoUserRole.ADMINISTRATOR);
      //first user, add ADMINISTRATOR role
      user.addRole(RepoUserRole.ADMINISTRATOR);
    } else{
      if(!callerIsAdmin){
        logger.trace("Checking for self-registering with privileged roles.");
        //if user contains ADMINISTRATOR role check for admin access
        user.getRolesAsEnum().stream().filter((role) -> (role.ordinal() >= RepoUserRole.ADMINISTRATOR.ordinal() && !callerIsAdmin)).forEachOrdered((_item) -> {
          logger.error("Creating users with privileged roles is only permitted for users with ROLE_ADMINISTRATOR. Throwing BadArgumentException.");
          throw new BadArgumentException("Self-registration with privileged roles not allowed.");
        });
      } else{
        logger.trace("Skipping role check due to admin call.");
      }
    }

    if(user.getRolesAsEnum().isEmpty()){
      logger.trace("No role assigned to user. Adding default role {}.", RepoUserRole.USER);
      user.addRole(RepoUserRole.USER);
    }

    user.setLoginFailures(0);

    //encode password
    logger.trace("Encoding user-provided password before persisting user to database.");
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    logger.trace("Activating user.");
    user.setActive(Boolean.TRUE);
    user.setLocked(Boolean.FALSE);
    logger.trace("Persisting user to database.");
    return getDao().save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public RepoUser loadUserByUsername(String name){
    logger.trace("Performing loadUserByUsername({}).", name);
    Optional<RepoUser> user = getDao().findByUsername(name);
    if(user.isPresent()){
      logger.trace("User is present. Performing check for status.");
      RepoUser result = user.get();
      if(!result.isEnabled()){
        logger.trace("User is inactive. Replacing roles by {}.", RepoUserRole.INACTIVE);
        result.setRolesAsEnum(Arrays.asList(RepoUserRole.INACTIVE));
      }
      return result;
    }

    return null;
  }

  @Override
  @Transactional(readOnly = true)
  public RepoUser findById(String id){
    logger.trace("Performing findById({}).", id);
    long lId = ControllerUtils.parseIdToLong(id);

    Optional<RepoUser> result = getDao().findById(lId);

    if(!result.isPresent()){
      logger.error("No user found for identifier {}. Throwing ResourceNotFoundException.", id);
      throw new ResourceNotFoundException("User with id " + id + " was not found.");
    }

    return result.get();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<RepoUser> findAll(RepoUser example, Pageable pgbl){
    logger.trace("Performing findAll({}, {}).", example, pgbl);
    if(example != null){
      logger.trace("Example provided, using example spec and calling findAll(spec, pgbl)");
      Specification<RepoUser> spec = Specification.where(new ByExampleSpecification(em).byExample(example));
      return getDao().findAll(spec, pgbl);
    } else{
      logger.trace("No example provided, using no spec and calling findAll(pgbl).");
      return getDao().findAll(pgbl);
    }
  }

  @Override
  public void update(RepoUser user){
    logger.trace("Performing update({}).", "RepoUser#" + user.getId());
    getDao().save(user);
    logger.trace("Resource successfully persisted.");
  }

  @Override
  public void patch(RepoUser entity, JsonPatch patch, Collection<? extends GrantedAuthority> userGrants){
    logger.trace("Performing patch({}, {}, {}).", "RepoUser#" + entity.getId(), patch, userGrants);
    RepoUser updated = PatchUtil.applyPatch(entity, patch, RepoUser.class, userGrants);
    logger.trace("Patch successfully applied. Persisting patched resource.");
    getDao().save(updated);
    logger.trace("Resource successfully persisted.");
  }

  @Override
  public void delete(RepoUser user){
    logger.trace("Performing delete({}).", "RepoUser#" + user.getId());
    if(user.getActive()){
      logger.debug("Deactivating user {}.", user.getUsername());
      user.setActive(Boolean.FALSE);
      logger.trace("Persisting deactivated user.");
      getDao().save(user);
      logger.trace("Resource successfully persisted.");
    } else{
      logger.trace("User {} is deactivated. Removing user.", user.getUsername());
      getDao().delete(user);
      logger.trace("Resource successfully removed.");
    }
  }

  protected IUserDao getDao(){
    return dao;
  }

  @Override
  public Health health(){
    return Health.up().withDetail("Users", dao.count()).build();
  }

  @Override
  public RepoUser put(RepoUser c, RepoUser c1, Collection<? extends GrantedAuthority> clctn) throws UpdateForbiddenException{
    throw new FeatureNotImplementedException("PUT is not supported for user resouces.");
  }
}
