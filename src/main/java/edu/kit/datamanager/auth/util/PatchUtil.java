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
package edu.kit.datamanager.auth.util;

import edu.kit.datamanager.auth.annotations.SecureUpdate;
import edu.kit.datamanager.auth.exceptions.CustomInternalServerError;
import edu.kit.datamanager.auth.exceptions.UpdateForbiddenException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author jejkal
 */
public class PatchUtil{

  public static boolean canUpdate(Object originalObj, Object patched, Collection<? extends GrantedAuthority> authorities){
    for(Field field : patched.getClass().getDeclaredFields()){
      SecureUpdate secureUpdate = field.getAnnotation(SecureUpdate.class);
      if(secureUpdate != null){
        try{
          field.setAccessible(true);
          Object persistedField = field.get(patched);
          Object originalField = field.get(originalObj);
          String[] allowedRoles = secureUpdate.value();

          if(!Objects.equals(persistedField, originalField)){
            boolean canUpdate = false;
            for(String role : allowedRoles){//go though all roles allowed to update
              for(GrantedAuthority authority : authorities){//check owned authorities
                if(authority.getAuthority().equalsIgnoreCase(role)){//the current authority allows to update, proceed to next field
                  canUpdate = true;
                  break;
                }
              }
              if(canUpdate){
                //this field can be updated
                break;
              }
            }
            if(!canUpdate){
              //at least one field cannot be updated
              return false;
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
