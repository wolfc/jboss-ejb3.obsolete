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
package org.jboss.ejb3;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.jboss.aop.DispatcherConnectException;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;

/**
 * An interceptor that blocks container shutdown until the interceptor chain completes
 * and rejects invocations if container is not completely started or is stopped.
 * 
 * @author Paul Ferraro
 */
public class BlockContainerShutdownInterceptor implements Interceptor, Serializable
{
   private static final long serialVersionUID = -1683253088746668495L;
   
   /**
    * @see org.jboss.aop.advice.Interceptor#getName()
    */
   public String getName()
   {
      return this.getClass().getName();
   }

   /**
    * @see org.jboss.aop.advice.Interceptor#invoke(org.jboss.aop.joinpoint.Invocation)
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      EJBContainer container = EJBContainer.getEJBContainer(invocation.getAdvisor());
      
      Lock lock = container.getInvocationLock();
      
      // We intentionally do not use tryLock() since it does not respect lock fairness
      if (!lock.tryLock(0, TimeUnit.SECONDS))
      {
         throw new DispatcherConnectException("EJB container is not completely started, or is stopped.");
      }
      
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         lock.unlock();
      }
   }
}
