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
package org.jboss.ejb3.core.test.ejbthree1549;

import java.io.Serializable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.jboss.ejb3.cache.simple.SimpleStatefulCache;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.logging.Logger;

/**
 * ForceEventsCache
 * 
 * An extension of the SimpleStatefulCache which provides for
 * forcing the Passivation and Removal tasks to run, also supplanting
 * barriers and locks throughout the tasks' lifecycle callbacks
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ForceEventsCache extends SimpleStatefulCache
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ForceEventsCache.class);

   /**
    * Shared barrier between the Cache and the test so that 
    * the test may block until passivation is completed
    */
   public static final CyclicBarrier POST_PASSIVATE_BARRIER = new CyclicBarrier(2);

   /**
    * Internal lock used to manually block the passivation task from running
    */
   private static final Object START_PASSIVATION_LOCK = new Object();

   private static volatile boolean passivationForced = false;

   /**
    * Internal lock used to manually block the removal task from running
    */
   private static final Object START_REMOVAL_LOCK = new Object();

   /**
    * Flag that removal has been forced
    */
   private static volatile boolean removalForced = false;

   public static final CyclicBarrier PRE_PASSIVATE_BARRIER = new CyclicBarrier(2);

   /**
    * Public barrier for removal to block until both test and removal tasks are ready
    */
   public static final CyclicBarrier PRE_REMOVE_BARRIER = new CyclicBarrier(2);

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public void clear()
   {
      cacheMap.clear();
   }

   /**
    * Triggers passivation to run
    */
   public static void forcePassivation()
   {
      // Get a lock
      log.info("Awaiting lock to force passivation");
      synchronized (START_PASSIVATION_LOCK)
      {
         passivationForced = true;
         // Notify that passivation should run
         log.info("Notifying passivation via manual force...");
         START_PASSIVATION_LOCK.notify();
      }
   }

   /**
    * Triggers removal to run
    */
   public static void forceRemoval()
   {
      // Get a lock
      log.info("Awaiting lock to force removal");
      synchronized (START_REMOVAL_LOCK)
      {
         removalForced = true;
         // Notify that removal should run
         log.info("Notifying removal via manual force...");
         START_REMOVAL_LOCK.notify();
      }
   }

   /**
    * Manually sets the session with the specified sessionId
    * past expiry for passivation
    * 
    * @param sessionId
    */
   public void makeSessionEligibleForPassivation(Serializable sessionId)
   {
      this.setSessionLastUsedPastTimeout(sessionId, this.getSessionTimeout());
   }

   /**
    * Manually sets the session with the specified sessionId
    * past expiry for removal
    * 
    * @param sessionId
    */
   public void makeSessionEligibleForRemoval(Serializable sessionId)
   {
      this.setSessionLastUsedPastTimeout(sessionId, this.getRemovalTimeout());
   }

   /**
    * Exposed for testing only
    * 
    * Returns whether or not the internal cacheMap contains
    * the specified key
    * 
    * @return
    */
   public boolean doesCacheMapContainKey(Serializable sessionId)
   {
      // Get the cacheMap
      CacheMap cm = this.cacheMap;

      // Synchronize on it
      synchronized (cm)
      {
         // Return whether the specified key was found
         return cm.containsKey(sessionId);
      }
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, overridden to switch up the Passivate and Removal tasks to
    * test-specific implementations
    */
   @Override
   public void start()
   {
      // Initialize
      String threadNamePrefix = "EJBTHREE-1549 SFSB Thread: ";

      // Switch up the Passivation and Removal Tasks to blocking implementations
      this.setTimeoutTask(new BlockingPassivationTask(threadNamePrefix + "PASSIVATION"));
      this.setRemovalTask(new BlockingRemovalTask(threadNamePrefix + "REMOVAL"));

      // Call super implementation
      super.start();

   }

   /**
    * BlockingRemovalTask
    * 
    * An extension of the default removal task which, instead 
    * of waiting for a timeout, will block until forced
    *
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private class BlockingRemovalTask extends RemovalTimeoutTask
   {
      public BlockingRemovalTask(String name)
      {
         super(name);
      }

      @Override
      public void block() throws InterruptedException
      {
         // Get a lock on our monitor
         synchronized (START_REMOVAL_LOCK)
         {
            if (!removalForced)
            {
               // Wait until we're signaled
               log.info("Waiting to be notified to run removal...");
               START_REMOVAL_LOCK.wait();
            }
            removalForced = false;
         }

         // Log that we've been notified
         log.info("Notified to run removal");
      }

      @Override
      protected void preRemoval()
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

         // Invoke super implementation
         super.preRemoval();
      }

   }

   /**
    * BlockingPassivationTask
    * 
    * An extension of the default timeout task which, instead of 
    * waiting for a timeout, will await (block until) notification that passivation
    * should run
    *
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private class BlockingPassivationTask extends SessionTimeoutTask
   {

      public BlockingPassivationTask(String name)
      {
         super(name);
      }

      @Override
      public void block() throws InterruptedException
      {
         // Get a lock on our monitor
         synchronized (START_PASSIVATION_LOCK)
         {
            if (!passivationForced)
            {
               // Wait until we're signaled
               log.info("Waiting to be notified to run passivation...");
               START_PASSIVATION_LOCK.wait();
            }
            passivationForced = false;
         }

         // Log that we've been notified
         log.info("Notified to run passivation");
      }

      @Override
      protected void passivationCompleted()
      {
         // Call super
         super.passivationCompleted();

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

      @Override
      protected void prePassivationCompleted()
      {
         super.prePassivationCompleted();

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

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

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
      // Get the cacheMap
      CacheMap cm = this.cacheMap;

      // Synchronize on it
      synchronized (cm)
      {
         // Find the session
         StatefulBeanContext session = (StatefulBeanContext) cm.get(sessionId);

         // Synchronize on the session
         synchronized (session)
         {
            // Manually set past expiry
            session.lastUsed = this.getExpiredTime(timeout);
         }
      }
   }

}