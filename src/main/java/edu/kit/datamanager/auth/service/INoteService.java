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

import edu.kit.datamanager.auth.domain.Note;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author jejkal
 */
public interface INoteService<T>{
  // read - one

  T findOne(final long id);

  // read - all
  List<T> findAll();

  Page<T> findPaginated(int page, int size);

  // write
  T create(final T entity);

  T update(final T entity);

  void delete(final T entity);

  void deleteById(final long entityId);

  Page<Note> findPaginated(Pageable pageable);
}
