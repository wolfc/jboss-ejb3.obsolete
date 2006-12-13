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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Processes instances by injecting their properties and calling their postconstructs.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InjectorProcessor
{
   public static void process(Object instance, Collection<Injector> injectors, Collection<Method> postConstructs)
   {
      assert instance != null;
      assert injectors != null;
      assert postConstructs != null : "postConstructs is null";
      
      for(Injector injector : injectors)
      {
         injector.inject(instance);
      }
      
      Class<?> cls = instance.getClass();
      for(Method method : postConstructs)
      {
         Object obj;
         if(cls.isAssignableFrom(method.getDeclaringClass()))
         {
            obj = instance;
         }
         else
         {
            try
            {
               // TODO: this is probably a bad idea
               obj = method.getDeclaringClass().newInstance();
            }
            catch (InstantiationException e)
            {
               throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
               throw new RuntimeException(e);
            }
         }
         Object args[] = null;
         method.setAccessible(true);
         try
         {
            method.invoke(obj, args);
         }
         catch (IllegalAccessException e)
         {
            // should not happen
            throw new RuntimeException(e);
         }
         catch (InvocationTargetException e)
         {
            Throwable t = e.getCause();
            if(t instanceof Error)
               throw (Error) t;
            if(t instanceof RuntimeException)
               throw (RuntimeException) t;
            throw new RuntimeException(t);
         }
      }
   }
}
