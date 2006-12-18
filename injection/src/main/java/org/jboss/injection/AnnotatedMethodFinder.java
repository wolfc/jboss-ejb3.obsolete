/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Finds all methods annotation with a certain annotation.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AnnotatedMethodFinder<T extends Annotation> implements Processor<Class<?>, Collection<Method>>
{
   private Class<T> annotationClass;
   
   public AnnotatedMethodFinder(Class<T> annotationClass)
   {
      assert annotationClass != null : "annotationClass is null";
      
      this.annotationClass = annotationClass;
   }
   
   public Collection<Method> process(Class<?> cls)
   {
      Collection<Method> list = new ArrayList<Method>();
      if(cls == null) return list;
      
      Method methods[] = cls.getDeclaredMethods();
      for(Method method : methods)
      {
         T annotation = method.getAnnotation(annotationClass);
         if(annotation != null)
         {
            list.add(method);
         }
      }
      
      list.addAll(process(cls.getSuperclass()));
      
      return list;
   }
}
