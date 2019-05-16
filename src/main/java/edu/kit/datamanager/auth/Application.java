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

import com.monitorjbl.json.JsonViewSupportFactoryBean;
import edu.kit.datamanager.auth.configuration.ApplicationProperties;
import edu.kit.datamanager.auth.service.IGroupService;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.auth.service.impl.RepoUserService;
import edu.kit.datamanager.auth.service.impl.RepoUserGroupService;
import edu.kit.datamanager.service.IMessagingService;
import edu.kit.datamanager.service.impl.RabbitMQMessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 *
 * @author jejkal
 */
@SpringBootApplication
@ComponentScan(basePackages = {"edu.kit.datamanager", "edu.kit.datamanager.messaging.client"})
public class Application{

  @Autowired
  private RequestMappingHandlerAdapter requestMappingHandlerAdapter;
//  @Autowired
//  private Javers javers;

  @Bean
  @Scope("prototype")
  public Logger logger(InjectionPoint injectionPoint){
    Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
    return LoggerFactory.getLogger(targetClass.getCanonicalName());
  }

  @Bean
  @ConfigurationProperties("repo")
  public ApplicationProperties applicationProperties(){
    return new ApplicationProperties();
  }

  @Bean
  public IMessagingService messagingService(){
    return new RabbitMQMessagingService();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

//  @Bean
//  public IUserService userService(){
//    return new RepoUserService();
//  }
//
//  @Bean
//  public IGroupService userGroupService(){
//    return new RepoUserGroupService();//javers);
//  }

  @Bean
  @Primary
  public RequestMappingHandlerAdapter adapter(){
    return requestMappingHandlerAdapter;
  }

  @Bean
  public JsonViewSupportFactoryBean views(){
    return new JsonViewSupportFactoryBean();
  }

  public static void main(String[] args){
    ApplicationContext ctx = SpringApplication.run(Application.class, args);
  }

}
