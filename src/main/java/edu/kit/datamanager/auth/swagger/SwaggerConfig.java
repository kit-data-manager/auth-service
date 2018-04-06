/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.auth.swagger;

import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.AuthorizationScopeBuilder;
import springfox.documentation.builders.PathSelectors;
import static springfox.documentation.builders.PathSelectors.regex;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * @author jejkal
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig{

  @Bean
  public Docket api(){
    AuthorizationScope[] authScopes = new AuthorizationScope[1];
    authScopes[0] = new AuthorizationScopeBuilder()
            .scope("")
            .build();

    SecurityReference securityReference = SecurityReference.builder()
            .reference("basicAuth")
            .scopes(authScopes)
            .build();

    ArrayList<SecurityScheme> auth = new ArrayList<>(1);
    auth.add(new BasicAuth("basicAuth"));

    ArrayList<SecurityContext> securityContexts = newArrayList(SecurityContext.builder().securityReferences(newArrayList(securityReference)).build());

    return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("edu.kit.datamanager.auth.web"))
            //.paths(regex("/v1/*"))
            // .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build().apiInfo(apiInfo())
            //  .securitySchemes(newArrayList(auth))
            //  .securityContexts(securityContexts);
            .securityContexts(Lists.newArrayList(securityContext()))
            .securitySchemes(Lists.newArrayList(apiKey()));
  }

  private ApiKey apiKey(){
    return new ApiKey("AUTHORIZATION", "Authorization", "header");
  }

  private ApiInfo apiInfo(){
    return new ApiInfo(
            "Auth Service - RESTful API",
            "This webpage describes the RESTful interface of the KIT Data Manager Auth Service.",
            "0.1",
            null,
            new Contact("Thomas Jejkal", "datamanager.kit.edu", "thomas.jejkal@kit.edu"),
            "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0.html", Collections.emptyList());
  }

  @Bean
  SecurityConfiguration security(){
    return new SecurityConfiguration(
            null,
            null,
            null, // realm Needed for authenticate button to work
            null, // appName Needed for authenticate button to work
            "BEARER ",// apiKeyValue
            ApiKeyVehicle.HEADER,
            "AUTHORIZATION", //apiKeyName
            null);
  }

  private SecurityContext securityContext(){
    return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .forPaths(PathSelectors.regex("/anyPath.*"))
            .build();
  }

  List<SecurityReference> defaultAuth(){
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return Lists.newArrayList(new SecurityReference("AUTHORIZATION", authorizationScopes));
  }
//  @Bean
//  SecurityContext securityContext(){
//    AuthorizationScope readScope = new AuthorizationScope("read:pets", "read your pets");
//    AuthorizationScope[] scopes = new AuthorizationScope[1];
//    scopes[0] = readScope;
//    SecurityReference securityReference = SecurityReference.builder()
//            .reference("petstore_auth")
//            .scopes(scopes)
//            .build();
//
//    return SecurityContext.builder()
//            .securityReferences(newArrayList(securityReference))
//            .forPaths(ant("/api/pet.*"))
//            .build();
//  }
//
//  @Bean
//  SecurityScheme oauth(){
//    return new OAuthBuilder()
//            .name("petstore_auth")
//            .grantTypes(grantTypes())
//            .scopes(scopes())
//            .build();
//  }
//
//  @Bean
//  SecurityScheme apiKey(){
//    return new ApiKey("api_key", "api_key", "header");
//  }
//
//  List<AuthorizationScope> scopes(){
//    return newArrayList(
//            new AuthorizationScope("write:pets", "modify pets in your account"),
//            new AuthorizationScope("read:pets", "read your pets"));
//  }
//
//  List<GrantType> grantTypes(){
//    GrantType grantType = new ImplicitGrantBuilder()
//            .loginEndpoint(new LoginEndpoint("http://petstore.swagger.io/api/oauth/dialog"))
//            .build();
//    return newArrayList(grantType);
//  }
//    @Override
//    public void init(ServletConfig config) throws ServletException {
//        super.init(config);
//
//        Info info = new Info()
//                .title("Auth Service - RESTful API")
//                .description("This webpage describes the RESTful interface of the KIT Data Manager Auth Service. "
//                        + "This interface is needed in order to authenticate users and to authorize access to resources.")
//                .termsOfService("http://datamanager.kit.edu")
//                .version("1.0")
//                .contact(new Contact()
//                        .email("thomas.jejkal@kit.edu"))
//                .license(new License()
//                        .name("Apache 2.0")
//                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
//
//        // ServletContext context = config.getServletContext();
//        Swagger swagger = new Swagger().info(info);
//      //  swagger.basePath("/auth-service-1.0-SNAPSHOT/api/v1");
//        swagger.basePath("/swagger");
//        //swagger.externalDocs(new ExternalDocs("Find out more about Swagger", "http://swagger.io"));
//        swagger.securityDefinition("api_key", new ApiKeyAuthDefinition("Authorization", In.HEADER));
//        new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
//    }
}
