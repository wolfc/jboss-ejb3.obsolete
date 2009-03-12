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
package org.jboss.ejb3.singleton;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.aop.AbstractInterceptor;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SingletonInstanceInterceptor extends AbstractInterceptor
{
   /* (non-Javadoc)
    * @see org.jboss.aop.advice.Interceptor#invoke(org.jboss.aop.joinpoint.Invocation)
    */
   @Override
   public Object invoke(Invocation invocation) throws Throwable
   {
      EJBContainerInvocation<SingletonContainer, BeanContext<SingletonContainer>> ejb = (EJBContainerInvocation<SingletonContainer, BeanContext<SingletonContainer>>) invocation;
      SingletonContainer container = getEJBContainer(invocation);
      BeanContext<SingletonContainer> ctx = container.getInstance();
      
      assert ctx != null : "no bean instance";
      
      ejb.setBeanContext(ctx);
      container.pushContext(ctx);
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         container.popContext();
         ejb.setBeanContext(null);
      }
   }
}
