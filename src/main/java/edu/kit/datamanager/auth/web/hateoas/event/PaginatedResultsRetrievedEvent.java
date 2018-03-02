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
package edu.kit.datamanager.auth.web.hateoas.event;

import java.io.Serializable;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
public final class PaginatedResultsRetrievedEvent<T extends Serializable> extends ApplicationEvent{

  private final UriComponentsBuilder uriBuilder;
  private final HttpServletResponse response;
  private final int page;
  private final int totalPages;
  private final int pageSize;

  public PaginatedResultsRetrievedEvent(final Class<T> clazz, final UriComponentsBuilder uriBuilderToSet, final HttpServletResponse responseToSet, final int pageToSet, final int totalPagesToSet, final int pageSizeToSet){
    super(clazz);

    uriBuilder = uriBuilderToSet;
    response = responseToSet;
    page = pageToSet;
    totalPages = totalPagesToSet;
    pageSize = pageSizeToSet;
  }

  // API
  public final UriComponentsBuilder getUriBuilder(){
    return uriBuilder;
  }

  public final HttpServletResponse getResponse(){
    return response;
  }

  public final int getPage(){
    return page;
  }

  public final int getTotalPages(){
    return totalPages;
  }

  public final int getPageSize(){
    return pageSize;
  }

  /**
   * The object on which the Event initially occurred.
   *
   * @return The object on which the Event initially occurred.
   */
  @SuppressWarnings("unchecked")
  public final Class<T> getClazz(){
    return (Class<T>) getSource();
  }

}
