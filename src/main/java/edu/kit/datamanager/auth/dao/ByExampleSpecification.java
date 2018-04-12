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
package edu.kit.datamanager.auth.dao;

import edu.kit.datamanager.auth.annotations.Searchable;
import io.micrometer.core.instrument.search.Search;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.SingularAttribute;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import org.apache.commons.lang3.Validate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * Helper to create find by example query.
 */
@Component
public class ByExampleSpecification{

  private EntityManager entityManager;

  public ByExampleSpecification(EntityManager entityManager){
    this.entityManager = entityManager;
  }

  public <T> Specification<T> byExample(T example){
    return byExample(entityManager, example);
  }

  /**
   * Lookup entities having at least one String attribute matching the passed
   * pattern.
   */
  public <T> Specification<T> byPatternOnStringAttributes(String pattern, Class<T> entityType){
    return byPatternOnStringAttributes(entityManager, pattern, entityType);
  }

  private <T> Specification<T> byExample(final EntityManager em, final T example){
    Validate.notNull(example, "example must not be null");

    @SuppressWarnings("unchecked")
    final Class<T> type = (Class<T>) example.getClass();

    return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
      List<Predicate> predicates = new ArrayList<>();

      Set<SingularAttribute<T, ?>> types = em.getMetamodel().entity(type).getDeclaredSingularAttributes();

      for(Attribute<T, ?> attr : types){
        if(attr.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE || attr.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE){
          continue;
        }

        String fieldName = attr.getName();

        try{
          Member javaMember = attr.getJavaMember();
          boolean searchableField;
          if(javaMember instanceof Field){
            searchableField = ((Field) javaMember).getAnnotation(Searchable.class) != null;
          } else{
            searchableField = ((Method) javaMember).getAnnotation(Searchable.class) != null;
          }

          if(searchableField){
            if(attr.getJavaType() == String.class){
              String fieldValue;
              if(javaMember instanceof Field){
                fieldValue = (String) ((Field) javaMember).get(example);
              } else{
                fieldValue = (String) ReflectionUtils.invokeMethod((Method) javaMember, example);
              }

              if(isNotEmpty(fieldValue)){
                // please compiler
                SingularAttribute<T, String> stringAttr = em.getMetamodel().entity(type).getDeclaredSingularAttribute(fieldName, String.class);
                // apply like
                predicates.add(builder.like(root.get(stringAttr), pattern(fieldValue)));
              }
            } else{
              Object fieldValue;
              if(javaMember instanceof Field){
                fieldValue = ((Field) javaMember).get(example);
              } else{
                fieldValue = (String) ReflectionUtils.invokeMethod((Method) javaMember, example);
              }

              if(fieldValue != null){
                // please compiler
                SingularAttribute<T, ?> anyAttr = em.getMetamodel().entity(type).getDeclaredSingularAttribute(fieldName, fieldValue.getClass());
                //  apply equal
                predicates.add(builder.equal(root.get(anyAttr), fieldValue));
              }
            }
          }
        } catch(IllegalAccessException | IllegalArgumentException e){
          throw new IllegalStateException("OOOUCH!!!", e);
        }
      }

      if(predicates.size() > 0){
        return builder.and((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));
      }

      return builder.conjunction();
    };
  }

  private <T> Specification<T> byPatternOnStringAttributes(final EntityManager em, final String pattern, final Class<T> type){
    return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
      List<Predicate> predicates = new ArrayList<>();

      Set<SingularAttribute<T, ?>> types = em.getMetamodel().entity(type).getDeclaredSingularAttributes();

      for(Attribute<T, ?> attr : types){
        if(attr.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE){
          continue;
        }

        String fieldName = attr.getName();

        try{
          if(attr.getJavaType() == String.class){
            if(isNotEmpty(pattern)){
              SingularAttribute<T, String> stringAttr = em.getMetamodel().entity(type)
                      .getDeclaredSingularAttribute(fieldName, String.class);
              predicates.add(builder.like(root.get(stringAttr), pattern(pattern)));
            }
          }
        } catch(Exception e){
          throw new IllegalStateException("OOOUCH!!!", e);
        }
      }

      if(predicates.size() > 0){
        return builder.or((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));
      }

      return builder.conjunction(); // 1 = 1
    };
  }

  static private String pattern(String str){
    return "%" + str + "%";
  }
}
