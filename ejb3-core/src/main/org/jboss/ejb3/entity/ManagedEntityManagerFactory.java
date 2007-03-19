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
package org.jboss.ejb3.entity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;
import javax.persistence.TransactionRequiredException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import org.jboss.logging.Logger;
import org.jboss.tm.TransactionLocal;
import org.jboss.tm.TxManager;
import org.jboss.tm.TxUtils;
import org.jboss.ejb3.ThreadLocalStack;
import org.jboss.ejb3.tx.TxUtil;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:gavine@hibernate.org">Gavin King</a>
 * @version $Revision$
 */
public class ManagedEntityManagerFactory
{
   private static final Logger log = Logger.getLogger(ManagedEntityManagerFactory.class);

   protected EntityManagerFactory entityManagerFactory;
   protected TransactionLocal session = new TransactionLocal(TxUtil.getTransactionManager());
   protected String kernelName;

   public static ThreadLocalStack<Map> nonTxStack = new ThreadLocalStack<Map>();

   public EntityManager getNonTxEntityManager()
   {
      Map map = nonTxStack.get();
      EntityManager em = (EntityManager)map.get(this);
      if (em == null)
      {
         em = entityManagerFactory.createEntityManager();
         map.put(this, em);
      }
      return em;
   }

   public ManagedEntityManagerFactory(EntityManagerFactory sf, String kernelName)
   {
      this.entityManagerFactory = sf;
      this.kernelName = kernelName;
   }

   public EntityManagerFactory getEntityManagerFactory()
   {
      return entityManagerFactory;
   }

   public String getKernelName()
   {
      return kernelName;
   }

   public void destroy()
   {
      entityManagerFactory.close();
   }

   private static class SessionSynchronization implements Synchronization
   {
      private EntityManager manager;
      private Transaction tx;
      private boolean closeAtTxCompletion;

      public SessionSynchronization(EntityManager session, Transaction tx, boolean close)
      {
         this.manager = session;
         this.tx = tx;
         closeAtTxCompletion = close;
      }

      public void beforeCompletion()
      {
         /*  IF THIS GETS REACTIVATED THEN YOU MUST remove the if(closeAtTxCompletion) block in getSession()
         try
         {
            int status = tx.getStatus();
            if (status != Status.STATUS_ROLLEDBACK && status != Status.STATUS_ROLLING_BACK && status != Status.STATUS_MARKED_ROLLBACK)
            {
               if (FlushModeInterceptor.getTxFlushMode() != FlushModeType.NEVER)
               {
                  log.debug("************** flushing.....");
                  manager.flush();
               }
            }
         }
         catch (SystemException e)
         {
            throw new RuntimeException(e);
         }
         */
      }

      public void afterCompletion(int status)
      {
         if (closeAtTxCompletion)
         {
            log.debug("************** closing entity managersession **************");
            manager.close();
         }
      }
   }

   public static ThreadLocal longLivedSession = new ThreadLocal();

   public TransactionLocal getTransactionSession()
   {
      return session;
   }

   public void registerExtendedWithTransaction(EntityManager pc)
   {
      pc.joinTransaction();
      session.set(pc);
   }

   public void verifyInTx()
   {
      Transaction tx = session.getTransaction();
      if (tx == null || !TxUtils.isActive(tx)) throw new TransactionRequiredException("EntityManager must be access within a transaction");
      if (!TxUtils.isActive(tx))
         throw new TransactionRequiredException("Transaction must be active to access EntityManager");
   }
   public boolean isInTx()
   {
      Transaction tx = session.getTransaction();
      if (tx == null || !TxUtils.isActive(tx)) return false;
      return true;
   }

   public EntityManager getTransactionScopedEntityManager()
   {
      Transaction tx = session.getTransaction();
      if (tx == null || !TxUtils.isActive(tx)) return getNonTxEntityManager();

      EntityManager rtnSession = (EntityManager) session.get();
      if (rtnSession == null)
      {
         rtnSession = createEntityManager();
         try
         {
            tx.registerSynchronization(new SessionSynchronization(rtnSession, tx, true));
         }
         catch (RollbackException e)
         {
            throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
         }
         catch (SystemException e)
         {
            throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
         }
         session.set(rtnSession);
         rtnSession.joinTransaction(); // force registration with TX
      }
      return rtnSession;
   }

   public EntityManager createEntityManager()
   {
      return entityManagerFactory.createEntityManager();
   }


}
