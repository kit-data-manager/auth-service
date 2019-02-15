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
package edu.kit.datamanager.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.kit.datamanager.annotations.Searchable;
import edu.kit.datamanager.annotations.SecureUpdate;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jejkal
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupMembership{

  @Autowired
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @Transient
  @JsonIgnore
  private Logger LOGGER;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @SecureUpdate({"FORBIDDEN"})
  @Searchable
  private Long id;
  
  @OneToOne
  private RepoUser user;
  @Enumerated(EnumType.STRING)
  private RepoUserGroup.GroupRole role;

  public GroupMembership(){
  }

  public GroupMembership(RepoUser user, RepoUserGroup.GroupRole role){
    this.user = user;
    this.role = role;
  }

}
