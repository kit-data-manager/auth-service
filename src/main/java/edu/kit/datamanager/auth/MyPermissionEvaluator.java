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
package edu.kit.datamanager.auth;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityGenerator;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.Authentication;

/**
 *
 * @author jejkal
 */
public class MyPermissionEvaluator implements PermissionEvaluator{

  private final Log logger = LogFactory.getLog(getClass());

  private final AclService aclService;
  private ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy = new ObjectIdentityRetrievalStrategyImpl();
  private ObjectIdentityGenerator objectIdentityGenerator = new ObjectIdentityRetrievalStrategyImpl();
  private SidRetrievalStrategy sidRetrievalStrategy = new SidRetrievalStrategyImpl();
  private PermissionFactory permissionFactory = new DefaultPermissionFactory();
  private BitMaskPermissionGrantingStrategy grantingStrategy = new BitMaskPermissionGrantingStrategy(new ConsoleAuditLogger());

  public MyPermissionEvaluator(AclService aclService){
    this.aclService = aclService;
  }

  /**
   * Determines whether the user has the given permission(s) on the domain
   * object using the ACL configuration. If the domain object is null, returns
   * false (this can always be overridden using a null check in the expression
   * itself).
   */
  public boolean hasPermission(Authentication authentication, Object domainObject, Object permission){
    if(domainObject == null){
      return false;
    }
    System.out.println("GET IDENT");
    ObjectIdentity objectIdentity = objectIdentityRetrievalStrategy
            .getObjectIdentity(domainObject);
    System.out.println("IDENT " + objectIdentity);
    return checkPermission(authentication, objectIdentity, permission);
  }

  public boolean hasPermission(Authentication authentication, Serializable targetId,
          String targetType, Object permission){
    ObjectIdentity objectIdentity = objectIdentityGenerator.createObjectIdentity(
            targetId, targetType);

    return checkPermission(authentication, objectIdentity, permission);
  }

  private boolean checkPermission(Authentication authentication, ObjectIdentity oid,
          Object permission){
    System.out.println("CHECK");
    // Obtain the SIDs applicable to the principal
    List<Sid> sids = sidRetrievalStrategy.getSids(authentication);
    System.out.println("SIDS " + sids);
    List<Permission> requiredPermission = resolvePermission(permission);
    System.out.println("REQ " + requiredPermission);

    final boolean debug = logger.isDebugEnabled();

    if(debug){
      logger.debug("Checking permission '" + permission + "' for object '" + oid
              + "'");
    }

    try{
      // Lookup only ACLs for SIDs we're interested in
      System.out.println("READ ACL");
      Acl acl = aclService.readAclById(oid, sids);
      System.out.println("ACL " + acl);

      //if(acl.isGranted(requiredPermission, sids, false)){
      if(grantingStrategy.isGranted(acl, requiredPermission, sids, false)){
        System.out.println("GRANTED");
        if(debug){
          logger.debug("Access is granted");
        }

        return true;
      } else{
        System.out.println("NOT GRANTED");
      }

      if(debug){
        logger.debug("Returning false - ACLs returned, but insufficient permissions for this principal");
      }

    } catch(NotFoundException nfe){
      System.out.println("EXE!!!!");
      nfe.printStackTrace();
      if(debug){
        logger.debug("Returning false - no ACLs apply for this principal");
      }
    }

    return false;

  }

  List<Permission> resolvePermission(Object permission){
    if(permission instanceof Integer){
      return Arrays.asList(permissionFactory.buildFromMask(((Integer) permission)
              .intValue()));
    }

    if(permission instanceof Permission){
      return Arrays.asList((Permission) permission);
    }

    if(permission instanceof Permission[]){
      return Arrays.asList((Permission[]) permission);
    }

    if(permission instanceof String){
      String permString = (String) permission;
      Permission p;

      try{
        p = permissionFactory.buildFromName(permString);
      } catch(IllegalArgumentException notfound){
        p = permissionFactory.buildFromName(permString.toUpperCase(Locale.ENGLISH));
      }

      if(p != null){
        return Arrays.asList(p);
      }

    }
    throw new IllegalArgumentException("Unsupported permission: " + permission);
  }

  public void setObjectIdentityRetrievalStrategy(
          ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy){
    this.objectIdentityRetrievalStrategy = objectIdentityRetrievalStrategy;
  }

  public void setObjectIdentityGenerator(ObjectIdentityGenerator objectIdentityGenerator){
    this.objectIdentityGenerator = objectIdentityGenerator;
  }

  public void setSidRetrievalStrategy(SidRetrievalStrategy sidRetrievalStrategy){
    this.sidRetrievalStrategy = sidRetrievalStrategy;
  }

  public void setPermissionFactory(PermissionFactory permissionFactory){
    this.permissionFactory = permissionFactory;
  }
}
