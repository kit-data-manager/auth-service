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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.auth.exceptions.CustomInternalServerError;
import edu.kit.datamanager.auth.exceptions.UpdateForbiddenException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author jejkal
 */
@MappedSuperclass
public class OEntity<T>{

  @Transient
  T originalObj;

  @PostLoad
  public void onLoad(){
    ObjectMapper mapper = new ObjectMapper();
    try{
      String serialized = mapper.writeValueAsString(this);
      this.originalObj = (T) mapper.readValue(serialized, this.getClass());
    } catch(IOException e){
      throw new CustomInternalServerError(e.getMessage());
    }
  }

  public boolean canUpdate(List<GrantedAuthority> authorities){
    if(originalObj == null){
      //creation detected
      return true;
    }

    for(Field field : this.getClass().getDeclaredFields()){
      SecureUpdate secureUpdate = field.getAnnotation(SecureUpdate.class);
      if(secureUpdate != null){
        try{
          field.setAccessible(true);
          Object persistedField = field.get(this);
          Object originalField = field.get(originalObj);
          String[] allowedRoles = secureUpdate.value();

          if(!Objects.equals(persistedField, originalField)){
            boolean canUpdate = true;
            for(String role : allowedRoles){//go though all roles allowed to update
              for(GrantedAuthority authority : authorities){//check owned authorities
                if(authority.getAuthority().equalsIgnoreCase(role)){//the current authority allows to update, proceed to next field
                  canUpdate = true;
                  break;
                }
              }
              if(canUpdate){
                break;
              }
            }

            if(!canUpdate){
              //the current field cannot be updated. Throw a new exception
              throw new UpdateForbiddenException("Updating field " + field.getName() + " is forbidden.");
              //return false;
            }
          }
        } catch(IllegalAccessException | IllegalArgumentException | SecurityException e){
          throw new CustomInternalServerError(e.getMessage());
        }
      }
    }

    return true;
  }
}
