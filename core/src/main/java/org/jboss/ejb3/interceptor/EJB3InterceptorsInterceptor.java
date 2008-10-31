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
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class EJB3InterceptorsInterceptor implements Interceptor
{
   protected static Logger log = Logger.getLogger(EJB3InterceptorsInterceptor.class);
   private InterceptorInfo[] interceptorInfos;
   private Method[] beanAroundInvokes;

   public EJB3InterceptorsInterceptor(InterceptorInfo[] interceptorInfos, Method[] beanAroundInvokes)
   {
      this.interceptorInfos = interceptorInfos;
      this.beanAroundInvokes = beanAroundInvokes;
   }

   public String getName()
   {
      return getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      EJBContainerInvocation inv = (EJBContainerInvocation) invocation;
                                
      //We need to do this every time to make sure we have interceptor instances corresponding 
      //to the bean instance
      //TODO Cache this for non-stateful beans?
      Object[] interceptors = inv.getBeanContext().getInterceptorInstances(interceptorInfos);
      if (interceptors != null && interceptors.length == 0 && beanAroundInvokes != null && beanAroundInvokes.length == 0) return invocation.invokeNext();
      InvocationContextImpl ctx = new InvocationContextImpl(inv, interceptorInfos, interceptors, beanAroundInvokes);
      return ctx.proceed();
   }

}
