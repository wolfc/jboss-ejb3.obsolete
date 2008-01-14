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
package org.jboss.ejb3.interceptors.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.jboss.aop.Advisor;
import org.jboss.logging.Logger;

/**
 * Wraps an AOP Advisor to become an extended advisor of itself.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ExtendedAdvisorWrapper implements ExtendedAdvisor
{
   private static final Logger log = Logger.getLogger(ExtendedAdvisorWrapper.class);

   private Advisor advisor;
   
   ExtendedAdvisorWrapper(Advisor advisor)
   {
      assert advisor != null : "advisor is null";
      this.advisor = advisor;
   }
   
   private final void checkClass(Class<?> cls)
   {
      if(cls != advisor.getClazz())
         throw new IllegalArgumentException("Can't advise on " + cls + " (only on " + advisor.getClazz() + ")");
   }
   
   public boolean isAnnotationPresent(Class<?> cls, Class<? extends Annotation> annotationType)
   {
      checkClass(cls);
      // AOP weirdness
      return advisor.hasAnnotation(cls, annotationType);
   }

   public boolean isAnnotationPresent(Class<?> cls, Member member, Class<? extends Annotation> annotationType)
   {
      checkClass(cls);
      if(member instanceof Constructor)
         return advisor.resolveAnnotation((Constructor<?>) member, annotationType) != null; // don't ask
      else if(member instanceof Field)
         return advisor.resolveAnnotation((Field) member, annotationType) != null; // don't ask
      else if(member instanceof Method)
         return advisor.hasAnnotation((Method) member, annotationType);
      throw new IllegalStateException("unknown type of member " + member);
   }

   public <A extends Annotation> A resolveAnnotation(Class<?> cls, Class<A> annotationType)
   {
      return annotationType.cast(advisor.resolveAnnotation(annotationType));
   }

   public <A extends Annotation> A resolveAnnotation(Class<?> cls, Member member, Class<A> annotationType)
   {
      checkClass(cls);
      if(member instanceof Constructor)
         return annotationType.cast(advisor.resolveAnnotation((Constructor<?>) member, annotationType));
      else if(member instanceof Field)
         return annotationType.cast(advisor.resolveAnnotation((Field) member, annotationType));
      else if(member instanceof Method)
         return annotationType.cast(advisor.resolveAnnotation((Method) member, annotationType));
      throw new IllegalStateException("unknown type of member " + member);
   }
}
