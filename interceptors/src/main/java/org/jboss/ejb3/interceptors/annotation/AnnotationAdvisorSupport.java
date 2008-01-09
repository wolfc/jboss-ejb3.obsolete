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

import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AnnotationAdvisorSupport implements AnnotationAdvisor
{
   private static final Logger log = Logger.getLogger(AnnotationAdvisorSupport.class);

   public <T extends Annotation> T getAnnotation(Class<?> cls, Class<T> annotationClass)
   {
      return cls.getAnnotation(annotationClass);
   }

   public <T extends Annotation> T getAnnotation(Class<?> cls, Field field, Class<T> annotationClass)
   {
      return field.getAnnotation(annotationClass);
   }

   public <T extends Annotation> T getAnnotation(Class<?> cls, Method method, Class<T> annotationClass)
   {
      return method.getAnnotation(annotationClass);
   }

   public boolean isAnnotationPresent(Class<?> cls, Class<? extends Annotation> annotationClass)
   {
      return cls.isAnnotationPresent(annotationClass);
   }

   public boolean isAnnotationPresent(Class<?> cls, Field field, Class<? extends Annotation> annotationClass)
   {
      return field.isAnnotationPresent(annotationClass);
   }

   public boolean isAnnotationPresent(Class<?> cls, Method method, Class<? extends Annotation> annotationClass)
   {
      return method.isAnnotationPresent(annotationClass);
   }
}
