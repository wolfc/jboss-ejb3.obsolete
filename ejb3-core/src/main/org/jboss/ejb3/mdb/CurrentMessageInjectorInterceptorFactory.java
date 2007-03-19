/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.mdb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.annotation.ejb.CurrentMessage;

import org.jboss.ejb3.Container;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class CurrentMessageInjectorInterceptorFactory implements AspectFactory
{
   public Object createPerVM()
   {
      return null;
   }

   public Object createPerClass(Advisor advisor)
   {
      Class clazz = advisor.getClazz();
      ArrayList<Field> fs = new ArrayList<Field>();
      ArrayList<Method> ms = new ArrayList<Method>();

      search(advisor,clazz, fs, ms);

      Method[] methods = ms.toArray(new Method[ms.size()]);
      Field[] fields = fs.toArray(new Field[fs.size()]);
      if (methods.length == 0) methods = null;
      if (fields.length == 0) fields = null;
      return new CurrentMessageInjectorInterceptor(fields, methods);
   }

   protected void search(Advisor advisor, Class clazz, ArrayList<Field> fs, ArrayList<Method> ms)
   {
      Method[] methods = clazz.getDeclaredMethods();
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields)
      {
         if (advisor.resolveAnnotation(field, CurrentMessage.class) != null)
            fs.add(field);
      }
      for (Method method : methods)
      {
         if (advisor.resolveAnnotation(method, CurrentMessage.class) != null)
            ms.add(method);
      }
      if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class))
      {
         search(advisor, clazz.getSuperclass(), fs, ms);
      }
   }

   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      throw new IllegalArgumentException("NOT LEGAL");
   }

   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   {
      throw new IllegalArgumentException("NOT LEGAL");
   }

   public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint jp)
   {
      throw new IllegalArgumentException("NOT LEGAL");
   }

   public String getName()
   {
      return CurrentMessageInjectorInterceptor.class.getName();
   }
}
