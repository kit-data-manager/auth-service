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

/**
 *
 * @author jejkal
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
//  DependencyInjectionTestExecutionListener.class,
//  DirtiesContextTestExecutionListener.class,
//  TransactionalTestExecutionListener.class,
//  WithSecurityContextTestExecutionListener.class})
//@Ignore
public class HttpRequestTest{

//  @Autowired
//  private MockMvc mockMvc;
//
////  @TestConfiguration
////  static class NoteServiceTestContextConfiguration{
////
////    @Bean
////    public INoteService noteService(){
////      return new TestNoteService();
////    }
////  }
////
////  @Autowired
////  private INoteService noteService;
//
////  @MockBean
////  private INoteDao dao;
//  @Before
//  public void setUp(){
//    Note note = new Note();
//    note.setValue("Patched1");
//    note.setNoteId(4l);
//    note.setVersion(0l);
//    Set<AclEntry> acls = new HashSet<>();
//    AclEntry e = new AclEntry("admin", AclEntry.PERMISSION.WRITE);
//    note.setAcls(acls);
//  //  Mockito.when(noteService.findByAclsSidInAndAclsPermissionGreaterThanEqual(4l, java.util.Arrays.asList(new String[]{"admin", "ROLE_ADMINISTRATOR", "ROLE_USER"}), PERMISSION.READ)).thenReturn(note);
//  }
//
//  @Test
//  @WithMockCustomUser
//  public void shouldReturnDefaultMessage() throws Exception{
//    this.mockMvc.perform(get("/api/v1/notes/4")).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.value").value("Patched1"));
//  }
}
