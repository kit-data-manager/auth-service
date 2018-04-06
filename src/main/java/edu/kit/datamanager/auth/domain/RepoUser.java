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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.kit.datamanager.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author jejkal
 */
@Entity
@ApiModel(description = "An agent of type 'user' related to a resource, e.g. the creator or a contributor.")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class RepoUser extends OEntity<RepoUser> implements UserDetails{

  public enum UserRole{
    CURATOR,
    ADMINISTRATOR,
    USER,
    GUEST;
  }

  private static RepoUser SYSTEM = null;
  private static RepoUser ANONYMOUS = null;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @SecureUpdate({"FORBIDDEN"})
  private Long id;
  @SecureUpdate({"FORBIDDEN"})
  private String identifier;
  private String username;
  private String email;
  private String activeGroup;
  @JsonIgnore
  private String password;
  private String orcid;
  //special/internal properties that cannot be changed by the user
  @SecureUpdate({"ADMINISTRATOR"})
  @ApiModelProperty(hidden = true)
  @JsonIgnore
  private int loginFailures = 0;
  @Temporal(TemporalType.TIMESTAMP)
  @SecureUpdate({"ADMINISTRATOR"})
  @ApiModelProperty(hidden = true)
  @JsonIgnore
  private Date lockedUntil = null;
  @SecureUpdate({"ADMINISTRATOR"})
  @ApiModelProperty(hidden = true)
  @JsonIgnore
  private boolean active;
  @SecureUpdate({"ADMINISTRATOR"})
  @ApiModelProperty(hidden = true)
  @JsonIgnore
  private boolean locked;
  @SecureUpdate({"ADMINISTRATOR"})
  private ArrayList<String> roles;

  public static final synchronized RepoUser getSystemUser(){
    if(SYSTEM == null){
      SYSTEM = new RepoUser();
      SYSTEM.setIdentifier(Constants.SYSTEM_USER_ID);
      SYSTEM.setUsername("System");
      SYSTEM.setRoles(new ArrayList<>(Arrays.asList(UserRole.ADMINISTRATOR.toString(), UserRole.USER.toString(), UserRole.GUEST.toString(), UserRole.CURATOR.toString())));
    }
    return SYSTEM;
  }

  public static final synchronized RepoUser getAnonymousUser(){
    if(ANONYMOUS == null){
      ANONYMOUS = new RepoUser();
      ANONYMOUS.setIdentifier(Constants.ANONYMOUS_USER_ID);
      ANONYMOUS.setUsername("Anonymous");
      ANONYMOUS.setRoles(new ArrayList<>(Arrays.asList(UserRole.GUEST.toString())));
    }
    return ANONYMOUS;
  }

  public static RepoUser createUser(){
    return new RepoUser();
  }

  public RepoUser(){
    super();
    setRoles(new ArrayList<>(Arrays.asList(UserRole.USER.toString())));
  }

  public void erasePassword(){
    this.password = null;
  }

  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities(){
    Collection<GrantedAuthority> auths = new ArrayList<>();
    for(String role : roles){
      auths.add(new SimpleGrantedAuthority(role));
    }
    return auths;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired(){
    return isActive();
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked(){
    return !isLocked();
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired(){
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isEnabled(){
    return isAccountNonExpired() && isAccountNonLocked() && !isCredentialsNonExpired();
  }
}
