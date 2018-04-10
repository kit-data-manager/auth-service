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
package edu.kit.datamanager.auth.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;
import edu.kit.datamanager.auth.domain.AclEntry;
import edu.kit.datamanager.auth.domain.DataEntry;
import edu.kit.datamanager.auth.domain.Note;
import edu.kit.datamanager.auth.service.INoteService;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author jejkal
 */
//@Controller
//@RequestMapping(value = "/api/v1/notes")
public class NoteController{

//  @Autowired
//  private ApplicationEventPublisher eventPublisher;
//
//  @Autowired
//  private INoteService noteService;
//
//  @Autowired
//  private RabbitTemplate rabbitTemplate;
//
//  public NoteController(){
//    super();
//  }
//
////  @RequestMapping(value = "/autogen", method = RequestMethod.GET)
////  @ResponseBody
////  @Transactional
////  public void autogen(final HttpServletResponse response){
////
////    for(int i = 0; i < 1000; i++){
////      Note n = new Note();
////      n.setValue("Note " + i);
////
////      Set<AclEntry> acls = new HashSet<>();
////      AclEntry entry = new AclEntry();
////      entry.setPermission(AclEntry.PERMISSION.READ);
////      entry.setSid("admin");
////      entry = (AclEntry) myAclService.create(entry);
////      //System.out.println("SAVE " + entry);
////      acls.add(entry);
////
////      AclEntry entry2 = new AclEntry();
////      entry2.setPermission(AclEntry.PERMISSION.READ);
////      entry2.setSid("ROLE_USER");
////      entry2 = (AclEntry) myAclService.create(entry2);
////      acls.add(entry2);
////
////      n.setAcls(acls);
////      n = (Note) service.create(n);
////      //System.out.println("NO " + n);
//////      System.out.println("Note " + i + " created.");
//////      MutableAcl acl = ((JdbcMutableAclService) aclService).createAcl(new ObjectIdentityImpl(Note.class, n.getId()));
//////
//////      acl.insertAce(0, BasePermission.READ, new PrincipalSid("admin"), true);
//////      acl.insertAce(1, BasePermission.READ, new PrincipalSid("ROLE_USER"), true);
//////      ((JdbcMutableAclService) aclService).updateAcl(acl);
//////      System.out.println("ACL created.");
////    }
////  }
//  @RequestMapping(value = "/", method = RequestMethod.GET)
//  @ResponseBody
//  public ResponseEntity<Resources<Note>> findAll(Pageable request, final HttpServletResponse response, final Authentication authentication){
//    List<String> sids = new ArrayList<>();
//    sids.add((String) authentication.getPrincipal());
//    authentication.getAuthorities().forEach((auth) -> {
//      sids.add("ROLE_" + auth.getAuthority());
//    });
//
//    Page<Note> pge = noteService.findByAclsSidInAndAclsPermissionGreaterThanEqual(sids, AclEntry.PERMISSION.READ, request);
//
//    List<Note> res = new ArrayList<>();
//    for(Note n : pge.getContent()){
//      n.add(new Link("http://heise.de/" + n.getNoteId(), "_self"));
//      res.add(n);
//    }
//    Link link = new Link("http://example.com/products/", "_next");
//
//    Resources<Note> resources = new Resources<>(res, link);
//
//    return ResponseEntity.ok(resources);
//  }
//
//  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
//  @ResponseBody
//  public ResponseEntity<Note> findById(@PathVariable("id") final Long id, WebRequest request, final HttpServletResponse response, final Authentication authentication){
//    List<String> sids = new ArrayList<>();
//    sids.add((String) authentication.getPrincipal());
//    authentication.getAuthorities().forEach((auth) -> {
//      sids.add("ROLE_" + auth.getAuthority());
//    });
//
//    Note n = noteService.findByNoteIdAndAclsSidInAndAclsPermissionGreaterThanEqual(id, sids, AclEntry.PERMISSION.READ);
//    if(n == null){
//      throw new EntityNotFoundException("Note with id " + id + " not found.");
//    }
//    if(request.checkNotModified(Long.toString(n.getVersion()))){
//      return null;
//    }
//    n.add(new Link("http://heise.de/" + n.getNoteId(), "_self"));
//
//    return ResponseEntity.ok(n);
//  }
//
//  @RequestMapping(value = "/{id}/acls", method = RequestMethod.GET)
//  @ResponseBody
//  public ResponseEntity<Resources<AclEntry>> findPermissions(@PathVariable("id") final Long id, WebRequest request, final HttpServletResponse response, final Authentication authentication){
//
//    List<String> sids = new ArrayList<>();
//    sids.add((String) authentication.getPrincipal());
//    authentication.getAuthorities().forEach((auth) -> {
//      sids.add("ROLE_" + auth.getAuthority());
//    });
//
//    Note n = noteService.findByNoteIdAndAclsSidInAndAclsPermissionGreaterThanEqual(id, sids, AclEntry.PERMISSION.READ);
//    Set<AclEntry> acls = n.getAcls();
//
//    final Link link = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this.getClass()).findPermissions(id, request, response, authentication)).withSelfRel();
//    Resources<AclEntry> resources = new Resources<>(acls, link);
//    return ResponseEntity.ok(resources);
//  }
//
//  @RequestMapping(value = "/search", method = RequestMethod.POST)
//  @ResponseBody
//  public ResponseEntity<Resources<Note>> findByExample(@RequestBody Note example, Pageable request, final HttpServletResponse response, final Authentication authentication){
//    List<String> sids = new ArrayList<>();
//    sids.add((String) authentication.getPrincipal());
//    authentication.getAuthorities().forEach((auth) -> {
//      sids.add("ROLE_" + auth.getAuthority());
//    });
//
//    Page<Note> pge = noteService.findAll(example, sids, AclEntry.PERMISSION.READ, request);
//
//    List<Note> res = new ArrayList<>();
//    for(Note n : pge.getContent()){
//      n.add(new Link("http://heise.de/" + n.getNoteId(), "_self"));
//      res.add(n);
//    }
//    Link link = new Link("http://example.com/products/", "_next");
//
//    Resources<Note> resources = new Resources<>(res, link);
//
//    return ResponseEntity.ok(resources);
//  }
//
//  @RequestMapping(value = "/", method = RequestMethod.POST)
//  @ResponseBody
//  public ResponseEntity<Note> create(@RequestBody Note note, final HttpServletResponse response, final Authentication authentication){
//    note.setNoteId(null);
//    boolean callerHasAcl = false;
//    if(note.getAcls() == null || note.getAcls().isEmpty()){
//      Set<AclEntry> acls = new HashSet<>();
//      note.setAcls(acls);
//    } else{
//      for(AclEntry entry : note.getAcls()){
//        if(((String) authentication.getPrincipal()).equals(entry.getSid())){
//          entry.setPermission(AclEntry.PERMISSION.ADMINISTRATE);
//          callerHasAcl = true;
//        }
//      }
//    }
//
//    if(!callerHasAcl){
//      AclEntry entry = new AclEntry();
//      entry.setSid((String) authentication.getPrincipal());
//      entry.setPermission(AclEntry.PERMISSION.ADMINISTRATE);
//      note.getAcls().add(entry);
//    }
//
//    note = (Note) noteService.create(note);
//
//    return ResponseEntity.created(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this.getClass()).create(note, response, authentication)).toUri()).build();
//  }
//
//  @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
//  @ResponseBody
//  public ResponseEntity patch(@PathVariable("id") final Long id, @RequestBody JsonPatch patch, WebRequest request, final HttpServletResponse response, final Authentication authentication){
//
//    List<String> sids = new ArrayList<>();
//    sids.add((String) authentication.getPrincipal());
//    authentication.getAuthorities().forEach((auth) -> {
//      sids.add("ROLE_" + auth.getAuthority());
//    });
//
//    Note n = noteService.findByNoteIdAndAclsSidInAndAclsPermissionGreaterThanEqual(id, sids, AclEntry.PERMISSION.WRITE);
//
//    if(!request.checkNotModified(Long.toString(n.getVersion()))){
//      return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
//    }
//    ObjectMapper mapper = new ObjectMapper();
//    JsonNode node = mapper.valueToTree(n);
//    try{
//      JsonNode patchedNode = patch.apply(node);
//      JsonNode diff = JsonDiff.asJson(node, patchedNode);
//      if(diff.elements().hasNext()){
//        //things have changed
//        Note patched = mapper.treeToValue(patchedNode, Note.class);
//        n = (Note) noteService.update(patched);
//      }
//    } catch(JsonPatchException | JsonProcessingException ex){
//      ex.printStackTrace();
//    }
//
//    return ResponseEntity.noContent().eTag("\"" + Long.toString(n.getVersion()) + "\"").build();
//  }
//
//  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
//  @ResponseBody
//  public ResponseEntity delete(@PathVariable("id") final Long id, WebRequest request, final HttpServletResponse response, final Authentication authentication){
//
//    List<String> sids = new ArrayList<>();
//    sids.add((String) authentication.getPrincipal());
//    authentication.getAuthorities().forEach((auth) -> {
//      sids.add("ROLE_" + auth.getAuthority());
//    });
//
//    //service always return value or an appropriate error
//    //if this fails due to missing permissions FORBIDDEN is already thrown, if no note is found NOT FOUND is thrown
//    Note n = (Note) noteService.findByNoteIdAndAclsSidInAndAclsPermissionGreaterThanEqual(id, sids, AclEntry.PERMISSION.DELETE);
//
//    if(!request.checkNotModified(Long.toString(n.getVersion()))){
//      return null;
//    }
//    noteService.delete(n);
//    return ResponseEntity.noContent().build();
//  }
//
//  @RequestMapping(path = "/{id}/data/{path:.+}", method = RequestMethod.POST)
//  public ResponseEntity handleFileUpload(@PathVariable(value = "id") String resourceIdentifier,
//          @PathVariable(value = "path") String path,
//          @RequestPart("file") MultipartFile file, @RequestPart(name = "metadata", required = false) DataEntry entry){
//
//    System.out.println("PATH " + path);
//    System.out.println("Received " + file.getOriginalFilename());
//    if(entry != null){
//      System.out.println("META " + entry.getMetadata());
//    }
//
//    // ApplicationContext context = new AnnotationConfigApplicationContext(RabbitMQConfiguration.class);
//    //  AmqpTemplate template = context.getBean(AmqpTemplate.class);
//    rabbitTemplate.convertAndSend("topic_note", "note.data.update", path + "/" + file.getOriginalFilename());
//
//    return ResponseEntity.ok().build();
//  }
//
//  @RequestMapping(path = "/{id}/data", method = RequestMethod.GET)
//  @ResponseBody
//  public ResponseEntity handleFileDownloadRoot(@PathVariable(value = "id") String id){
//    System.out.println("DOWNLOAD ROOT");
//
//    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "myFile.txt" + "\"").body(new FileSystemResource(new File("/Users/jejkal/Downloads/0901c413b1d1ac33.pdf")));
//  }
//
//  @RequestMapping(path = "/{id}/data/{path:.+}", method = RequestMethod.GET)
//  @ResponseBody
//  public ResponseEntity handleFileDownload(@PathVariable(value = "id") String resourceIdentifier,
//          @PathVariable(value = "path") String path){
//    System.out.println("PATH " + path);
//    System.out.println("DOWNLOAD SUB");
//
//    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "myFile.txt" + "\"").body(new FileSystemResource(new File("/Users/jejkal/Downloads/0901c413b1d1ac33.pdf")));
//  }
//
////  @RequestMapping(params = {"page", "size"}, method = RequestMethod.GET)
////  @ResponseBody
////  public Page<Note> findPaginated(@RequestParam("page") final int p, @RequestParam("size") final int size, final UriComponentsBuilder uriBuilder, final HttpServletResponse response){
////    final Page<Note> page = service.findPaginated(p, size);
////    final Link link = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this.getClass()).findPaginated(p, size, uriBuilder, response)).withSelfRel();
////
////    if(p > page.getTotalPages()){
////      throw new RuntimeException();
////    }
////    eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent<>(Note.class, uriBuilder, response, p, page.getTotalPages(), size));
////
////    return page;
////    //PagedResources<Note> result = new PagedResources<>(page.getContent(), new PagedResources.PageMetadata(page.getSize(), p, page.getTotalElements(), page.getTotalPages()), link);
////    //return result;
////  }
}
