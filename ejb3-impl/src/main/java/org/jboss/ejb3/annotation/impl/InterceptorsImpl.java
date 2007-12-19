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
package org.jboss.ejb3.annotation.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.interceptor.Interceptors;

/**
 * // *
 *
 * @author <a href="mailto:bill@jboss.org">William DeCoste</a>
 * @version $Revision$
 */
public class InterceptorsImpl implements Interceptors
{
   private Set<Class<?>> values = new LinkedHashSet<Class<?>>();

   public InterceptorsImpl()
   {
   }

   public Class<?>[] value()
   {
      Class<?>[] result = new Class[values.size()];
      values.toArray(result);
      return result;
   }

   public void addValue(Class<?> value)
   {
      values.add(value);
   }

   public static InterceptorsImpl getImpl(Interceptors interceptors)
   {
      if (interceptors == null)
      {
         return new InterceptorsImpl();
      }
      
      if (interceptors instanceof InterceptorsImpl)
      {
         return (InterceptorsImpl)interceptors;
      }
      
      InterceptorsImpl impl = new InterceptorsImpl();
      
      for (Class<?> clazz : interceptors.value())
      {
         impl.addValue(clazz);
      }
      return impl;
   }
   
   public Class<Interceptors> annotationType()
   {
      return Interceptors.class;
   }
   
   @Override
   public String toString()
   {
      return super.toString() + "{value=" + values + "}";
   }
}
