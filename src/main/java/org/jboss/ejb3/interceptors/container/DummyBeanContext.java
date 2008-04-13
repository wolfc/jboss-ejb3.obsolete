/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptors.container;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DummyBeanContext<T> implements BeanContext<T>
{
   private T instance;
   private Object[] interceptors;
   private LinkedHashMap<Class<?>, Object> interceptorsMap = new LinkedHashMap<Class<?>, Object>();
   
   public DummyBeanContext(T instance, List<Object> interceptors)
   {
      assert instance != null : "instance is null";
      assert interceptors != null : "interceptors is null";
      this.instance = instance;
      this.interceptors = interceptors.toArray(new Object[0]);
      for(Object interceptor : interceptors)
         this.interceptorsMap.put(interceptor.getClass(), interceptor);
   }
   
   public T getInstance()
   {
      return instance;
   }

   public Object getInterceptor(Class<?> interceptorClass) throws IllegalArgumentException
   {
      Object interceptor = interceptorsMap.get(interceptorClass);
      if(interceptor == null)
         throw new IllegalArgumentException("No interceptor found for " + interceptorClass + " in " + this);
      return interceptor;
   }

   public Object[] getInterceptors()
   {
      return interceptors;
   }
}
