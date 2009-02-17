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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.NoSuchEJBException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCache;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;
import org.jboss.ejb3.cache.spi.impl.GroupCreationContext;
import org.jboss.logging.Logger;

/**
 * Non-group-aware <code>Cache</code> implementation 
 * that applies transactional access and release semantics. Specifically: 
 * <ol>
 * <li>monitors the transactional status of threads that invoke create() and get()</li>
 * <li>ensures that until the first transaction that accesses a given CacheItem 
 * has completed, any other transaction that attempts to access the item will
 * receive an IllegalStateException</li>
 * <li>Only releases the CacheItem to the underlying {@link BackingCache}
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
   protected final Logger log = Logger.getLogger(getClass().getName());
   
   /** BackingCache that handles passivation, groups, etc */
   private final BackingCache<C, T> backingCache;
   
   /** Cache of items that are in use by a tx or non-transactional invocation */
   private final ConcurrentMap<Object, Entry> inUseCache;
   /** Map of Synchronizations to release items in use by a tx*/
   private final ConcurrentMap<Object, CacheReleaseSynchronization<C, T>> synchronizations;
   /** Our transaction manager */
   private final TransactionManager tm;
   /** 
    * Helper to allow coordinated Transaction Synchronization execution
    * between ourself and other elements of the caching subsystem.
    */
   private final SynchronizationCoordinator synchronizationCoordinator;   
   /** 
    * Whether we are strict about enforcing that all items associated with
    * a {@link GroupCreationContext} are consistently group-aware
    */
   private boolean strictGroups;
   
   /** Whether we are in the started state */
   private boolean started;
   
   private class Entry
   {
      C obj;
      ReentrantLock lock;
      int getCount;
      
      Entry()
      {
         this.lock = new ReentrantLock();
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
         cache.releaseItem(cacheItem);         
      }
      
      public void afterCompletion(int arg0)
      {
         cache.releaseSynchronization(cacheItem);
         cache = null;
         cacheItem = null;
         tx = null;         
      }      
   }
   
   public TransactionalCache(BackingCache<C, T> delegate, 
                             TransactionManager tm,
                             SynchronizationCoordinator syncCoordinator,
                             boolean strictGroups)
   {
      assert delegate != null : "backingCache is null";
      assert tm != null : "tm is null";
      assert syncCoordinator != null : "syncCoordinator is null";
      
      this.backingCache = delegate;
      this.tm = tm;
      this.synchronizationCoordinator = syncCoordinator;
      this.strictGroups = strictGroups;
      
      this.inUseCache = new ConcurrentHashMap<Object, Entry>();
      this.synchronizations = new ConcurrentHashMap<Object, CacheReleaseSynchronization<C, T>>();
   }
   
   public boolean getStrictGroups()
   {
      return strictGroups;
   }

   public Object create(Class<?>[] initTypes, Object[] initValues)
   {
      boolean outer = false;
      GroupCreationContext groupContext = GroupCreationContext.getGroupCreationContext();
      if (groupContext != null)
      {
         // There's a nested hierarchy being formed, but we can't participate
         // in a serialization group. If we're configured to object or the
         // group itself is configured to object, we throw an ISE
         if (groupContext.isStrict() 
               || (getStrictGroups() &&  groupContext.getPairs().size() > 0))
         {
            throw new IllegalStateException("Incompatible cache implementations in nested hierarchy");
         }
      }
      else
      {
         GroupCreationContext.startGroupCreationContext(getStrictGroups());
         outer = true;
      }
      
      try
      {
         // We don't participate in a group, so our "sharedState" isn't really shared
         Map<Object, Object> unsharedState = new ConcurrentHashMap<Object, Object>();
         return createInternal(initTypes, initValues, unsharedState).getId();
      }
      finally
      {
         if (outer)
            GroupCreationContext.clearGroupCreationContext();
      }
   }
   
   /**
    * Does the actual item creation.
    */
   protected C createInternal(Class<?>[] initTypes, Object[] initValues, Map<Object, Object> sharedState)
   {
      Entry entry = new Entry(); 
      entry.lock.lock();
      try
      {
         T backingEntry = backingCache.create(initTypes, initValues, sharedState);
         C obj = backingEntry.getUnderlyingItem();
         
         // Note we deliberately don't assign obj to entry -- we want
         // a call to get() to get it from backingCache so backingCache can lock it
         
         Entry old = inUseCache.putIfAbsent(obj.getId(), entry);
         if (old != null)
            throw new IllegalStateException("entry for " + obj.getId() + " already exists");
         registerSynchronization(obj);
         return obj;
      }
      finally
      {
         entry.lock.unlock();
      }
   }
   
   public C get(Object key) throws NoSuchEJBException
   {
      Entry entry = inUseCache.get(key);
      if (entry == null)
      {
         entry = new Entry(); 
         entry.lock.lock();
         Entry old = inUseCache.putIfAbsent(key, entry);
         if (old != null)
         {
            // We've got a race for this key.  See if we won
            if (old.lock.tryLock())
            {
               // We won. Just use "old"; let the one we just created get gc'd
               entry = old;
            }
            else
               throw new IllegalStateException(key + " is already in use");
         }
      }
      else
      {
         if (!entry.lock.tryLock())
            throw new IllegalStateException(key + " is already in use");
      }
         
      try
      {
         if (entry.obj == null)
            entry.obj = backingCache.get(key).getUnderlyingItem();
         
         validateTransaction(entry.obj);
         entry.getCount++;
         return entry.obj;
      }
      catch (RuntimeException e)
      {
         entry.lock.unlock();
         throw e;
      }
   }
   
   public void release(C obj)
   {
      Entry entry = inUseCache.get(obj.getId());
      if (entry == null)
         throw new IllegalStateException("No entry for " + obj.getId());
      
      if(!entry.lock.isHeldByCurrentThread())
      {
         throw new IllegalStateException("entry " + obj.getId() + 
               " lock is not held by " + Thread.currentThread().getName());
      }
      
      try
      {
         if (entry.getCount < 1)
            throw new IllegalStateException("Unmatched calls to finished");         
         entry.getCount--;
         
         // If there is no tx associated with this object, we can release it
         if (entry.getCount == 0 && synchronizations.get(obj.getId()) == null)
         {
            releaseItem(obj);
         }
      }
      finally
      {
         entry.lock.unlock();
      }
      
   }

   public void remove(Object key)
   {
      // Note that the object is not in my cache at this point.
      // FIXME BES 2008/03/10 -- above comment not true!
      backingCache.remove(key);
      inUseCache.remove(key);
   }

   public void start()
   {
      backingCache.start();
      started = true;
   }

   public void stop()
   {
      try
      {
         backingCache.stop();
      }
      finally
      {
         started = false;
      }
   }
   
   public boolean isStarted()
   {
      return started;
   }
   
   public int getAvailableCount()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getCacheSize()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getCreateCount()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getCurrentSize()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getMaxSize()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getPassivatedCount()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getRemoveCount()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getTotalSize()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public BackingCache<C, T> getBackingCache()
   {
      return backingCache;
   }

   /**
    * Actually release the object from our backingCache 
    * @param obj
    */
   private void releaseItem(C obj)
   {
      Object key = obj.getId();
      Entry entry = inUseCache.get(key);
      if (entry == null)
      {
         // TODO is this correct?
         if (log.isTraceEnabled())
            log.trace("Item " + key + " is not in use; cannot release");
         return;
      }
      
      // We either came here from finished(), and we thus hold the lock,
      // or we came from our synchronization's beforeCompletion(), and
      // validateTransaction() will promptly force any other thread
      // that got the lock to give it up. So, we acquire the lock and are
      // willing to wait for it
      try
      {
         entry.lock.lockInterruptibly();
         // For sure we now control this key -- tell backingCache to release
         backingCache.release(obj.getId());
         
         // Now remove the entry
         inUseCache.remove(key);
      }
      catch (InterruptedException ie)
      {
         throw new RuntimeException("Interrupted waiting to lock " + key);
      }

      // Note we don't release the lock!  If anyone has a ref to entry,
      // it's now stale and they should fail to acquire the lock
   }
   
   private void registerSynchronization(C cacheItem)
   {
      Transaction tx = getCurrentTransaction();
      if (tx != null)
      {
         CacheReleaseSynchronization<C, T> sync = new CacheReleaseSynchronization<C, T>(this, cacheItem, tx);
         synchronizationCoordinator.addSynchronizationFirst(sync);
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
      else
      {
         registerSynchronization(cacheItem);
      }
   }   
}
