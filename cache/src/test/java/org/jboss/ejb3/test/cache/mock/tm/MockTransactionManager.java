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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Hashtable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.ejb3.cache.spi.impl.TransactionSynchronizationRegistrySource;
import org.jboss.logging.Logger;

/**
 * Variant of SimpleJtaTransactionManagerImpl that doesn't use a VM-singleton,
 * but rather a set of impls keyed by a node id.
 *
 * @author Brian Stansberry
 */
public class MockTransactionManager implements TransactionManager, TransactionSynchronizationRegistrySource, Serializable 
{
   public static final String DEFAULT = "default";
   
   private static final Logger log = Logger.getLogger(MockTransactionManager.class);
   
   private static final Hashtable<String, MockTransactionManager> INSTANCES = new Hashtable<String, MockTransactionManager>();

   private ThreadLocal<MockTransaction> currentTransaction = new ThreadLocal<MockTransaction>();
   private String nodeId;

   public synchronized static MockTransactionManager getInstance()
   {
      return getInstance(DEFAULT);
   }
   
   public synchronized static MockTransactionManager getInstance(String nodeId)
   {
      MockTransactionManager tm = (MockTransactionManager) INSTANCES.get(nodeId);
      if (tm == null)
      {
         tm = new MockTransactionManager(nodeId);
         INSTANCES.put(nodeId, tm);
      }
      return tm;
   }

   public synchronized static void cleanupTransactions()
   {
      for (MockTransactionManager tm : INSTANCES.values())
      {
         try
         {
            tm.currentTransaction.remove();
         }
         catch (Exception e)
         {
            log.error("Exception cleaning up TransactionManager " + tm);
         }
      }
   }

   public synchronized static void cleanupTransactionManagers()
   {
      INSTANCES.clear();
   }

   private MockTransactionManager(String nodeId)
   {
      this.nodeId = nodeId;
   }

   public int getStatus() throws SystemException
   {
      return currentTransaction.get() == null ? Status.STATUS_NO_TRANSACTION : currentTransaction.get().getStatus();
   }

   public Transaction getTransaction() throws SystemException
   {
      return currentTransaction.get();
   }

   public MockTransaction getCurrentTransaction()
   {
      return currentTransaction.get();
   }

   public void begin() throws NotSupportedException, SystemException
   {
      currentTransaction.set(new MockTransaction(this));
   }

   public Transaction suspend() throws SystemException
   {
      log
            .trace(nodeId + ": Suspending " + currentTransaction.get() + " for thread "
                  + Thread.currentThread().getName());
      MockTransaction suspended = currentTransaction.get();
      currentTransaction.set(null);
      return suspended;
   }

   public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException,
         SystemException
   {
      currentTransaction.set((MockTransaction) transaction);
      log.trace(nodeId + ": Resumed " + transaction + " for thread " + Thread.currentThread().getName());
   }

   public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
         SecurityException, IllegalStateException, SystemException
   {
      if (currentTransaction.get() == null)
      {
         throw new IllegalStateException("no current transaction to commit");
      }
      currentTransaction.get().commit();
   }

   public void rollback() throws IllegalStateException, SecurityException, SystemException
   {
      if (currentTransaction.get() == null)
      {
         throw new IllegalStateException("no current transaction");
      }
      currentTransaction.get().rollback();
   }

   public void setRollbackOnly() throws IllegalStateException, SystemException
   {
      if (currentTransaction.get() == null)
      {
         throw new IllegalStateException("no current transaction");
      }
      currentTransaction.get().setRollbackOnly();
   }

   public void setTransactionTimeout(int i) throws SystemException
   {
   }
   
   public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
   {
      if (currentTransaction.get() == null)
      {
         throw new IllegalStateException("no current transaction");
      }
      return currentTransaction.get().getTransactionSynchronizationRegistry();
      
   }

   void endCurrent(MockTransaction transaction)
   {
      if (transaction == currentTransaction.get())
      {
         currentTransaction.set(null);
      }
   }

   public String toString()
   {
      StringBuffer sb = new StringBuffer(getClass().getName());
      sb.append("[nodeId=");
      sb.append(nodeId);
      sb.append("]");
      return sb.toString();
   }
   
   /** Hack to allow binding in JNDI */
   private Object writeReplace() throws ObjectStreamException
   {
      return new Serializer(nodeId);
   }
   
   /** Hack to allow binding in JNDI */
   static class Serializer implements Serializable
   {
      private static final long serialVersionUID = -608936399074867086L;
      
      private final String nodeId;
      
      Serializer(String nodeId)
      {
         this.nodeId = nodeId;
      }
      
      private Object readResolve() throws ObjectStreamException
      {
         return getInstance(nodeId);
      }
   }


   
   
}
