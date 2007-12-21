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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;

/**
 * Chapter 12.4
 * 
 * Lifecycle callback interceptor methods defined on a bean class have the following signature:
 * void <METHOD>()
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class LifecycleCallbackBeanMethodInterceptor implements Interceptor
{
   private Object interceptor;
   private Method method;
   
   /**
    * 
    * @param interceptor                an spec interceptor
    * @param lifecycleCallbackMethod    a lifecycle callback on the spec interceptor
    */
   public LifecycleCallbackBeanMethodInterceptor(Object interceptor, Method lifecycleCallbackMethod)
   {
      assert interceptor != null : "interceptor is null";
      assert lifecycleCallbackMethod != null : "lifecycleCallbackMethod is null";
      assert lifecycleCallbackMethod.getReturnType() == Void.TYPE : "return type must be void";
      assert lifecycleCallbackMethod.getParameterTypes().length == 0 : "wrong parameter signature";
      
      this.interceptor = interceptor;
      this.method = lifecycleCallbackMethod;
   }
   
   public String getName()
   {
      return "LifecycleCallbackBeanMethodInterceptor";
   }

   public Object invoke(final Invocation invocation) throws Throwable
   {
      try
      {
         method.invoke(interceptor);
         // TODO: return null or invokeTarget?
         return invocation.invokeNext();
      }
      catch(InvocationTargetException e)
      {
         throw e.getCause();
      }
   }
}
