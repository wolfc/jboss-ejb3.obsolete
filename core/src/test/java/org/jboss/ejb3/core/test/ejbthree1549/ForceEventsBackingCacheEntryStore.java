/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ejb3.core.test.ejbthree1549;

import java.io.Serializable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.impl.backing.SimpleBackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.PersistentObjectStore;
import org.jboss.ejb3.cache.spi.impl.AbstractBackingCacheEntry;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.logging.Logger;

/**
 * @author Brian Stansberry
 *
 */
public class ForceEventsBackingCacheEntryStore<T extends BackingCacheEntry<StatefulBeanContext>> 
   extends SimpleBackingCacheEntryStore<StatefulBeanContext, T>
{
   private static final Object START_PASSIVATION_LOCK = new Object();
   
   private static volatile boolean passivationForced = false;
   
   private static final Logger log = Logger.getLogger(ForceEventsBackingCacheEntryStore.class);
   
   /**
    * Shared barrier between the Cache and the test so that 
    * the test may block until passivation is completed
    */
   public static final CyclicBarrier POST_PASSIVATE_BARRIER = new CyclicBarrier(2);

   public static final CyclicBarrier PRE_PASSIVATE_BARRIER = new CyclicBarrier(2);

   /**
    * Public barrier for removal to block until both test and removal tasks are ready
    */
   public static final CyclicBarrier PRE_REMOVE_BARRIER = new CyclicBarrier(2);
   
   private boolean blockOnExpiration;
   private boolean blockOnPassivation;
   
   /**
    * Create a new SimpleIntegratedObjectStore.
    */
   public ForceEventsBackingCacheEntryStore(PersistentObjectStore<T> store, 
                                                 CacheConfig config,
                                                 String name,
                                                 boolean forGroups)
   {
      super(store, config, name, forGroups);  
   }
   
   public static void triggerPassivationExpiration()
   {
      // Get a lock
      log.info("Awaiting lock to force passivation");
      synchronized (START_PASSIVATION_LOCK)
      {
         passivationForced = true;
         // Notify that passivation should run
         log.info("Notifying passivation via manual force...");
         START_PASSIVATION_LOCK.notifyAll();
      }
      
   }

   @Override
   public void processPassivationExpiration()
   {
      // Get a lock on our monitor
      synchronized (START_PASSIVATION_LOCK)
      {
         if (!passivationForced)
         {
            // Wait until we're signaled
            log.info("Waiting to be notified to run passivation...");
            try
            {
               START_PASSIVATION_LOCK.wait();
            }
            catch (InterruptedException e)
            {
               log.error(e);
               return;
            }
         }
         passivationForced = false;
      }
      
      super.processPassivationExpiration();
   }



   public boolean isBlockOnExpiration()
   {
      return blockOnExpiration;
   }



   public void setBlockOnExpiration(boolean blockOnExpiration)
   {
      this.blockOnExpiration = blockOnExpiration;
   }



   public boolean isBlockOnPassivation()
   {
      return blockOnPassivation;
   }



   public void setBlockOnPassivation(boolean blockOnPassivation)
   {
      this.blockOnPassivation = blockOnPassivation;
   }



   /**
    * Manually sets the session with the specified sessionId
    * past expiry for passivation
    * 
    * @param sessionId
    */
   public void makeSessionEligibleForPassivation(Serializable sessionId)
   {
      this.setSessionLastUsedPastTimeout(sessionId, this.getIdleTimeSeconds());
   }

   /**
    * Manually sets the session with the specified sessionId
    * past expiry for removal
    * 
    * @param sessionId
    */
   public void makeSessionEligibleForRemoval(Serializable sessionId)
   {
      this.setSessionLastUsedPastTimeout(sessionId, this.getExpirationTimeSeconds());
   }

   /**
    * Expose as public for unit test.
    */
   @Override
   public boolean containsInMemoryEntry(Object key)
   {
      return super.containsInMemoryEntry(key);
   }

   /**
    * Expose as public for unit test.
    */
   @Override
   public void clear()
   {
      super.clear();
      
      this.blockOnExpiration = false;
      this.blockOnPassivation = false;
   }

   @Override
   protected void expirationCompleted()
   {
      super.expirationCompleted();
   }

   @Override
   protected void passivationCompleted()
   {
      // Call super
      super.passivationCompleted();

      if (blockOnPassivation)
      {
         // Tell the barrier we've arrived
         try
         {
            log.info("Waiting on the post-passivate barrier...");
            POST_PASSIVATE_BARRIER.await();
         }
         catch (InterruptedException e)
         {
            throw new RuntimeException("Post Passivate prematurely interrupted", e);
         }
         catch (BrokenBarrierException e)
         {
            throw new RuntimeException("Post Passivate prematurely broken", e);
         }
         finally
         {
            // Reset the barrier
            log.info("Post-passivate of PM is done, resetting the barrier");
            POST_PASSIVATE_BARRIER.reset();
         }
      }
   }

   @Override
   protected void preExpirationCompleted()
   {
      if (blockOnExpiration)
      {
         // Block until the barrier is cleared
         try
         {
            PRE_REMOVE_BARRIER.await();
         }
         catch (InterruptedException e)
         {
            throw new RuntimeException(e);
         }
         catch (BrokenBarrierException e)
         {
            throw new RuntimeException(e);
         }
      }
      
      // Invoke super implementation
      super.preExpirationCompleted();
   }

   @Override
   protected void prePassivationCompleted()
   {
      super.prePassivationCompleted();

      if (blockOnPassivation)
      {
         try
         {
            PRE_PASSIVATE_BARRIER.await();
         }
         catch (BrokenBarrierException e)
         {
            throw new RuntimeException("PRE_PASSIVATE_BARRIER prematurely broken", e);
         }
         catch (InterruptedException e)
         {
            throw new RuntimeException(e);
         }
      }
   }
   /**
    * Obtains a time in the past further away than the specified timeout value,
    * expressed in milliseconds since the epoch (per contract of System.currentTimeMillis()
    * 
    * @param timeoutValue
    * @return
    */
   private long getExpiredTime(long timeoutValue)
   {
      long now = System.currentTimeMillis();
      return (now - (timeoutValue * 1000)) - 1;
   }

   /**
    * Marks the session with the specified ID as last used past the 
    * specified timeout period
    * 
    * @param sessionId
    * @param timeout
    */
   private void setSessionLastUsedPastTimeout(Serializable sessionId, long timeout)
   {
      // Find the session
      @SuppressWarnings("unchecked")
      AbstractBackingCacheEntry entry = (AbstractBackingCacheEntry) getBackingCacheEntry(sessionId);

      // Synchronize on the session
      synchronized (entry)
      {
         // Manually set past expiry
         entry.setLastUsed(this.getExpiredTime(timeout));
      }
   }
   
   

   
   
}
