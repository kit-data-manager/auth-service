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

import com.google.common.collect.Lists;
import edu.kit.datamanager.auth.dao.INoteDao;
import edu.kit.datamanager.auth.domain.Note;
import edu.kit.datamanager.auth.service.INoteService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jejkal
 */
@Service
@Transactional
public class NoteService implements INoteService<Note>{

  @Autowired
  private INoteDao dao;

  public NoteService(){
    super();
  }

  // read - one
  @Transactional(readOnly = true)
  @Override
  public Note findOne(final long id){
    return getDao().findOne(id);
  }

  // read - all
  /**
   *
   * @return
   */
  @Transactional(readOnly = true)
  @Override
  public List<Note> findAll(){
    return Lists.newArrayList(getDao().findAll());
  }

  @Override
  public Page<Note> findPaginated(final int page, final int size){
    return getDao().findAll(new PageRequest(page, size));
  }

  // write
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

  @Override
  public void deleteById(final long entityId){
    getDao().delete(entityId);
  }

  @Override
  public Page<Note> findPaginated(Pageable pageable){
    return getDao().findAll(pageable);
  }

  protected PagingAndSortingRepository<Note, Long> getDao(){
    return dao;
  }
}
