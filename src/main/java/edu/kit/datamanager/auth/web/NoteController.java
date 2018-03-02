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

import edu.kit.datamanager.auth.domain.Note;
import edu.kit.datamanager.auth.service.INoteService;
import edu.kit.datamanager.auth.web.hateoas.event.PaginatedResultsRetrievedEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
@Controller
@RequestMapping(value = "/api/v1/notes")
public class NoteController{

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private INoteService service;

  @Autowired
  private AclService aclService;

  public NoteController(){
    super();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/count")
  @ResponseBody
  @ResponseStatus(value = HttpStatus.OK)
  public long count(){
    return 2l;
  }

  @RequestMapping(value = "/autogen", method = RequestMethod.GET)
  @ResponseBody
  @Transactional
  public void autogen(final HttpServletResponse response){

    for(int i = 0; i < 1000; i++){
      Note n = new Note();
      n.setValue("Note " + i);
      n = (Note) service.create(n);
      System.out.println("Note " + i + " created.");
      MutableAcl acl = ((JdbcMutableAclService) aclService).createAcl(new ObjectIdentityImpl(Note.class, n.getId()));

      acl.insertAce(0, BasePermission.READ, new PrincipalSid("admin"), true);
      acl.insertAce(1, BasePermission.READ, new PrincipalSid("ROLE_USER"), true);
      ((JdbcMutableAclService) aclService).updateAcl(acl);
      System.out.println("ACL created.");
    }
  }

  @RequestMapping(value = "/impossible", method = RequestMethod.GET)
  @ResponseBody
  public void impossible(final HttpServletResponse response, final Authentication authentication){

    List<Note> all = service.findAll();
    System.out.println("SIZE " + all.size());

    JdbcMutableAclService aclSer = ((JdbcMutableAclService) aclService);
    long s = System.currentTimeMillis();
    long dd = 0;
    long desc = 0;

    List<ObjectIdentity> ids = new ArrayList<>();

    all.forEach((n) -> {
      ids.add(new ObjectIdentityImpl(Note.class, n.getId()));
    });
    long t = System.currentTimeMillis();
    Map<ObjectIdentity, Acl> acls = aclSer.readAclsById(ids, Arrays.asList(new PrincipalSid(authentication)));
    Set<Entry<ObjectIdentity, Acl>> entries = acls.entrySet();
    for(Entry<ObjectIdentity, Acl> entry : entries){
      long dt = System.currentTimeMillis();
      //Acl acl = aclSer.readAclById(new ObjectIdentityImpl(Note.class, n.getId()));
      try{
        if(entry.getValue().isGranted(Arrays.asList(BasePermission.WRITE), Arrays.asList(new PrincipalSid("admin"), new PrincipalSid("ROLE_USER")), false)){
          System.out.println("GRANTED");
          //break;
        }

      } catch(NotFoundException ex){
        //    System.out.println("NO PERM for note " + n.getId());
      }

      dd += System.currentTimeMillis() - dt;
      desc++;
    }

    System.out.println("DUR " + (System.currentTimeMillis() - s));
    System.out.println("DESCT " + ((double) dd / (double) desc));
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  @ResponseBody
  public Note findById(@PathVariable("id") final Long id, final HttpServletResponse response){
    final Note resourceById = (Note) service.findOne(id);

    if(resourceById == null){
      throw new RuntimeException();
    }
    //eventPublisher.publishEvent(new SingleResourceRetrievedEvent(this, response));
    return resourceById;
  }

  @RequestMapping(params = {"page", "size"}, method = RequestMethod.GET)
  @ResponseBody
  public Page<Note> findPaginated(@RequestParam("page") final int p, @RequestParam("size") final int size, final UriComponentsBuilder uriBuilder, final HttpServletResponse response){
    final Page<Note> page = service.findPaginated(p, size);
    final Link link = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this.getClass()).findPaginated(p, size, uriBuilder, response)).withSelfRel();

    if(p > page.getTotalPages()){
      throw new RuntimeException();
    }
    eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent<>(Note.class, uriBuilder, response, p, page.getTotalPages(), size));

    return page;
    //PagedResources<Note> result = new PagedResources<>(page.getContent(), new PagedResources.PageMetadata(page.getSize(), p, page.getTotalElements(), page.getTotalPages()), link);
    //return result;
  }
}
