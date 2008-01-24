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
package org.jboss.ejb3.interceptors.annotation.impl;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.interceptor.Interceptors;

import org.jboss.logging.Logger;

/**
 * A new implementation of Interceptors which allows for
 * a bit more functionality.
 * 
 * Note: this one has an unsafe life-cycle. Modifications can
 * be made after it is in use.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorsImpl implements Interceptors
{
   private static final Logger log = Logger.getLogger(InterceptorsImpl.class);

   private Set<Class<?>> values = new LinkedHashSet<Class<?>>();
   
   public boolean add(Interceptors annotation)
   {
      if(annotation == null)
         return false;
      boolean result = false;
      for(Class<?> cls : annotation.value())
      {
         result |= values.add(cls);
      }
      return result;
   }
   
   public boolean addValue(Class<?> interceptorClass)
   {
      return values.add(interceptorClass);
   }
   
   public Class<? extends Annotation> annotationType()
   {
      return Interceptors.class;
   }

   public boolean isEmpty()
   {
      return values.isEmpty();
   }

   public Class<?>[] value()
   {
      return values.toArray(new Class<?>[0]);
   }
}
