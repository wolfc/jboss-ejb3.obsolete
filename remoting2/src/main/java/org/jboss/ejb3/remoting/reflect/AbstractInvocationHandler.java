/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.remoting.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class AbstractInvocationHandler implements InvocationHandler
{
   // subclasses might be serializable
   private static final long serialVersionUID = 1L;
   
   private static final Method METHOD_EQUALS;
   private static final Method METHOD_HASH_CODE;
   private static final Method METHOD_TO_STRING;
   
   static
   {
      try
      {
         METHOD_EQUALS = Object.class.getDeclaredMethod("equals", Object.class);
         METHOD_HASH_CODE = Object.class.getDeclaredMethod("hashCode");
         METHOD_TO_STRING = Object.class.getDeclaredMethod("toString");
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   protected abstract Object innerInvoke(Object proxy, Method method, Object[] args) throws Throwable;
   
   public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if(method.equals(METHOD_EQUALS))
         return equals(args[0]);
      if(method.equals(METHOD_HASH_CODE))
         return hashCode();
      if(method.equals(METHOD_TO_STRING))
         return toProxyString();
      return innerInvoke(proxy, method, args);
   }
   
   protected abstract String toProxyString();
}
