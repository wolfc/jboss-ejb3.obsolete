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

package org.jboss.ejb3.cache.impl.factory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.jboss.ejb3.cache.spi.PassivationExpirationCoordinator;
import org.jboss.ejb3.cache.spi.PassivationExpirationProcessor;
import org.jboss.ejb3.cache.spi.impl.AbstractTimerTask;
import org.jboss.logging.Logger;
import org.jboss.util.threadpool.BasicThreadPool;

/**
 * {@link PassivationExpirationCoordinator} implementation that runs as a  
 * background TimerTask which when executed takes threads from a thread
 * pool and uses them to process passivation and expiration.
 * <p>
 * If no thread pool is provided, the passivation/expiration will be handled
 * by the timer thread itself.
 * </p>
 * 
 * @author Brian Stansberry
 */
public class PassivationExpirationCoordinatorImpl
   extends AbstractTimerTask
   implements PassivationExpirationCoordinator
{
   public static final long DEFAULT_INTERVAL = 10000L;
   public static final String TIMER_NAME = "EJB3SFSBPassivationExpirationCoordinator";
   
   private static final Logger log = Logger.getLogger(PassivationExpirationCoordinatorImpl.class);
   
   private BasicThreadPool threadPool;
   private int maxPoolThreads;
   private Set<PassivationExpirationProcessor> processors = new HashSet<PassivationExpirationProcessor>();
   private Semaphore threadLimit;
   
   public PassivationExpirationCoordinatorImpl()
   {
      super(TIMER_NAME, DEFAULT_INTERVAL);
   }
   
   // -------------------------------------------------------------- Properties
   
   /**
    * Gets the thread pool used to get threads for concurrently processing
    * passivation/expiration on different caches.
    * 
    * @return the pool. May be <code>null</code>
    */
   public BasicThreadPool getThreadPool()
   {
      return threadPool;
   }

   /**
    * Sets the thread pool to use to get threads for concurrently processing
    * passivation/expiration on different caches.
    * 
    * @param threadPool the pool. May be <code>null</code>
    */
   public void setThreadPool(BasicThreadPool threadPool)
   {
      this.threadPool = threadPool;
   }

   /**
    * Gets the maximum number of threads that can be concurrently taken from the
    * {@link #getThreadPool() thread pool} in order to process
    * passivation/expiration on different caches.
    * 
    * @return the maximum number of threads. A value less than 1 means
    *         the thread pool will not be used.
    */
   public int getMaxPoolThreads()
   {
      return maxPoolThreads;
   }

   /**
    * Sets the maximum number of threads to concurrently take from the
    * {@link #getThreadPool() thread pool} in order to process
    * passivation/expiration on different caches.
    * 
    * @param numThreads the maximum number of threads. A value less than
    *                   1 disables use of the thread pool.
    */
   public void setMaxPoolThreads(int numThreads)
   {
      this.maxPoolThreads = numThreads;
   }

   // ---------------------------------------- PassivationExpirationCoordinator
   
   public void addPassivationExpirationProcessor(PassivationExpirationProcessor processor)
   {
      synchronized (processors)
      {
         processors.add(processor);
      }
   }

   public void removePassivationExpirationProcessor(PassivationExpirationProcessor processor)
   {
      synchronized (processors)
      {
         processors.remove(processor);
      }
   }

   // -------------------------------------------------------------- TimerTask
   
   @Override
   public void run()
   {
      try
      {
      Set<PassivationExpirationProcessor> toProcess = new HashSet<PassivationExpirationProcessor>();
      synchronized (processors)
      {
         toProcess.addAll(processors);
      }
      
      for (PassivationExpirationProcessor cache : toProcess)
      {            
         if (cache.isPassivationExpirationSelfManaged())
            continue;
         
         PassivationExpirationTask task = new PassivationExpirationTask(cache, threadLimit);
         if (threadLimit != null)
         {
            // Limit the number of concurrent threads
            try
            {
               threadLimit.acquire();
            }
            catch (InterruptedException ignore)
            {
               // TODO don't ignore
            }
            
            if (isStopped())
               break;
            
            threadPool.run(task);
         }
         else
         {
            // We run it our self
            task.run();
         }
      }
      }
      catch (Exception e)
      {
         // Don't let an exception kill us
         log.error("Caught exception in handler thread", e);
      }
   }
   
   @Override
   public void start()
   {
      if (isStopped())
      {
         if (threadPool != null && getMaxPoolThreads() > 0)
         {
            threadLimit = new Semaphore(getMaxPoolThreads());
         }
      }
      super.start();      
   }
   
   /**
    * Task executed by the thread pool thread.
    */
   private class PassivationExpirationTask implements Runnable
   {
      private final PassivationExpirationProcessor cache;
      private final Semaphore semaphore;
      
      PassivationExpirationTask(PassivationExpirationProcessor cache, Semaphore semaphore)
      {
         assert cache != null : "cache is null";
         assert semaphore != null : "semaphore is null";
         
         this.cache = cache;
         this.semaphore = semaphore;
      }
      
      public void run()
      {
         try
         {
            cache.processPassivationExpiration();
         }
         finally
         {
            semaphore.release();            
         }
      }
   }
}
