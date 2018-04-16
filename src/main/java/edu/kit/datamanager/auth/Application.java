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
import edu.kit.datamanager.auth.service.IGroupService;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.auth.service.impl.RepoUserService;
import edu.kit.datamanager.auth.service.impl.RepoUserGroupService;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 *
 * @author jejkal
 */
@SpringBootApplication
public class Application{

  @Autowired
  private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

  @Bean
  @Scope("prototype")
  public Logger logger(InjectionPoint injectionPoint){
    Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
    return LoggerFactory.getLogger(targetClass.getCanonicalName());
  }

  @Bean
  public ConnectionFactory connectionFactory(){
    return new CachingConnectionFactory("localhost");
  }

  @Bean
  public AmqpAdmin amqpAdmin(){
    return new RabbitAdmin(connectionFactory());
  }

  @Bean
  public RabbitTemplate rabbitTemplate(){
    return new RabbitTemplate(connectionFactory());
  }

  @Bean
  TopicExchange exchange(){
    return new TopicExchange("topic_note");
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  public IUserService userService(){
    return new RepoUserService();
  }

  @Bean
  public IGroupService userGroupService(){
    return new RepoUserGroupService();
  }

  @Bean
  @Primary
  public RequestMappingHandlerAdapter adapter(){
    return requestMappingHandlerAdapter;
  }

  @Bean
  public JsonViewSupportFactoryBean views(){
    return new JsonViewSupportFactoryBean();
  }
  
  @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter
          = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

//  @Bean
//  public Queue myQueue(){
//    return new Queue("myqueue");
//  }
//  @Bean
//  public Filter shallowETagHeaderFilter(){
//    return new ShallowEtagHeaderFilter();
//  }
  public static void main(String[] args){
    ApplicationContext ctx = SpringApplication.run(Application.class, args);
    /*  String[] beanNames = ctx.getBeanDefinitionNames();
    Arrays.sort(beanNames);
    for(String beanName : beanNames){
      System.out.println(beanName);
    }
    System.out.println("Spring Boot started...");*/
  }

}
