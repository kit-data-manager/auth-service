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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.kit.dama.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;
import java.util.Objects;
import edu.kit.dama.entities.Role;
import edu.kit.dama.entities.dc40.Agent;
import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author jejkal
 */
@Entity
@Table(name = "repoUser")
@ApiModel(description = "An agent of type 'user' related to a resource, e.g. the creator or a contributor.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends Agent{

  public static final String USER_AGENT_TYPE = "USER";

  private static User SYSTEM = null;
  private static User ANONYMOUS = null;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String id;

  @ApiModelProperty(value = "Family name of the user.", example = "Doe", dataType = "String", required = false)
  private String familyName;
  @ApiModelProperty(value = "Given name of the user.", example = "John", dataType = "String", required = false)
  private String givenName;
  @ApiModelProperty(value = "Affiliation of the user, e.g. home institution.", example = "Karlsruhe Institute of Techology", required = false)
  private String affiliation;
  private String email;
  private String password;
  private boolean active;
  private String activeGroup;

  @ApiModelProperty(value = "The permission limit for the user. "
          + "This limit cannot be exceeded by any ACL, thus it can be used to e.g. block a user by limiting the permission to NONE. "
          + "The default for a physical user should be APPEND.", required = false)

  private ArrayList<String> roles;

  public static final synchronized User getSystemUser(){
    if(SYSTEM == null){
      SYSTEM = new User();
      SYSTEM.setIdentifier(Constants.SYSTEM_USER_ID);
      SYSTEM.setName("System");
      SYSTEM.setRoles(new ArrayList<>(Arrays.asList(Role.ADMINISTRATOR.toString(), Role.USER.toString(), Role.GUEST.toString(), Role.CURATOR.toString())));
    }
    return SYSTEM;
  }

  public static final synchronized User getAnonymousUser(){
    if(ANONYMOUS == null){
      ANONYMOUS = new User();
      ANONYMOUS.setIdentifier(Constants.ANONYMOUS_USER_ID);
      ANONYMOUS.setName("Anonymous");
      ANONYMOUS.setRoles(new ArrayList<>(Arrays.asList(Role.GUEST.toString())));
    }
    return ANONYMOUS;
  }

  public static User createUser(){
    return new User();
  }

  public User(){
    super();
    setAgentType(USER_AGENT_TYPE);
    getRoles().add(Role.USER.toString());
  }

  public String getFamilyName(){
    return familyName;
  }

  public void setFamilyName(String familyName){
    this.familyName = familyName;
  }

  public String getGivenName(){
    return givenName;
  }

  public void setGivenName(String givenName){
    this.givenName = givenName;
  }

  public String getAffiliation(){
    return affiliation;
  }

  public void setAffiliation(String affiliation){
    this.affiliation = affiliation;
  }

  public void setRoles(ArrayList<String> roles){
    this.roles = roles;
  }

  public ArrayList<String> getRoles(){
    if(roles == null){
      roles = new ArrayList<>();
    }
    return roles;
  }

  @Override
  public final void setAgentType(String agentType){
    super.setAgentType(USER_AGENT_TYPE);
  }

  public void setEmail(String email){
    this.email = email;
  }

  public String getEmail(){
    return email;
  }

  public void setActiveGroup(String activeGroup){
    this.activeGroup = activeGroup;
  }

  public String getActiveGroup(){
    return activeGroup;
  }

  public String getPassword(){
    return password;
  }

  public void setPassword(String password){
    this.password = password;
  }

  public void erasePassword(){
    this.password = null;
  }

  public void setActive(boolean active){
    this.active = active;
  }

  public boolean isActive(){
    return active;
  }

  @Override
  public int hashCode(){
    int hash = 7;
    hash = 41 * hash + Objects.hashCode(this.getName());
    hash = 41 * hash + Objects.hashCode(this.getIdentifier());
    hash = 41 * hash + Objects.hashCode(this.getIdentifierScheme());
    hash = 41 * hash + Objects.hashCode(this.getExpiresAt());
    hash = 41 * hash + Objects.hashCode(this.familyName);
    hash = 41 * hash + Objects.hashCode(this.givenName);
    hash = 41 * hash + Objects.hashCode(this.affiliation);
    hash = 41 * hash + (this.isDisabled() ? 1 : 0);
    hash = 41 * hash + Objects.hashCode(this.roles);
    return hash;
  }

  @Override
  public boolean equals(Object obj){
    if(this == obj){
      return true;
    }
    if(obj == null){
      return false;
    }
    if(getClass() != obj.getClass()){
      return false;
    }
    final User other = (User) obj;
    if(!Objects.equals(this.getName(), other.getName())){
      return false;
    }
    if(!Objects.equals(this.getIdentifier(), other.getIdentifier())){
      return false;
    }
    if(!Objects.equals(this.getIdentifierScheme(), other.getIdentifierScheme())){
      return false;
    }
    if(!Objects.equals(this.getExpiresAt(), other.getExpiresAt())){
      return false;
    }
    if(!Objects.equals(this.isDisabled(), other.isDisabled())){
      return false;
    }
    if(!Objects.equals(this.familyName, other.familyName)){
      return false;
    }
    if(!Objects.equals(this.givenName, other.givenName)){
      return false;
    }
    if(!Objects.equals(this.affiliation, other.affiliation)){
      return false;
    }
    return Objects.equals(this.roles, other.roles);
  }
}
