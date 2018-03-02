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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

/**
 *
 * @author jejkal
 */
public interface JsonMapper{

  ObjectMapper mapper = ObjectMapperWrapper.init();

  class ObjectMapperWrapper{

    private static ObjectMapper init(){
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JodaModule());
      objectMapper.registerModule(new AfterburnerModule());
      return objectMapper;
    }
  }
}
