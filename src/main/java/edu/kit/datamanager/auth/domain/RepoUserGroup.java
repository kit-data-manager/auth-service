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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.kit.datamanager.auth.annotations.Searchable;
import edu.kit.datamanager.auth.annotations.SecureUpdate;
import static edu.kit.datamanager.auth.domain.RepoUser.UserRole.values;
import io.swagger.annotations.ApiModel;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jejkal
 */
@Entity
@ApiModel(description = "An agent of type 'user' related to a resource, e.g. the creator or a contributor.")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class RepoUserGroup{

  public enum GroupRole{
    GROUP_MANAGER("ROLE_GROUP_MANAGER"),
    GROUP_MEMBER("ROLE_GROUP_MEMBER");

    private final String value;

    GroupRole(String role){
      this.value = role;
    }

    @Override
    public String toString(){
      return value;
    }

    public static GroupRole fromValue(String value){
      for(GroupRole uRole : values()){
        if(uRole.value.equals(value)){
          return uRole;
        }
      }
      throw new IllegalArgumentException("Value argument '" + value + " has no matching GroupRole.");
    }
  }
  @Autowired
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @Transient
  private Logger LOGGER;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @SecureUpdate({"FORBIDDEN"})
  @Searchable
  private Long id;
  @SecureUpdate({"FORBIDDEN"})
  @Searchable
  private String identifier;
  @Searchable
  @SecureUpdate({"ROLE_GROUP_MANAGER"})
  private String groupname;

  @OneToMany
  private Set<GroupMembership> memberships = new HashSet<>();

}
