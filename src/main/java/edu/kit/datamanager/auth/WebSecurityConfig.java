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

import edu.kit.datamanager.auth.configuration.ApplicationProperties;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.auth.web.security.ExtendedJwtAuthenticationProvider;
import edu.kit.datamanager.security.filter.JwtAuthenticationFilter;
import edu.kit.datamanager.security.filter.NoopAuthenticationEventPublisher;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 *
 * @author jejkal
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

  @Autowired
  private Logger logger;

  @Autowired
  private ApplicationProperties applicationProperties;

  @Autowired
  private IUserService userService;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

//  @Autowired
//  private UserRepositoryImpl userRepositoryImpl;
  public WebSecurityConfig(){
  }

//  @Autowired
//  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
//    auth
//            .inMemoryAuthentication()
//            .withUser("admin").password("admin").roles("USER", "ADMIN").and().withUser("user").password("user").roles("GUEST");
//  }
  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception{
    auth.authenticationEventPublisher(new NoopAuthenticationEventPublisher()).authenticationProvider(new ExtendedJwtAuthenticationProvider(applicationProperties.getJwtSecret(), userService, passwordEncoder, logger));
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception{
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()
            .addFilterBefore(new BasicAuthenticationFilter(authenticationManager()), AbstractPreAuthenticatedProcessingFilter.class)
            .addFilterAfter(new JwtAuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class).
            authorizeRequests().
            antMatchers("/api/v1/login").permitAll().
            antMatchers("/swagger-ui.html").permitAll().
            antMatchers("/api/v1").authenticated();
    http.headers().cacheControl().disable();
    //.authorizeRequests()
    //.antMatchers("/api/v1/login").permitAll()
    // .antMatchers("/admin/**").hasAuthority("ADMIN")
    // .antMatchers("/owner/**").hasAnyAuthority("OWNER", "ADMIN")
    // .antMatchers("/health", "invitation/accept").permitAll()
    //.antMatchers("/**").hasAnyRole("USER", "GUEST");//hasAnyAuthority("USER");
    // .anyRequest().hasAnyRole(new String[]{"ADMIN", "USER", "GUEST"});
    //http.authorizeRequests().antMatchers("/css/**", "/js/**", "/loggedout").permitAll().anyRequest().authenticated().and().httpBasic().and().logout().disable().csrf().disable();
  }

//  @Bean
//  CorsConfigurationSource corsConfigurationSource(){
//    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//    source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
//    return source;
//  }
//  @Bean
//  public UserRepositoryImpl userRepositoryImpl(){
//    return new UserRepositoryImpl();
//  }
}
