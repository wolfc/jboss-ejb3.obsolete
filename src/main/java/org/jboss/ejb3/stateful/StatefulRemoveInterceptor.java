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

import java.lang.reflect.Method;

import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.aop.AbstractInterceptor;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.logging.Logger;
import org.jboss.tm.TxUtils;

/**
 * Handles @Remove on a Stateful bean.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class StatefulRemoveInterceptor extends AbstractInterceptor
{
   private static final Logger log = Logger.getLogger(StatefulRemoveInterceptor.class);
   protected boolean retainIfException;

   public StatefulRemoveInterceptor(boolean removeOnException)
   {
      this.retainIfException = removeOnException;
   }

   public String getName()
   {
      return this.getClass().getName();
   }

   private static class RemoveSynchronization implements Synchronization
   {
      protected boolean retainIfException;
      private StatefulContainer container;
      private Object id;

      public RemoveSynchronization(StatefulContainer container, Object id, boolean retainIfException)
      {
         this.container = container;
         this.id = id;
         this.retainIfException = retainIfException;
      }


      public void beforeCompletion()
      {
      }

      public void afterCompletion(int status)
      {
         try
         {
            container.getCache().remove(id);
         }
         catch(Throwable t)
         {
            // An exception thrown from afterCompletion is gobbled up
            log.error("Removing bean " + id + " from " + container + " failed", t);
            if(t instanceof Error)
               throw (Error) t;
            if(t instanceof RuntimeException)
               throw (RuntimeException) t;
            throw new RuntimeException(t);
         }
      }
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      Object rtn = null;
      try
      {
         rtn = invocation.invokeNext();
      }
      catch (Throwable t)
      {
         // don't remove if we're an application exception and retain is true
         if (retainIfException(retainIfException, t, (MethodInvocation)invocation)) throw t;

         // otherwise, just remove it.
         removeSession(invocation, true);
         throw t;
      }
      removeSession(invocation, false);
      return rtn;
   }
   
   protected static boolean retainIfException(boolean retainIfException, Throwable t, MethodInvocation invocation)
   {
      if (retainIfException && isApplicationException(t, invocation))
         return true;
      
      return false;
   }
   
   // application exception is @ApplicationException or checked exception extended from Exception or RuntimeException
   public static boolean isApplicationException(Throwable t, MethodInvocation invocation)
   {
      if (TxUtil.getApplicationException(t.getClass(), invocation) != null)
         return true;
      
      Method method = invocation.getMethod();
      Class[] exceptionTypes = method.getExceptionTypes();
      for (Class exceptionClass : exceptionTypes)
      {
         if (exceptionClass.isAssignableFrom(t.getClass()))
            return true;
      }
      
      return false;
   }

   protected void removeSession(Invocation invocation, boolean exceptionThrown) throws Throwable
   {
      StatefulContainerInvocation ejb = (StatefulContainerInvocation) invocation;
      StatefulBeanContext ctx = (StatefulBeanContext)ejb.getBeanContext();
      if (ctx == null || ctx.isDiscarded() || ctx.isRemoved()) return;
      Object id = ejb.getId();

      StatefulContainer container = getEJBContainer(invocation);
      Transaction tx = null;
      try
      {
         tx = TxUtil.getTransactionManager().getTransaction();
      }
      catch (SystemException e)
      {
         throw new RuntimeException(e);
      }
    
      if (tx != null && TxUtils.isActive(tx))
      {
         try
         {
            ctx.registerSynchronization(tx, new RemoveSynchronization(container, id, exceptionThrown != true && retainIfException));
         }
         catch (RollbackException e)
         {
            throw new RuntimeException(e);
         }
         catch (SystemException e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         container.getCache().remove(id);
      }
   }
}
