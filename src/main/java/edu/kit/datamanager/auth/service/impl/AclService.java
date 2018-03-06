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

import edu.kit.datamanager.auth.dao.IAclDao;
import edu.kit.datamanager.auth.dao.INoteDao;
import edu.kit.datamanager.auth.domain.AclEntry;
import edu.kit.datamanager.auth.domain.Note;
import edu.kit.datamanager.auth.service.IAclService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jejkal
 */
@Service
@Transactional
public class AclService implements IAclService<AclEntry>{

  @Autowired
  private IAclDao dao;

  public AclService(){
    super();
  }

  // write
  @Override
  public AclEntry create(final AclEntry entity){
    return getDao().saveAndFlush(entity);
  }

  @Override
  public AclEntry update(final AclEntry entity){
    return getDao().save(entity);
  }

  @Override
  public void delete(final AclEntry entity){
    getDao().delete(entity);
  }

  @Override
  public void deleteById(final long entityId){
    getDao().delete(entityId);
  }

  protected JpaRepository<AclEntry, Long> getDao(){
    return dao;
  }
}
