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
package org.jboss.ejb3.interceptors.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.interceptor.InvocationContext;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EJB3InterceptorInterceptor implements Interceptor
{
   private static final Class<?> PARAMETER_TYPES[] = { InvocationContext.class };
   
   private Class<?> interceptorClass;
   private Method method;
   
   /**
    * @param interceptorClass
    * @param businessMethodInterceptorMethod    a business method interceptor on the spec interceptor (@AroundInvoke)
    */
   public EJB3InterceptorInterceptor(Class<?> interceptorClass, Method businessMethodInterceptorMethod)
   {
      assert interceptorClass != null : "interceptorClass is null";
      assert businessMethodInterceptorMethod != null : "businessMethodInterceptorMethod is null";
      assert businessMethodInterceptorMethod.getDeclaringClass().isAssignableFrom(interceptorClass) : businessMethodInterceptorMethod + " does not belong to " + interceptorClass;
      assert businessMethodInterceptorMethod.getReturnType() == Object.class : "return type must be Object " + businessMethodInterceptorMethod;
      assert Arrays.equals(businessMethodInterceptorMethod.getParameterTypes(), PARAMETER_TYPES) : "wrong parameter signature";
      // Ignore throw clause
      
      this.interceptorClass = interceptorClass;
      this.method = businessMethodInterceptorMethod;
   }

   public String getName()
   {
      return EJB3InterceptorInterceptor.class.getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      // TODO: speed up
      Object interceptors[] = ContainerMethodInvocation.getContainerMethodInvocation(invocation).getBeanContext().getInterceptors();
      if(interceptors != null)
      {
         for(Object interceptor : interceptors)
         {
            if(interceptor.getClass().equals(interceptorClass))
               return invoke(interceptor, invocation);
         }
      }
      //throw new IllegalStateException("Can't find an interceptor instance for " + interceptorClass + " among " + Arrays.toString(instances));
      // The business method interceptor method interceptor only exists when there is an aroundInvoke
      return invocation.invokeNext();
   }

   private Object invoke(Object interceptor, final Invocation invocation) throws Throwable
   {
      InvocationContext ctx = InvocationContextInterceptor.getInvocationContext(invocation);
      try
      {
         Object args[] = { ctx };
         boolean accessible = method.isAccessible();
         method.setAccessible(true);
         try
         {
            return method.invoke(interceptor, args);
         }
         finally
         {
            method.setAccessible(accessible);
         }
      }
      catch(InvocationTargetException e)
      {
         throw e.getCause();
      }
   }
}
