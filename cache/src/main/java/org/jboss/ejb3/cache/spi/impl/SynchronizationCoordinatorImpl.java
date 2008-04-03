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

import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;
import org.jboss.logging.Logger;

/**
 * Default implementation of {@link SynchronizationCoordinator}.
 * 
 * @author Brian Stansberry
 */
public class SynchronizationCoordinatorImpl implements SynchronizationCoordinator
{
   private static final Logger log = Logger.getLogger(SynchronizationCoordinatorImpl.class);
   
   private ConcurrentMap<Object, OrderedSynchronizationHandler> handlers = 
      new ConcurrentHashMap<Object, OrderedSynchronizationHandler>();
   
   private TransactionSynchronizationRegistrySource registrySource;
   
   public void addSynchronizationFirst(Synchronization sync)
   {      
      getHandler().registerAtHead(sync);
   }

   public void addSynchronizationLast(Synchronization sync)
   {
      getHandler().registerAtTail(sync);
   }
   
   public void start()
   {
      if (registrySource == null)
      {
         registrySource = new JndiTransactionSynchronizationRegistrySource();
      }
   }
   
   public TransactionSynchronizationRegistrySource getTransactionSynchronizationRegistrySource()
   {
      return registrySource;
   }

   public void setTransactionSynchronizationRegistrySource(TransactionSynchronizationRegistrySource registrySource)
   {
      this.registrySource = registrySource;
   }

   private OrderedSynchronizationHandler getHandler()
   {
      TransactionSynchronizationRegistry syncRegistry = registrySource.getTransactionSynchronizationRegistry();
      Object txId = syncRegistry.getTransactionKey();
      
      OrderedSynchronizationHandler handler = handlers.get(txId);
      if (handler == null)
      {
         handler = new OrderedSynchronizationHandler(txId, this);
         OrderedSynchronizationHandler old = handlers.putIfAbsent(txId, handler);
         if (old != null)
         {
            handler = old;
         }
         else
         {
            syncRegistry.registerInterposedSynchronization(handler);
         }
      }
      
      return handler;      
   }
   
   private void removeHandler(Object txId)
   {
      handlers.remove(txId);
   }

   private static class OrderedSynchronizationHandler implements Synchronization
   {       
      private Object txId = null;
      private SynchronizationCoordinatorImpl coordinator;
      private final LinkedList<Synchronization> synchronizations = new LinkedList<Synchronization>();


      private OrderedSynchronizationHandler(Object txId, SynchronizationCoordinatorImpl coordinator)
      {
         assert txId != null : "txId is null";
         assert coordinator != null : "coordinator is null";
         
         this.txId = txId;
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
         coordinator.removeHandler(txId);
         txId = null;

         // throw the exception so the TM can deal with it.
         if (exceptionInAfterCompletion != null) throw exceptionInAfterCompletion;
      }

      public String toString()
      {
         StringBuffer sb = new StringBuffer();
         sb.append("txId=" + txId + ", handlers=" + synchronizations);
         return sb.toString();
      }
      
   }
}
