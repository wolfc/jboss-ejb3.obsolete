/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.annotation.finder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.annotations.Element;
import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.finder.AnnotationFinder;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AnnotationFinderEnvironmentBridge<E extends AnnotatedElement> implements AnnotationFinder<E>
{
   private static final Logger log = Logger.getLogger(AnnotationFinderEnvironmentBridge.class);
   
   private static final Annotation NONE = new Annotation() {
      public Class<? extends Annotation> annotationType()
      {
         return Annotation.class;
      }
   };
   
   private AnnotationEnvironment env;
   
   private Map<AnnotatedElement, MyMap<? extends Annotation>> cache = new HashMap<AnnotatedElement, MyMap<? extends Annotation>>();
   
   public AnnotationFinderEnvironmentBridge(AnnotationEnvironment env)
   {
      assert env != null : "env is null";
      this.env = env;
   }
   
   public <T extends Annotation> T getAnnotation(E element, Class<T> annotationType)
   {
      MyMap<? extends Annotation> cached = cache.get(element);
      if(cached == null)
      {
         synchronized(cache)
         {
            cached = cache.get(element);
            if(cached == null)
            {
               cached = new MyHashMap<T>();
               cache.put(element, cached);
            }
         }
      }
      
      Annotation annotation = cached.get(annotationType);
      if(annotation == null)
      {
         synchronized(cached)
         {
            annotation = cached.get(annotationType);
            if(annotation == null)
            {
               Set<Element<T, ?>> elements;
               if(element instanceof Class)
                  elements = (Set) env.classIsAnnotatedWith(annotationType);
               else if(element instanceof Field)
                  elements = (Set) env.classHasFieldAnnotatedWith(annotationType);
               else if(element instanceof Method)
                  elements = (Set) env.classHasMethodAnnotatedWith(annotationType);
               else
                  throw new UnsupportedOperationException("AnnotationEnvironment has no support for " + element);
               for(Element<T, ?> e : elements)
               {
                  MyMap<? extends Annotation> m = cache.get(e.getAnnotatedElement());
                  if(m == null)
                  {
                     MyMap<T> newEntry = (MyMap<T>) cached;
                     newEntry.put(annotationType, e.getAnnotation());
                     cache.put(e.getAnnotatedElement(), newEntry);
                     m = newEntry;
                  }
                  else
                  {
                     MyMap<T> newEntry = (MyMap<T>) m;
                     newEntry.put(annotationType, e.getAnnotation());
                  }
               }
            }
            annotation = cached.get(annotationType);
            if(annotation == null)
            {
               annotation = NONE;
            }
            assert annotation != null;
         }
      }
      if(annotation == NONE)
         return null;
      return (T) annotation;
   }

   public Annotation[] getAnnotations(E element)
   {
      throw new UnsupportedOperationException();
   }

   public Annotation[] getDeclaredAnnotations(E element)
   {
      throw new UnsupportedOperationException();
   }

   public boolean isAnnotationPresent(E element, Class<? extends Annotation> annotationType)
   {
      return getAnnotation(element, annotationType) != null;
   }
}
