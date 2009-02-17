/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2007, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.jboss.ejb3.test.cache.mock.tm;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * Mock Transaction implementation that works with DualNodeTransactionManagerImpl.
 *
 * @author Brian Stansberry
 */
public class MockTransaction implements Transaction
{
   private static final Logger log = Logger.getLogger(MockTransaction.class);

   private static final AtomicInteger count = new AtomicInteger();
   
   private int status;
   private final String id;

   private LinkedList<Synchronization> synchronizations = new LinkedList<Synchronization>();
   private MockTransactionSynchronizationRegistry syncRegistry; 
   
   private final MockTransactionManager jtaTransactionManager;

   public MockTransaction(MockTransactionManager jtaTransactionManager)
   {
      this.jtaTransactionManager = jtaTransactionManager;
      this.status = Status.STATUS_ACTIVE;
      this.id = "MockTransaction-" + count.incrementAndGet();
   }
   
   public String getId()
   {
      return id;
   }

   public int getStatus()
   {
      return status;
   }

   public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
         IllegalStateException, SystemException
   {

      if (status == Status.STATUS_MARKED_ROLLBACK)
      {
         log.trace("on commit, status was marked for rollback-only");
         rollback();
      }
      else
      {
         status = Status.STATUS_PREPARING;

         for (int i = 0; i < synchronizations.size(); i++)
         {
            Synchronization s = (Synchronization) synchronizations.get(i);
            s.beforeCompletion();
         }

         if (syncRegistry != null)
         {
            for (Synchronization sync : syncRegistry.getSynchronizations())
            {
               sync.beforeCompletion();
            }
         }

         status = Status.STATUS_COMMITTING;

         status = Status.STATUS_COMMITTED;

         if (syncRegistry != null)
         {
            for (Synchronization sync : syncRegistry.getSynchronizations())
            {
               sync.afterCompletion(status);
            }
         }

         for (int i = 0; i < synchronizations.size(); i++)
         {
            Synchronization s = (Synchronization) synchronizations.get(i);
            s.afterCompletion(status);
         }

         //status = Status.STATUS_NO_TRANSACTION;
         jtaTransactionManager.endCurrent(this);
      }
   }

   public void rollback() throws IllegalStateException, SystemException
   {
      status = Status.STATUS_ROLLEDBACK;

      if (syncRegistry != null)
      {
         for (Synchronization sync : syncRegistry.getSynchronizations())
         {
            sync.afterCompletion(status);
         }
      }
      
      if (synchronizations != null)
      {
         for (int i = 0; i < synchronizations.size(); i++)
         {
            Synchronization s = (Synchronization) synchronizations.get(i);
            s.afterCompletion(status);
         }
      }

      //status = Status.STATUS_NO_TRANSACTION;
      jtaTransactionManager.endCurrent(this);
   }

   public void setRollbackOnly() throws IllegalStateException, SystemException
   {
      status = Status.STATUS_MARKED_ROLLBACK;
   }

   public void registerSynchronization(Synchronization synchronization) throws RollbackException,
         IllegalStateException, SystemException
   {
      // todo : find the spec-allowable statuses during which synch can be registered...
      synchronizations.add(synchronization);
   }

   public boolean enlistResource(XAResource xaResource) throws RollbackException, IllegalStateException,
         SystemException
   {
      return false;
   }

   public boolean delistResource(XAResource xaResource, int i) throws IllegalStateException, SystemException
   {
      return false;
   }
   
   public synchronized TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
   {
      if (syncRegistry == null)
      {
         syncRegistry = new MockTransactionSynchronizationRegistry(this);
      }
      return syncRegistry;
   }
}
