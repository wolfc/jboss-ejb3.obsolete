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
package org.jboss.ejb3.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

/**
 * A repository of interceptor details shared amongst all containers in this deployment.
 * Interceptors differ from other ejb 3 artifacts in that we can have annotations on the
 * interceptor classes which are not the ejb container, so we cannot use annotation overrides
 * on the interceptors themselves.<BR/>
 * <BR/>
 * The xml structures get added on deployment.<BR/>
 * Interceptors only declared by using @Interceptors on the bean class get added on demand.<BR/>
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 * @deprecated  use the new interceptors component
 */
@Deprecated
public class InterceptorInfoRepository
{
   private static Logger log = Logger.getLogger(InterceptorInfoRepository.class);

   public InterceptorInfoRepository(ClassLoader classLoader)
   {
      log.warn("EJBTHREE-1852: InterceptorInfoRepository is deprecated");
   }
   
   private static boolean checkExceptions(Class<?> allowedExceptions[], Method method)
   {
      for(Class<?> exception : method.getExceptionTypes())
      {
         boolean isAllowed = false;
         for(Class<?> allowed : allowedExceptions)
         {
            if(allowed.isAssignableFrom(exception))
               isAllowed = true;
         }
         if(!isAllowed)
         {
            log.warn("Illegal exception '" + exception.getName() + "' in lifecycle signature (EJB3 12.4.2): " + method);
            return false;
         }
      }
      return true;
   }
   
   /**
    * EJB3 12.4
    * Lifecycle methods may throw runtime exceptions, but not application exceptions.
    * Note that for 2.1 beans CreateException (on ejbCreate) and RemoteException should pass.
    * 
    * @param method
    * @return
    */
   public static boolean checkValidBeanLifecycleSignature(Method method)
   {
      int modifiers = method.getModifiers();
      if (method.getName().equals("ejbCreate"))
      {
         // for public void ejbCreate(...) throws javax.ejb.CreateException
         if (!Modifier.isStatic(modifiers) && method.getReturnType().equals(Void.TYPE)
               && method.getExceptionTypes().length <= 1)
         {
            if(!checkExceptions(new Class<?>[] { RuntimeException.class, CreateException.class, RemoteException.class }, method))
               return false;
            return true;
         }
      }
      else if (!Modifier.isStatic(modifiers) && method.getReturnType().equals(Void.TYPE)
            && method.getParameterTypes().length == 0)
      {
         if(!checkExceptions(new Class<?>[] { RuntimeException.class, RemoteException.class }, method))
            return false;
         return true;
      }
      return false;
   }

   public static boolean checkValidBusinessSignature(Method method)
   {
      int modifiers = method.getModifiers();

      if (!Modifier.isStatic(modifiers))
      {
         if (method.getReturnType().equals(Object.class))
         {
            Class[] params = method.getParameterTypes();
            if (params.length == 1 && params[0].equals(InvocationContext.class))
            {
               Class[] exceptions = method.getExceptionTypes();
               if (exceptions.length == 1 && exceptions[0].equals(Exception.class))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   public static boolean checkValidLifecycleSignature(Method method)
   {
      int modifiers = method.getModifiers();

      if (!Modifier.isStatic(modifiers))
      {
         if (method.getReturnType().equals(Void.TYPE))
         {
            Class[] params = method.getParameterTypes();
            if (params.length == 1 && params[0].equals(InvocationContext.class))
            {
               return true;
            }
         }
      }
      return false;
   }
}
