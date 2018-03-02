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

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 *
 * @author jejkal
 */
@Configuration
@EnableAutoConfiguration
public class ACLContext{

  @Autowired
  DataSource dataSource;

  @Bean
  public EhCacheBasedAclCache aclCache(){
    return new EhCacheBasedAclCache(aclEhCacheFactoryBean().getObject(), permissionGrantingStrategy(), aclAuthorizationStrategy());
  }

  @Bean
  public EhCacheFactoryBean aclEhCacheFactoryBean(){
    EhCacheFactoryBean ehCacheFactoryBean = new EhCacheFactoryBean();
    ehCacheFactoryBean.setCacheManager(aclCacheManager().getObject());
    ehCacheFactoryBean.setCacheName("aclCache");
    return ehCacheFactoryBean;
  }

  @Bean
  public EhCacheManagerFactoryBean aclCacheManager(){
    return new EhCacheManagerFactoryBean();
  }

  @Bean
  public PermissionGrantingStrategy permissionGrantingStrategy(){
    return new BitMaskPermissionGrantingStrategy(new ConsoleAuditLogger());
  }

  @Bean
  public AclAuthorizationStrategy aclAuthorizationStrategy(){
    return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }

  @Bean
  public MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler(){
    DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
    
//    AclPermissionEvaluator permissionEvaluator = new AclPermissionEvaluator(aclService()){
//
//      @Override
//      public boolean hasPermission(Authentication authentication, Object domainObject, Object permission){
//        System.out.println("HAS PERMISSION 3");
//        System.out.println("CHECKING PERMISSION " + permission + " for " + domainObject + " with " + authentication);
//        System.out.println("IDENT " + new ObjectIdentityRetrievalStrategyImpl().getObjectIdentity(domainObject));
//        boolean result = super.hasPermission(authentication, domainObject, permission); //To change body of generated methods, choose Tools | Templates.
//        System.out.println("RESULT " + result);
//        return result;
//      }
//
//    };
    MyPermissionEvaluator permissionEvaluator = new MyPermissionEvaluator(aclService());
    expressionHandler.setPermissionEvaluator(permissionEvaluator);
    return expressionHandler;
  }

  @Bean
  public LookupStrategy lookupStrategy(){
    return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), new ConsoleAuditLogger());
  }

  @Bean
  public JdbcMutableAclService aclService(){
    return new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
  }

}
