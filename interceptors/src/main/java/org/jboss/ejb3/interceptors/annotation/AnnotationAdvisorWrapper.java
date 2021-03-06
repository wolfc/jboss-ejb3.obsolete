/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptors.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.jboss.aop.Advisor;
import org.jboss.logging.Logger;

/**
 * Wraps an AOP Advisor to become an AnnotationAdvisor.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
class AnnotationAdvisorWrapper implements AnnotationAdvisor
{
   private static final Logger log = Logger.getLogger(AnnotationAdvisorWrapper.class);

   private Advisor advisor;
   
   AnnotationAdvisorWrapper(Advisor advisor)
   {
      assert advisor != null : "advisor is null";
      this.advisor = advisor;
   }
   
   private final void checkClass(Class<?> cls)
   {
      if(cls != advisor.getClazz())
         throw new IllegalArgumentException("Can't advise on " + cls + " (only on " + advisor.getClazz() + ")");
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<?> cls, Class<T> annotationClass)
   {
      checkClass(cls);
      return (T) advisor.resolveAnnotation(annotationClass);
   }

   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<?> cls, Field field, Class<T> annotationClass)
   {
      checkClass(cls);
      return (T) advisor.resolveAnnotation(field, annotationClass);
   }

   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<?> cls, Method method, Class<T> annotationClass)
   {
      checkClass(cls);
      return (T) advisor.resolveAnnotation(method, annotationClass);
   }

   public boolean isAnnotationPresent(Class<?> cls, Class<? extends Annotation> annotationClass)
   {
      return advisor.hasAnnotation(cls, annotationClass);
   }

   public boolean isAnnotationPresent(Class<?> cls, Field field, Class<? extends Annotation> annotationClass)
   {
      checkClass(cls);
      // I hate AOP
      return advisor.resolveAnnotation(field, annotationClass) != null;
   }
   
   public boolean isAnnotationPresent(Class<?> cls, Method method, Class<? extends Annotation> annotationClass)
   {
      checkClass(cls);
      return advisor.hasAnnotation(method, annotationClass);
   }
}
