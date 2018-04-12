/*
 * Copyright 2017 Karlsruhe Institute of Technology.
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

import edu.kit.datamanager.auth.annotations.Searchable;
import edu.kit.datamanager.auth.annotations.SecureUpdate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.Constants;
import edu.kit.datamanager.auth.exceptions.CustomInternalServerError;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import lombok.AccessLevel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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
public class RepoUser implements UserDetails{

  @Autowired
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @Transient
  private Logger LOGGER;

  public enum UserRole{
    CURATOR("ROLE_CURATOR"),
    ADMINISTRATOR("ROLE_ADMINISTRATOR"),
    USER("ROLE_USER"),
    GUEST("ROLE_GUEST"),
    INACTIVE("ROLE_INACTIVE");

    private final String value;

    UserRole(String role){
      this.value = role;
    }

    @Override
    public String toString(){
      return value;
    }

    public static UserRole fromValue(String value){
      for(UserRole uRole : values()){
        if(uRole.value.equals(value)){
          return uRole;
        }
      }
      throw new IllegalArgumentException("Value argument '" + value + " has no matching UserRole.");
    }
  }

  private static RepoUser SYSTEM = null;
  private static RepoUser ANONYMOUS = null;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @SecureUpdate({"FORBIDDEN"})
  @Searchable
  private Long id;
  @SecureUpdate({"FORBIDDEN"})
  @Searchable
  private String identifier;
  @Searchable
  @SecureUpdate({"ROLE_ADMINISTRATOR"})
  private String username;
  @Searchable
  private String email;
  private String activeGroup;
  private String password;
  @Searchable
  private String orcid;
  //special/internal properties that cannot be changed by the user
  @SecureUpdate({"ROLE_ADMINISTRATOR"})
  @ApiModelProperty(hidden = true)
  private Integer loginFailures = 0;
  @Temporal(TemporalType.TIMESTAMP)
  @SecureUpdate({"ROLE_ADMINISTRATOR"})
  @ApiModelProperty(hidden = true)
  private Date lockedUntil = null;
  @SecureUpdate({"ROLE_ADMINISTRATOR"})
  @ApiModelProperty(hidden = true)
  private Boolean active;
  @SecureUpdate({"ROLE_ADMINISTRATOR"})
  @ApiModelProperty(hidden = true)
  private Boolean locked;
  @Transient
  private transient Collection<UserRole> rolesAsEnum = new ArrayList<>();
  @SecureUpdate({"ROLE_ADMINISTRATOR"})
  private String roles;

  public static final synchronized RepoUser getSystemUser(){
    if(SYSTEM == null){
      SYSTEM = new RepoUser();
      SYSTEM.setIdentifier(Constants.SYSTEM_USER_ID);
      SYSTEM.setUsername("System");
      SYSTEM.setRolesAsEnum(Arrays.asList(UserRole.ADMINISTRATOR, UserRole.USER, UserRole.GUEST, UserRole.CURATOR));
    }
    return SYSTEM;
  }

  public static final synchronized RepoUser getAnonymousUser(){
    if(ANONYMOUS == null){
      ANONYMOUS = new RepoUser();
      ANONYMOUS.setIdentifier(Constants.ANONYMOUS_USER_ID);
      ANONYMOUS.setUsername("Anonymous");
      ANONYMOUS.setRolesAsEnum(Arrays.asList(UserRole.GUEST));
    }
    return ANONYMOUS;
  }

  public static RepoUser createUser(){
    return new RepoUser();
  }

  public RepoUser(){
    super();
    setRolesAsEnum(Arrays.asList(UserRole.USER));
  }

  public void erasePassword(){
    this.password = null;
  }

  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities(){
    Collection<GrantedAuthority> auths = new ArrayList<>();
    getRolesAsEnum().forEach((role) -> {
      auths.add(new SimpleGrantedAuthority(role.toString()));
    });
    return auths;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired(){
    return getActive();
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked(){
    return !getLocked();
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired(){
    return true;
  }

  @PostLoad
  public void convertRolesToEnum(){
    if(roles != null){
      try{
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.readTree(roles);
        rolesAsEnum = new ArrayList<>();
        if(jsonNode.isArray()){
          for(JsonNode node : jsonNode){
            String role = node.asText();
            UserRole r = UserRole.fromValue(role);
            rolesAsEnum.add(r);
          }
        }
      } catch(IOException | IllegalArgumentException ex){
        LOGGER.error("Failed to read user roles from " + this, ex);
        throw new CustomInternalServerError("Failed to read user roles.");
      }
    }
  }

  @PrePersist
  @PreUpdate
  public void convertEnumToRoles(){
    String[] values = new String[rolesAsEnum.size()];

    int cnt = 0;
    for(UserRole role : rolesAsEnum){
      values[cnt] = role.value;
      cnt++;
    }

    try{
      ObjectMapper mapper = new ObjectMapper();
      roles = mapper.writeValueAsString(values);
    } catch(JsonProcessingException ex){
      LOGGER.error("Failed to write user roles from " + this, ex);
      throw new CustomInternalServerError("Failed to write user roles.");
    }
  }

  @Override
  @JsonIgnore
  public boolean isEnabled(){
    return isAccountNonExpired() && isAccountNonLocked() && isCredentialsNonExpired();
  }
}
