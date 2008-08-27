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
package org.jboss.ejb3.injection;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationBase;
import org.jboss.ejb3.BeanContext;
import org.jboss.injection.Injector;

/**
 * Perform injection via an interceptor chain.
 * 
 * This should be part of ejb3-interceptors.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InjectionInvocation extends InvocationBase 
   implements org.jboss.ejb3.interceptors.container.InjectionInvocation
{
   private static final long serialVersionUID = 1L;

   private BeanContext<?> ctx;
   private Injector injector;
   
   public InjectionInvocation(BeanContext<?> ctx, Injector injector, Interceptor interceptors[])
   {
      super(interceptors);
      
      assert ctx != null : "ctx is null";
      assert injector != null : "injector is null";
      
      this.ctx = ctx;
      this.injector = injector;
   }
   
   public Invocation copy()
   {
      throw new RuntimeException("NYI");
   }

   public Invocation getWrapper(Interceptor[] newchain)
   {
      throw new RuntimeException("NYI");
   }
   
   @Override
   public Object invokeNext() throws Throwable
   {
      if (interceptors != null && currentInterceptor < interceptors.length)
      {
         try
         {
            return interceptors[currentInterceptor++].invoke(this);
         }
         finally
         {
            // so that interceptors like clustering can reinvoke down the chain
            currentInterceptor--;
         }
      }

      return invokeTarget();
   }
   
   @Override
   public Object invokeTarget() throws Throwable
   {
      injector.inject(ctx);
      return null;
   }
}
