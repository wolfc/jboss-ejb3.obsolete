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
import java.util.Arrays;

import javax.interceptor.InvocationContext;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class BusinessMethodBeanMethodInterceptor implements Interceptor
{
   private static final Class<?> PARAMETER_TYPES[] = { InvocationContext.class };
   
   private Method method;
   
   /**
    * 
    * @param beanMethodInterceptorMethod    a bean method interceptor on the bean itself
    */
   public BusinessMethodBeanMethodInterceptor(Method beanMethodInterceptorMethod)
   {
      assert beanMethodInterceptorMethod != null : "businessMethodInterceptorMethod is null";
      assert beanMethodInterceptorMethod.getReturnType() == Object.class : "return type must be Object " + beanMethodInterceptorMethod;
      assert Arrays.equals(beanMethodInterceptorMethod.getParameterTypes(), PARAMETER_TYPES) : "wrong parameter signature";
      // Ignore throw clause
      
      this.method = beanMethodInterceptorMethod;
   }

   public String getName()
   {
      return "BusinessMethodBeanMethodInterceptor";
   }

   public Object invoke(final Invocation invocation) throws Throwable
   {
      final InvocationContext ctx = InvocationContextInterceptor.getInvocationContext(invocation);
      try
      {
         method.setAccessible(true);
         final Object args[] = { ctx };
         return method.invoke(ctx.getTarget(), args);
      }
      catch(InvocationTargetException e)
      {
         throw e.getCause();
      }
   }

   public String toString()
   {
      return method.toString();
   }
}
