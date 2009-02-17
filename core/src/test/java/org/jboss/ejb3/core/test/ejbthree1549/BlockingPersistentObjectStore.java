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

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJBException;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.PersistentObjectStore;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.logging.Logger;

/**
 * BlockingPersistenceManager
 * 
 * An implementation of a PersistenceManager which, instead of
 * persisting directly, exposes a blocking mechanism allowing 
 * tests to control when passivation occurs 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class BlockingPersistentObjectStore<T extends CacheItem> implements PersistentObjectStore<T>
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(BlockingPersistentObjectStore.class);

   /**
    * Publicly-accessible lock
    * 
    * Used by the test to block the act of passivation
    */
   public static final Lock PASSIVATION_LOCK = new ReentrantLock();

   /**
    * Publicly-accessible barrier
    * 
    * Will block until both the test and the PM agree that passivation 
    * should take place
    */
   public static final CyclicBarrier BARRIER = new CyclicBarrier(2);
   
   private Map<Object, MarshalledObject> passivated = new ConcurrentHashMap<Object, MarshalledObject>();

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public T load(Object id)
   {
      log.info("Activating " + id);
      MarshalledObject o = passivated.remove(id);
      if(o == null)
         throw new EJBException("Can't find bean " + id);
      try
      {
         return (T) o.get();
      }
      catch (IOException e)
      {
         throw new EJBException(e);
      }
      catch (ClassNotFoundException e)
      {
         throw new EJBException(e);
      }
   }

   public List<StatefulBeanContext> getPassivatedBeans()
   {
      // very stupid and slow, don't do this
      throw new RuntimeException("NYI");
   }
   
   public void store(T ctx)
   {

      try
      {
         /*
          * Block until the lock may be acquired,
          * may currently be held by the test Thread until the test is ready.
          * So here both the test and passivation will block until this barrier
          * is agreed by both Threads to be released
          */
         log.info("Waiting until the test is ready for passivation to start...");
         try
         {
            BARRIER.await();
         }
         catch (BrokenBarrierException e1)
         {
            log.debug("BARRIER was broken");
         }

         // Block until the test releases this lock
         log.info("Blocking until the test tells us that the act of passivation may continue...");
         PASSIVATION_LOCK.lock();

         try
         {
            // Mock Passivate
            log.info("Mock Passivation on " + ctx.getId());
            passivated.put(ctx.getId(), new MarshalledObject(ctx));
         }
         catch(IOException e)
         {
            throw new EJBException(e);
         }
         finally
         {
            // Release the passivation lock
            log.info("We're done with passivation, letting the lock go.");
            PASSIVATION_LOCK.unlock();
         }
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException("Barrier was interrupted prematurely", e);
      }
      finally
      {
         // Reset the Barrier
         BARRIER.reset();
      }
   }

   public void start()
   {
   }

   public void stop()
   {
      passivated.clear();
   }
   
   public void removePassivated(Object id)
   {
      MarshalledObject o = passivated.remove(id);
      if(o == null)
         throw new EJBException("Can't find bean " + id);
   }

}
