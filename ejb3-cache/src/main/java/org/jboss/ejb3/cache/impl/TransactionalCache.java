/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ejb.NoSuchEJBException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.SerializationGroup;
import org.jboss.ejb3.cache.spi.BackingCache;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.IntegratedObjectStore;

/**
 * {@link Cache#isGroupAware() Non-group-aware} <code>Cache</code> implementation 
 * that applies transactional access and release semantics. Specifically: 
 * <ol>
 * <li>monitors the transactional status of threads that invoke create() and get()</li>
 * <li>ensures that until the first transaction that accesses a given CacheItem 
 * has completed, any other transaction that attempts to access the item will
 * receive an IllegalStateException</li>
 * <li>Only releases the CacheItem to the underlying {@link IntegratedObjectStore}
 * when the transaction commits or rolls back.</li>
 * </ol>
 * <p>
 * Delegates many functions to a backing {@link BackingCache}.
 * </p>
 * 
 * @author Brian Stansberry
 * @version $Revision: 69058 $
 */
public class TransactionalCache<C extends CacheItem, T extends BackingCacheEntry<C>> implements Cache<C>
{
   /** BackingCache that handles passivation, groups, etc */
   private final BackingCache<C, T> delegate;
   
   /** Cache of items that are in use by a tx or non-transactional invocation */
   private final Map<Object, Entry> inUseCache;
   /** Map of Synchronizations to release items in use by a tx*/
   private final ConcurrentMap<Object, CacheReleaseSynchronization<C, T>> synchronizations;
   /** Our transaction manager */
   private final TransactionManager tm;
   
   private enum State { INITIALIZED, IN_USE, FINISHED };
   private class Entry
   {
      long lastUsed;
      C obj;
      State state;
      
      Entry()
      {
         this.lastUsed = System.currentTimeMillis();
         this.state = State.INITIALIZED;
      }
   }
   
   private static class CacheReleaseSynchronization<C extends CacheItem, T extends BackingCacheEntry<C>> 
      implements Synchronization
   {
      private TransactionalCache<C, T> cache;
      private C cacheItem;
      private Transaction tx;
      
      private CacheReleaseSynchronization(TransactionalCache<C, T> cache,
                                          C cacheItem, Transaction tx)
      {
         assert cache != null : "cache is null";
         assert cacheItem != null : "cacheItem is null";
         assert tx != null : "tx is null";
         
         this.cache = cache;
         this.cacheItem = cacheItem;
         this.tx = tx;
      }
      
      public Transaction getTransaction()
      {
         return tx;
      }
      
      public void beforeCompletion()
      {
         cache.release(cacheItem);         
      }
      
      public void afterCompletion(int arg0)
      {
         cache.releaseSynchronization(cacheItem);
         cache = null;
         cacheItem = null;
         tx = null;         
      }      
   }
   
   public TransactionalCache(BackingCache<C, T> delegate, TransactionManager tm)
   {
      assert delegate != null : "delegate is null";
      assert tm != null : "tm is null";
      
      this.delegate = delegate;
      this.tm = tm;
      
      this.inUseCache = new HashMap<Object, Entry>();
      this.synchronizations = new ConcurrentHashMap<Object, CacheReleaseSynchronization<C, T>>();
   }

   public C create(Class<?>[] initTypes, Object[] initValues)
   {
      T backingEntry = delegate.create(initTypes, initValues);
      C obj = backingEntry.getUnderlyingItem();
      synchronized (inUseCache)
      {
         // Create an entry, but do not store a ref to obj in the entry.
         // This will ensure get() goes to the backingCache to get the
         // missing obj, giving the backingCache an opportunity to mark
         // the BackingCacheEntry as being in use
         registerEntry(obj, false);
      }
      return obj;
   }

   private Entry registerEntry(C obj, boolean storeRef)
   {
      Entry entry = new Entry(); 
      if (storeRef)
         entry.obj = obj;
      inUseCache.put(obj.getId(), entry);      
      registerSynchronization(obj);
      return entry;
   }
   
   public C get(Object key) throws NoSuchEJBException
   {
      Entry entry = null;
      
      // Yuck. This is a bottleneck
      synchronized (inUseCache)
      {
         entry = inUseCache.get(key);
         if(entry == null)
         {
            T backingEntry = delegate.get(key);
            C obj = backingEntry.getUnderlyingItem();
            entry = registerEntry(obj, true); 
         }
      }
      
      if (entry.obj == null)
         entry.obj = delegate.get(key).getUnderlyingItem();
      
      validateTransaction(entry.obj);
      if(entry.state == State.IN_USE)
         throw new IllegalStateException("entry " + key + " is already in use");
      entry.state = State.IN_USE;
      entry.lastUsed = System.currentTimeMillis();
      return entry.obj;
   }
   
   public void finished(C obj)
   {
      Entry entry = null;
      synchronized (inUseCache)
      {
         entry = inUseCache.get(obj.getId());
      }
      if (entry == null)
         throw new IllegalStateException("No entry for " + obj.getId());
      if(entry.state != State.IN_USE)
         throw new IllegalStateException("entry " + obj.getId() + " is not in operation");
      entry.state = State.FINISHED;
      entry.lastUsed = System.currentTimeMillis();
      
      // If there is no tx associated with this object, we can release it
      if (synchronizations.get(obj.getId()) == null)
      {
         release(obj);
      }
      
   }

   public void remove(Object key)
   {
      // Note that the object is not in my cache at this point.
      // FIXME BES 2008/03/10 -- above comment not true!
      delegate.remove(key);
      inUseCache.remove(key);
   }

   public void start()
   {
      delegate.start();
   }

   public void stop()
   {
      delegate.stop();
   }

   public boolean isGroupAware()
   {
      return false;
   }

   public SerializationGroup<C> getGroup(C obj)
   {
      return null;
   }
   
   public BackingCache<C, T> getBackingCache()
   {
      return delegate;
   }

   /**
    * Actually release the object from our delegate 
    * @param obj
    */
   private void release(C obj)
   {
      Object key = obj.getId();
      synchronized (inUseCache)
      {
         Entry entry = inUseCache.get(key);
         if (entry == null)
         {
            // FIXME is this correct?
            return;
         }
         if(entry.state == State.IN_USE)
            throw new IllegalStateException("entry " + key + " is not finished");
         inUseCache.remove(key);
      }
      delegate.release(obj.getId());
   }
   
   private void registerSynchronization(C cacheItem)
   {
      Transaction tx = getCurrentTransaction();
      if (tx != null)
      {
         CacheReleaseSynchronization<C, T> sync = new CacheReleaseSynchronization<C, T>(this, cacheItem, tx);
         try
         {
            tx.registerSynchronization(sync);
         }
         catch (RollbackException e)
         {
            throw new RuntimeException("Failed registering synchronization for " + cacheItem, e);
         }
         catch (SystemException e)
         {
            throw new RuntimeException("Failed registering synchronization for " + cacheItem, e);
         }
         synchronizations.put(cacheItem.getId(), sync);
      }
   }
   
   private Transaction getCurrentTransaction()
   {
      try
      {
         return tm == null ? null : tm.getTransaction();
      }
      catch (SystemException e)
      {
         throw new RuntimeException("Failed getting current transaction from " + tm, e);
      }
   }
   
   private void releaseSynchronization(C cacheItem)
   {
      synchronizations.remove(cacheItem.getId());      
   }
   
   private void validateTransaction(C cacheItem)
   {
      CacheReleaseSynchronization<C, T> sync = synchronizations.get(cacheItem.getId());
      
      if (sync != null)
      {         
         Transaction syncTx = sync.getTransaction();
         if (syncTx != null) // may be null if sync has just completed
         {
            Transaction threadTx = getCurrentTransaction();
            if(!syncTx.equals(threadTx))
            {
               throw new IllegalStateException("Illegal concurrent access to " + cacheItem +
                                               " by two transactions: " + syncTx + " and " + threadTx);
            }
         }
               
      }
   }   
}
