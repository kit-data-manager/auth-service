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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import edu.kit.datamanager.annotations.Searchable;
import edu.kit.datamanager.annotations.SecureUpdate;
import edu.kit.datamanager.entities.EtagSupport;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jejkal
 */
@Entity
@Schema(description = "An agent of type 'user' related to a resource, e.g. the creator or a contributor.")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(Include.NON_NULL)
public class RepoUserGroup implements EtagSupport, Serializable{

  public enum GroupRole{
    GROUP_MANAGER("ROLE_GROUP_MANAGER"),
    GROUP_MEMBER("ROLE_GROUP_MEMBER"),
    NO_MEMBER("ROLE_NO_MEMBER");

    private final String value;

    GroupRole(String role){
      this.value = role;
    }

    public String getValue(){
      return value;
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
  @JsonIgnore
  private Logger LOGGER;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @SecureUpdate({"FORBIDDEN"})
  @Searchable
  private Long id;
  @Searchable
  @SecureUpdate({"FORBIDDEN"})
  @Column(nullable = false, unique = true)
  private String groupId;
  @Column(nullable = false)
  @Searchable
  @SecureUpdate({"ROLE_GROUP_MANAGER", "ROLE_ADMINISTRATOR"})
  private String groupname;
  @Column(nullable = false)
  @SecureUpdate({"ROLE_GROUP_MANAGER", "ROLE_ADMINISTRATOR"})
  private Boolean active = true;
  @OneToMany(cascade = javax.persistence.CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @SecureUpdate({"ROLE_GROUP_MANAGER", "ROLE_ADMINISTRATOR"})
  private Set<GroupMembership> memberships = new HashSet<>();

  public static RepoUserGroup createGroup(String groupId){
    RepoUserGroup group = new RepoUserGroup();
    group.setGroupId(groupId);
    return group;
  }

  public void setGroupId(String groupId){
    if(groupId != null){
      this.groupId = groupId.toUpperCase();
    }
  }

  public final void addOrUpdateMembership(final RepoUser user, GroupRole role){
    GroupMembership membership = IteratorUtils.find(memberships.iterator(), (GroupMembership t) -> Long.compare(user.getId(), t.getUser().getId()) == 0);

    if(membership == null){
      //new membership
      memberships.add(new GroupMembership(user, role));
    } else{
      //update membership
      membership.setRole(role);
    }
  }

  public final boolean isMember(final RepoUser user){
    GroupMembership membership = IteratorUtils.find(memberships.iterator(), (GroupMembership t) -> Long.compare(user.getId(), t.getUser().getId()) == 0);
    return membership != null && !membership.getRole().equals(GroupRole.NO_MEMBER);
  }

  public final GroupRole getUserRole(final String userName){
    GroupMembership membership = IteratorUtils.find(memberships.iterator(), (GroupMembership t) -> userName.equals(t.getUser().getUsername()));
    if(membership != null){
      return membership.getRole();
    }

    return GroupRole.NO_MEMBER;
  }

  @JsonIgnore
  @Override
  public String getEtag(){
    return Integer.toString(hashCode());
  }

}
