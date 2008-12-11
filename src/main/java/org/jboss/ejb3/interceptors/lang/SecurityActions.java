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
package org.jboss.ejb3.interceptors.lang;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Privileged Blocks
 * @author Anil.Saldhana@redhat.com
 * @since Nov 13, 2008
 */
class SecurityActions
{
   static Method[] getDeclaredMethods(final Class<?> cls)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Method[]>() 
      {
         public Method[] run()
         {
            return cls.getDeclaredMethods();
         }
      });
   }
   
   static Method getDeclaredMethod(final Class<?> clazz, final String methodName, final Class<?>[] params)
         throws NoSuchMethodException
   {
      try
      {
         return AccessController.doPrivileged(new PrivilegedExceptionAction<Method>()
         {
            public Method run() throws NoSuchMethodException
            {

               return clazz.getDeclaredMethod(methodName, params);

            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         // Only checked exceptions are wrapped, so this cast should be safe
         throw (NoSuchMethodException) pae.getException();
      }
   }

}