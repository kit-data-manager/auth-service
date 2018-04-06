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
import edu.kit.datamanager.auth.dao.INoteDao;
import edu.kit.datamanager.auth.dao.PermissionSpecification;
import edu.kit.datamanager.auth.domain.AclEntry;
import edu.kit.datamanager.auth.domain.Note;
import edu.kit.datamanager.auth.service.INoteService;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jejkal
 */
@Service
@Transactional
public class NoteService implements INoteService{

  @Autowired
  private INoteDao dao;

  @PersistenceContext
  private EntityManager em;

  public NoteService(){
    super();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Note> findByAclsSidInAndAclsPermissionGreaterThanEqual(List<String> sids, AclEntry.PERMISSION permission){
    return ((INoteDao) getDao()).findByAclsSidInAndAclsPermissionGreaterThanEqual(sids, permission);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Note> findByAclsSidInAndAclsPermissionGreaterThanEqual(List<String> sids, AclEntry.PERMISSION permission, Pageable pgbl){
    return ((INoteDao) getDao()).findByAclsSidInAndAclsPermissionGreaterThanEqual(sids, permission, pgbl);
  }

  @Override
  @Transactional(readOnly = true)
  public Note findByNoteIdAndAclsSidInAndAclsPermissionGreaterThanEqual(Long noteId, List<String> sids, AclEntry.PERMISSION permission){
    Optional<Note> theNote = ((INoteDao) getDao()).findById(noteId);
    //TODO: check permission
    return theNote.get();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Note> findAll(Note example, List<String> sids, AclEntry.PERMISSION permission, Pageable pgbl){
    Specification<Note> spec = Specification.where(PermissionSpecification.toSpecification(sids, permission)).and(new ByExampleSpecification(em).byExample(example));
    return ((INoteDao) getDao()).findAll(spec, pgbl);
  }

  @Override
  public Note create(final Note entity){
    return getDao().save(entity);
  }

  @Override
  public Note update(final Note entity){
    return getDao().save(entity);
  }

  @Override
  public void delete(final Note entity){
    getDao().delete(entity);
  }

  protected PagingAndSortingRepository<Note, Long> getDao(){
    return dao;
  }

  @Override
  public Health health(){
    return Health.up()
            .withDetail("Notes", dao.count()).build();
  }
}
