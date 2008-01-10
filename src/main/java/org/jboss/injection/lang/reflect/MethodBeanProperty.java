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
package org.jboss.injection.lang.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.logging.Logger;

/**
 * Morphs a setter method into a bean property.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MethodBeanProperty extends AbstractAccessibleObjectBeanProperty<Method>
{
   private final Logger log = Logger.getLogger(MethodBeanProperty.class);
   
   private String name = null; // lazily initialized
   
   /**
    * @param method
    */
   public MethodBeanProperty(Method method)
   {
      super(method);
      
      assert method.getReturnType() == Void.TYPE;
      assert method.getParameterTypes().length == 1;
      assert method.getName().startsWith("set");
   }

   public Class<?> getDeclaringClass()
   {
      return getMethod().getDeclaringClass();
   }
   
   public String getName()
   {
      if(name == null)
      {
         String name = getMethod().getName().substring(3);
         if (name.length() > 1)
         {
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
         }
         else
         {
            name = name.toLowerCase();
         }
         this.name = name; // atomair, no synch
      }
      return name;
   }
   
   protected Method getMethod()
   {
      return getAccessibleObject();
   }
   
   public Class<?> getType()
   {
      return getMethod().getParameterTypes()[0];
   }
   
   /* (non-Javadoc)
    * @see org.jboss.injection.lang.reflect.BeanProperty#set(java.lang.Object, java.lang.Object)
    */
   public void set(Object instance, Object value)
   {
      Method method = getMethod();
      Object args[] = { value };
      try
      {
         method.invoke(instance, args);
      }
      catch (IllegalAccessException e)
      {
         log.fatal("illegal access on method " + method, e);
         throw new RuntimeException(e);
      }
      catch (IllegalArgumentException e)
      {
         String msg = "failed to set value " + value + " with setter " + method;
         log.error(msg, e);
         throw new IllegalArgumentException(msg);
      }
      catch (InvocationTargetException e)
      {
         Throwable cause = e.getCause();
         if(cause instanceof Error)
            throw (Error) cause;
         if(cause instanceof RuntimeException)
            throw (RuntimeException) cause;
         throw new RuntimeException(cause);
      }
   }

}
