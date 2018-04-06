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
package edu.kit.datamanager.auth.test;

import edu.kit.datamanager.auth.domain.AclEntry;
import edu.kit.datamanager.auth.domain.AclEntry.PERMISSION;
import edu.kit.datamanager.auth.domain.Note;
import edu.kit.datamanager.auth.service.INoteService;
import java.util.HashSet;
import java.util.Set;
import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author jejkal
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
  DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class,
  TransactionalTestExecutionListener.class,
  WithSecurityContextTestExecutionListener.class})
public class HttpRequestTest{

  @Autowired
  private MockMvc mockMvc;

  @TestConfiguration
  static class NoteServiceTestContextConfiguration{

    @Bean
    public INoteService noteService(){
      return new TestNoteService();
    }
  }

  @Autowired
  private INoteService noteService;

//  @MockBean
//  private INoteDao dao;
  @Before
  public void setUp(){
    Note note = new Note();
    note.setValue("Patched1");
    note.setNoteId(4l);
    note.setVersion(0l);
    Set<AclEntry> acls = new HashSet<>();
    AclEntry e = new AclEntry("admin", AclEntry.PERMISSION.WRITE);
    note.setAcls(acls);
    Mockito.when(noteService.findByNoteIdAndAclsSidInAndAclsPermissionGreaterThanEqual(4l, java.util.Arrays.asList(new String[]{"admin", "ROLE_ADMINISTRATOR", "ROLE_USER"}), PERMISSION.READ)).thenReturn(note);
  }

  @Test
  @WithMockCustomUser
  public void shouldReturnDefaultMessage() throws Exception{
    this.mockMvc.perform(get("/api/v1/notes/4")).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.value").value("Patched1"));
  }
}
