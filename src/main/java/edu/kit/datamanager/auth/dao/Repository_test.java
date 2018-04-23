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

import edu.kit.datamanager.auth.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jejkal
 */
//@Repository
public interface Repository_test {//extends JpaRepository<Note, Long>{//,JpaSpecificationExecutor{

//  @Override
//  @PostFilter(value = "hasPermission(filterObject, 'READ')")
//  public List<Note> findAll();
//
//  @Override
//  @PostAuthorize("hasPermission(returnObject, 'READ')")
//  public Note findOne(Long id);
//
//  @Override
//  //@PreAuthorize("hasPermission(#s, 'WRITE')")
//  public <S extends Note> S save(S s);
//
//  @Override
//  @PreAuthorize("hasPermission(#s, 'DELETE')")
//  public void delete(Note t);

  //@PostAuthorize("hasPermission(returnObject, 'READ')")
  // @PostFilter("hasRole('ADMIN')")
  // @PreAuthorize("hasRole('ROLE_ADMIN')")
  // Note findById(Long id);
//  @Override
//  @PostFilter("hasPermission(filterObject, 'READ')")
//  public Iterable<Note> findAll();
  // User findByEmail(String email);
//  @Override
//  @RestResource(exported = false)
//  void delete(String id);
//  @Override
//  //@PostFilter("hasPermission(filterObject, 'READ')")
//  @RestResource(exported = false)
//  public Page<Note> findAll(Pageable pgbl);
//
//  @Override
//  @PostFilter("hasPermission(filterObject, 'READ')")
//  public Iterable<Note> findAll(Sort sort);
}
