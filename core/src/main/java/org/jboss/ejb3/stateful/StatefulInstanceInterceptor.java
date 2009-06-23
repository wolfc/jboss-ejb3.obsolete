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
package org.jboss.ejb3.stateful;

import java.rmi.RemoteException;

import javax.ejb.ConcurrentAccessException;
import javax.ejb.EJBException;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.annotation.SerializedConcurrentAccess;
import org.jboss.ejb3.aop.AbstractInterceptor;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulInstanceInterceptor extends AbstractInterceptor
{
   private static final Logger log = Logger.getLogger(StatefulInstanceInterceptor.class);
   
   public StatefulInstanceInterceptor()
   {
   }

   public String getName()
   {
      return null;
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      StatefulContainerInvocation ejb = (StatefulContainerInvocation) invocation;
      Object id = ejb.getId();
      StatefulContainer container = getEJBContainer(invocation);
      StatefulBeanContext target = container.getCache().get(id);

      boolean block = container.getAnnotation(SerializedConcurrentAccess.class) != null;
      
      if (block)
      {
         target.getLock().lockInterruptibly();
      }
      else
      {
         synchronized (target)
         {
            if (target.isInInvocation()) throw new ConcurrentAccessException("no concurrent calls on stateful bean '" + container.getName() + "' (EJB3 4.3.13)");
            target.setInInvocation(true);
         }
      }
      ejb.setBeanContext(target);
      StatefulBeanContext.currentBean.push(target);
      container.pushContext(target);
      try
      {
         if (target.isDiscarded()) throw new EJBException("SFSB was discarded by another thread");
         return ejb.invokeNext();
      }
      catch (Exception ex)
      {
         if (StatefulRemoveInterceptor.isApplicationException(ex, (MethodInvocation)invocation)) throw ex;
         if (ex instanceof RuntimeException
                 || ex instanceof RemoteException)
         {
            if(log.isTraceEnabled())
               log.trace("Removing bean " + id + " because of exception", ex);
            container.getCache().remove(id);
            target.setDiscarded(true);
         }
         throw ex;
      }
      finally
      {
         container.popContext();
         StatefulBeanContext.currentBean.pop();
         // FIXME: the bean context should really be disaccociated, but then the StatefulRemoveInterceptor will fail
         //ejb.setBeanContext(null);
         synchronized (target)
         {
            target.setInInvocation(false);
            // nobody attached the bean to the tx and nobody discarded it
            if (!target.isTxSynchronized() && !target.isDiscarded()) container.getCache().release(target);
            if (block) target.getLock().unlock();
         }
      }
   }
}
