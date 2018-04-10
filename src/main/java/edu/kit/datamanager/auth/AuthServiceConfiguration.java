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

import edu.kit.datamanager.auth.service.INoteService;
import edu.kit.datamanager.auth.service.IUserService;
import edu.kit.datamanager.auth.service.impl.CustomUserDetailsService;
import edu.kit.datamanager.auth.service.impl.NoteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jejkal
 */
@Configuration
public class AuthServiceConfiguration{

  @Bean
  public IUserService customUserDetailsService(){
    return new CustomUserDetailsService();
  }
//
//  @Bean
//  public INoteService noteService(){
//    return new NoteService();
//  }
}
