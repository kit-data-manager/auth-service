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

import edu.kit.datamanager.auth.domain.AclEntry;
import edu.kit.datamanager.auth.domain.Note;
import java.util.List;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author jejkal
 */
public interface INoteService extends HealthIndicator{

  public List<Note> findByAclsSidInAndAclsPermissionGreaterThanEqual(List<String> sids, AclEntry.PERMISSION permission);

  public Page<Note> findByAclsSidInAndAclsPermissionGreaterThanEqual(List<String> sids, AclEntry.PERMISSION permission, Pageable pgbl);

  public Note findByNoteIdAndAclsSidInAndAclsPermissionGreaterThanEqual(Long noteId, List<String> sids, AclEntry.PERMISSION permission);

  public Page<Note> findAll(Note example, List<String> sids, AclEntry.PERMISSION permission, Pageable pgbl);

//// read - one
  // Note findOne(final long id);
//  // read - all
//  List<T> findAll();
//
//  Page<T> findPaginated(int page, int size);
//
  // write
  Note create(final Note entity);

  Note update(final Note entity);

  void delete(final Note entity);

//  void deleteById(final long entityId);
//
//  Page<Note> findPaginated(Pageable pageable);
}
