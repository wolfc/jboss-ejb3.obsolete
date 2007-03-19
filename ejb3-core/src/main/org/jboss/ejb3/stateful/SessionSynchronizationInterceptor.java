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
import javax.ejb.EJBException;
import javax.ejb.SessionSynchronization;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.tx.TxUtil;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class SessionSynchronizationInterceptor implements Interceptor
{
   private TransactionManager tm;

   public SessionSynchronizationInterceptor()
   {
      this.tm = TxUtil.getTransactionManager();
   }

   public String getName()
   {
      return null;
   }

   protected static class SFSBSessionSynchronization implements Synchronization
   {
      private StatefulBeanContext ctx;

      public SFSBSessionSynchronization(StatefulBeanContext ctx)
      {
         this.ctx = ctx;
      }

      public void beforeCompletion()
      {
         SessionSynchronization bean = (SessionSynchronization) ctx.getInstance();
         try
         {
            bean.beforeCompletion();
         }
         catch (RemoteException e)
         {
            throw new RuntimeException(e);
         }
      }

      public void afterCompletion(int status)
      {
         ctx.setTxSynchronized(false);
         SessionSynchronization bean = (SessionSynchronization) ctx.getInstance();
         try
         {
            if (status == Status.STATUS_COMMITTED)
            {
               bean.afterCompletion(true);
            }
            else
            {
               bean.afterCompletion(false);
            }
         }
         catch (RemoteException ignore)
         {
         }
         finally
         {
            StatefulContainer container = (StatefulContainer) ctx.getContainer();
            container.getCache().finished(ctx);
         }
      }
   }

   protected void registerSessionSynchronization(StatefulBeanContext ctx) throws SystemException
   {
      if (ctx.isTxSynchronized()) return;
      Transaction tx = tm.getTransaction();
      if (tx == null) return;
      if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) return;
      SFSBSessionSynchronization synch = new SFSBSessionSynchronization(ctx);
      SessionSynchronization bean = (SessionSynchronization) ctx.getInstance();
      try
      {
         bean.afterBegin();
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (RemoteException e)
      {
         throw new EJBException(e);
      }
      try
      {
         tx.registerSynchronization(synch);
      }
      catch (RollbackException ignore)
      {
      }
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      StatefulContainerInvocation ejb = (StatefulContainerInvocation) invocation;
      StatefulBeanContext target = (StatefulBeanContext) ejb.getBeanContext();
      if (target.getInstance() instanceof SessionSynchronization)
      {
         registerSessionSynchronization(target);
      }
      return ejb.invokeNext();
   }
}
