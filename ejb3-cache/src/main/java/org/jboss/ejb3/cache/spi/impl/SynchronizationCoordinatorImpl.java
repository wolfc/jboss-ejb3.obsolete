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

package org.jboss.ejb3.cache.spi.impl;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;
import org.jboss.logging.Logger;

/**
 * @author Brian Stansberry
 *
 */
public class SynchronizationCoordinatorImpl implements SynchronizationCoordinator
{
   private static final Logger log = Logger.getLogger(SynchronizationCoordinatorImpl.class);
   
   private ConcurrentMap<Transaction, OrderedSynchronizationHandler> handlers = 
      new ConcurrentHashMap<Transaction, OrderedSynchronizationHandler>();
   
   public void addSynchronizationFirst(Transaction tx, Synchronization sync) 
      throws RollbackException, SystemException
   {      
      getHandler(tx).registerAtHead(sync);
   }

   public void addSynchronizationLast(Transaction tx, Synchronization sync) 
      throws RollbackException, SystemException
   {
      getHandler(tx).registerAtTail(sync);
   }
   
   private OrderedSynchronizationHandler getHandler(Transaction tx) throws RollbackException, SystemException
   {
      OrderedSynchronizationHandler handler = handlers.get(tx);
      if (handler == null)
      {
         handler = new OrderedSynchronizationHandler(tx, this);
         OrderedSynchronizationHandler old = handlers.putIfAbsent(tx, handler);
         if (old != null)
         {
            handler = old;
         }
         else
         {
            tx.registerSynchronization(handler);
         }
      }
      
      return handler;      
   }
   
   private void removeHandler(Transaction tx)
   {
      handlers.remove(tx);
   }

   private static class OrderedSynchronizationHandler implements Synchronization
   {       
      private Transaction tx = null;
      private SynchronizationCoordinatorImpl coordinator;
      private final LinkedList<Synchronization> synchronizations = new LinkedList<Synchronization>();


      private OrderedSynchronizationHandler(Transaction tx, SynchronizationCoordinatorImpl coordinator)
      {
         assert tx != null : "tx is null";
         assert coordinator != null : "coordinator is null";
         
         this.tx = tx;
         this.coordinator = coordinator;
      }


      public void registerAtHead(Synchronization handler)
      {
         register(handler, true);
      }

      public void registerAtTail(Synchronization handler)
      {
         register(handler, false);
      }

      void register(Synchronization handler, boolean head)
      {
         if (handler != null && !synchronizations.contains(handler))
         {
            if (head)
               synchronizations.addFirst(handler);
            else
               synchronizations.addLast(handler);
         }
      }

      public void beforeCompletion()
      {
         for (Synchronization sync : synchronizations)
         {
            sync.beforeCompletion();
         }
      }

      public void afterCompletion(int status)
      {
         RuntimeException exceptionInAfterCompletion = null;
         for (Synchronization sync : synchronizations)
         {
            try
            {
               sync.afterCompletion(status);
            }
            catch (Throwable t)
            {
               log.error("failed calling afterCompletion() on " + sync, t);
               exceptionInAfterCompletion = (RuntimeException) t;
            }
         }

         // finally unregister us from the hashmap
         coordinator.removeHandler(tx);
         tx = null;

         // throw the exception so the TM can deal with it.
         if (exceptionInAfterCompletion != null) throw exceptionInAfterCompletion;
      }

      public String toString()
      {
         StringBuffer sb = new StringBuffer();
         sb.append("tx=" + getTxAsString() + ", handlers=" + synchronizations);
         return sb.toString();
      }
      
      private String getTxAsString()
      {
         // Don't call toString() on tx or it can lead to stack overflow
         if (tx == null)
            return null;
         
         return tx.getClass().getName() + "@" + System.identityHashCode(tx);
      }
      
   }
}
