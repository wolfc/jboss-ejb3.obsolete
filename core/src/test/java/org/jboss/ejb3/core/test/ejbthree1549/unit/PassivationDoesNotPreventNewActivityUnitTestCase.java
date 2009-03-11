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
package org.jboss.ejb3.core.test.ejbthree1549.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactoryRegistry;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1549.BlockingPersistenceManager;
import org.jboss.ejb3.core.test.ejbthree1549.BlockingPersistenceManagerFactory;
import org.jboss.ejb3.core.test.ejbthree1549.ForceEventsCache;
import org.jboss.ejb3.core.test.ejbthree1549.ForceEventsCacheFactory;
import org.jboss.ejb3.core.test.ejbthree1549.MyStatefulBean;
import org.jboss.ejb3.core.test.ejbthree1549.MyStatefulLocal;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulLocalProxyInvocationHandler;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * PassivationDoesNotPreventNewActivityUnitTestCase
 * 
 * Contains tests to ensure that performing passivation does not 
 * block either new session creation or invocation in other sessions
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class PassivationDoesNotPreventNewActivityUnitTestCase extends AbstractEJB3TestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static StatefulContainer container;

   private static final Logger log = Logger.getLogger(PassivationDoesNotPreventNewActivityUnitTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that removal may take place during the course 
    * of passivation
    */
   @Test
   public void testSessionRemovalDuringPassivation() throws Throwable
   {
      /*
       * First we invoke upon a new session
       */

      // Initialize
      final String sfsbJndiName = MyStatefulLocal.JNDI_NAME;

      // Create a new session, which should be allowed during passivation
      MyStatefulLocal sfsb = (MyStatefulLocal) container.getInitialContext().lookup(sfsbJndiName);

      // If we've got nothing
      if (sfsb == null)
      {
         // Fail
         TestCase.fail("Lookup did not succeed");
      }

      // Invoke upon the new Session
      final int next = sfsb.getNextCounter();

      // Test the value is expected
      TestCase.assertEquals("Next counter received was not expected", 0, next);

      /*
       * Mark this session as eligible for removal
       */

      // Get the cache
      ForceEventsCache cache = (ForceEventsCache) container.getCache();

      // Get the Session ID
      Serializable sessionId = this.getSessionId(sfsb);

      // Mark
      cache.makeSessionEligibleForRemoval(sessionId);

      /*
       * All's OK with traditional invocation, so define a task to force removal 
       * of the SFSB, but don't invoke it yet (we're going to fire this off 
       * *during* passivation
       */

      // Define the task to invoke upon a SFSB, and trigger removal while passivation is suspended
      Callable<Boolean> invokeDuringPasssivationTest = new Callable<Boolean>()
      {
         /**
          * Force removal
          */
         public Boolean call()
         {

            /*
             * Force removal
             */

            // Force
            ForceEventsCache.forceRemoval();

            // Clear the barrier
            try
            {
               log.info("Test is waiting on the pre-remove barrier");
               ForceEventsCache.PRE_REMOVE_BARRIER.await();
               log.info("Test has cleared the pre-remove barrier");
            }
            catch (InterruptedException e)
            {
               throw new RuntimeException(e);
            }
            catch (BrokenBarrierException e)
            {
               throw new RuntimeException(e);
            }

            // Return OK
            return true;

         }
      };

      /*
       * Force passivation, but block it from completing (because we don't 
       * await on the PM)
       */
      
      // Force passivation
      ForceEventsCache.forcePassivation();
      ForceEventsCache.PRE_PASSIVATE_BARRIER.await();
      

      /*
       * Spawn off the test in another Thread
       */

      ExecutorService executor = Executors.newFixedThreadPool(1);
      Future<Boolean> futureResult = executor.submit(invokeDuringPasssivationTest);
      Boolean result = null;

      /*
       * Try to get the result of the test to remove, which 
       * should not be blocked by passivation already in progress
       */

      // Get result
      log.info("Attempting to get the result of the removal task...");
      result = futureResult.get(5, TimeUnit.SECONDS);

      // Make sure the result is expected
      TestCase.assertTrue("Removal task completed when expected, but got wrong result", result);
   }

   @Test
   public void testInvokeSameSessionDuringPassivation() throws Throwable
   {
      final MyStatefulLocal bean = lookup(MyStatefulLocal.JNDI_NAME, MyStatefulLocal.class);

      // Get our bean's Session ID
      StatefulLocalProxyInvocationHandler handler = (StatefulLocalProxyInvocationHandler) Proxy
            .getInvocationHandler(bean);
      Serializable sessionId = (Serializable) handler.getTarget();

      // Invoke upon our bean
      int next = bean.getNextCounter();
      log.info("Got counter from " + sessionId + ": " + next);
      TestCase.assertEquals("SFSB did not return expected next counter", 0, next);

      // Get the Cache
      ForceEventsCache cache = (ForceEventsCache) container.getCache();

      // Get the lock to block the PM, now
      boolean gotLock = BlockingPersistenceManager.PASSIVATION_LOCK.tryLock();

      Future<Integer> result;
      // Once PM lock is acquired, everything is in "try" so we release in "finally"
      try
      {
         // Ensure we got the PM lock, else fail the test
         TestCase.assertTrue("Test was not able to immediately get the lock to block the PersistenceManager", gotLock);
         log.info("Locked " + BlockingPersistenceManager.class.getSimpleName());

         // Mark
         cache.makeSessionEligibleForPassivation(sessionId);

         /*
          * Passivate
          */

         // Trigger Passivation
         ForceEventsCache.forcePassivation();
         log.info("Passivation forced, carrying out test");

         ForceEventsCache.PRE_PASSIVATE_BARRIER.await(5, TimeUnit.SECONDS);

         // Block until the PM is ready to passivate
         log.info("Waiting on common barrier for PM to run...");
         BlockingPersistenceManager.BARRIER.await(5, TimeUnit.SECONDS);
         log.info("PM and test have met barrier, passivation running (but will be blocked to complete by test)");

         Callable<Integer> task = new Callable<Integer>()
         {
            public Integer call() throws Exception
            {
               return bean.getNextCounter();
            }
         };
         ExecutorService executor = Executors.newFixedThreadPool(1);
         result = executor.submit(task);

         // TODO: there is no way to know where we are in StatefulInstanceInterceptor
         Thread.sleep(5000);
      }
      finally
      {
         // Allow the Persistence Manager to finish up
         log.info("Letting the PM perform passivation...");
         BlockingPersistenceManager.PASSIVATION_LOCK.unlock();
      }

      // We need to allow time to let the Cache finish passivation, so block until it's done
      log.info("Waiting on Cache to tell us passivation is completed...");
      ForceEventsCache.POST_PASSIVATE_BARRIER.await(5, TimeUnit.SECONDS);
      log.info("Test sees Cache reports passivation completed.");

      int duringPassivation = result.get(5, TimeUnit.SECONDS);
      log.info("Got counter from " + sessionId + ": " + duringPassivation);

      int postPassivation = bean.getNextCounter();
      log.info("Got counter from " + sessionId + ": " + postPassivation);

      assertEquals("the postPassivation counter should be 1 higher than the previous (during passivation)",
            duringPassivation + 1, postPassivation);
   }

   @Test
   public void testInvokeSameSessionDuringPrePassivation() throws Throwable
   {
      final MyStatefulLocal bean = lookup(MyStatefulLocal.JNDI_NAME, MyStatefulLocal.class);

      // Get our bean's Session ID
      StatefulLocalProxyInvocationHandler handler = (StatefulLocalProxyInvocationHandler) Proxy
            .getInvocationHandler(bean);
      Serializable sessionId = (Serializable) handler.getTarget();

      // Invoke upon our bean
      int next = bean.getNextCounter();
      log.info("Got counter from " + sessionId + ": " + next);
      TestCase.assertEquals("SFSB did not return expected next counter", 0, next);

      // Get the Cache
      ForceEventsCache cache = (ForceEventsCache) container.getCache();

      // Get the lock to block the PM, now
      boolean gotLock = BlockingPersistenceManager.PASSIVATION_LOCK.tryLock();

      Future<Integer> result;
      // Once PM lock is acquired, everything is in "try" so we release in "finally"
      try
      {
         // Ensure we got the PM lock, else fail the test
         TestCase.assertTrue("Test was not able to immediately get the lock to block the PersistenceManager", gotLock);
         log.info("Locked " + BlockingPersistenceManager.class.getSimpleName());

         // Mark
         cache.makeSessionEligibleForPassivation(sessionId);

         /*
          * Passivate
          */

         // Trigger Passivation
         ForceEventsCache.forcePassivation();
         log.info("Passivation forced, carrying out test");

         Callable<Integer> task = new Callable<Integer>()
         {
            public Integer call() throws Exception
            {
               return bean.getNextCounter();
            }
         };
         ExecutorService executor = Executors.newFixedThreadPool(1);
         result = executor.submit(task);

         // TODO: there is no way to know where we are in StatefulInstanceInterceptor
         Thread.sleep(5000);

         ForceEventsCache.PRE_PASSIVATE_BARRIER.await(5, TimeUnit.SECONDS);

         // Block until the PM is ready to passivate
         /* we're not passivating, we yanked it out
         log.info("Waiting on common barrier for PM to run...");
         BlockingPersistenceManager.BARRIER.await(5, TimeUnit.SECONDS);
         log.info("PM and test have met barrier, passivation running (but will be blocked to complete by test)");
         */
      }
      finally
      {
         // Allow the Persistence Manager to finish up
         log.info("Letting the PM perform passivation...");
         BlockingPersistenceManager.PASSIVATION_LOCK.unlock();
      }

      // We need to allow time to let the Cache finish passivation, so block until it's done
      log.info("Waiting on Cache to tell us passivation is completed...");
      ForceEventsCache.POST_PASSIVATE_BARRIER.await(5, TimeUnit.SECONDS);
      log.info("Test sees Cache reports passivation completed.");

      int duringPassivation = result.get(5, TimeUnit.SECONDS);
      log.info("Got counter from " + sessionId + ": " + duringPassivation);

      int postPassivation = bean.getNextCounter();
      log.info("Got counter from " + sessionId + ": " + postPassivation);

      assertEquals("the postPassivation counter should be 1 higher than the previous (during passivation)",
            duringPassivation + 1, postPassivation);
   }

   /**
    * Tests that a new session may be created while another is being passivated
    */
   @Test
   public void testNewSessionMayBeCreatedDuringPassivation() throws Throwable
   {
      // Initialize
      final String sfsbJndiName = MyStatefulLocal.JNDI_NAME;

      // Define the task to lookup a new session
      Callable<Boolean> lookupNewSessionTest = new Callable<Boolean>()
      {
         /**
          * Lookup a new session, and set that the test succeeded if we're able
          */
         public Boolean call()
         {
            try
            {
               // Create a new session, which should be allowed during passivation
               MyStatefulLocal bean2 = (MyStatefulLocal) container.getInitialContext().lookup(sfsbJndiName);

               // If we've don't have a new session
               if (bean2 == null)
               {
                  throw new RuntimeException("Got back null SFSB");
               }

            }
            // We can't fail the unit test from here, so log the error
            catch (Exception e)
            {
               log.error("Test encountered an error", e);
            }

            // Test is good
            return true;
         }
      };

      // Run the Test
      this.runTestDuringPassivation(lookupNewSessionTest);
   }

   /**
    * Tests that a one session may carry out an invocation while another session
    * is undergoing passivation
    */
   @Test
   public void testSessionMayBeInvokedWhileAnotherIsPassivating() throws Throwable
   {
      // Initialize
      final String sfsbJndiName = MyStatefulLocal.JNDI_NAME;

      // Define the task to invoke upon a SFSB
      Callable<Boolean> invokeDuringPasssivationTest = new Callable<Boolean>()
      {
         /**
          * Lookup a new session, invoke upon it, and set that the test succeeded if we're able
          */
         public Boolean call()
         {
            try
            {
               // Create a new session, which should be allowed during passivation
               MyStatefulLocal bean2 = (MyStatefulLocal) container.getInitialContext().lookup(sfsbJndiName);

               // If we've got nothing
               if (bean2 == null)
               {
                  // Let test fail, logging along the way
                  log.error("Lookup did not succeed");
               }

               // Invoke upon the new Session
               int next = bean2.getNextCounter();

               // Test the value
               if (next == 0)
               {
                  return true;
               }

               // Value was not expected, let the test fail and log
               log.error("Invocation succeeded, but expected next counter of 0 and got: " + next);

            }
            // We can't fail the unit test from here, so log the error
            catch (Exception e)
            {
               log.error("Test encountered an error", e);
            }

            // Fail, should have gotten expected value and returned above
            return false;
         }
      };

      // Run the Test
      this.runTestDuringPassivation(invokeDuringPasssivationTest);
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Performs the following common test actions:
    * 
    * 1) Looks up and invokes upon a new SFSB instance
    * 2) Blocks passivation from completing
    * 3) Waits for the SFSB instance to becomes eligible for passivation
    * 4) Forces passivation to run
    * 5) Runs the specified Test Thread
    * 6) Allows the test to complete either successfully or unsuccessfully, or times out if stuck
    * 7) Reports failure or success overall
    */
   protected void runTestDuringPassivation(Callable<Boolean> testThread) throws Throwable
   {
      // Initialize
      final String sfsbJndiName = MyStatefulLocal.JNDI_NAME;
      boolean testSucceeded = false;

      // Lookup an instance
      MyStatefulLocal bean1 = (MyStatefulLocal) container.getInitialContext().lookup(sfsbJndiName);

      // Invoke upon our bean
      int next = bean1.getNextCounter();
      log.info("Got counter from bean1 : " + next);
      TestCase.assertEquals("SFSB did not return expected next counter", 0, next);

      // Get our bean's Session ID
      Serializable sessionId = this.getSessionId(bean1);

      // Get the Cache
      ForceEventsCache cache = (ForceEventsCache) container.getCache();

      // Get the lock to block the PM, now
      boolean gotLock = BlockingPersistenceManager.PASSIVATION_LOCK.tryLock();

      // Once PM lock is acquired, everything is in "try" so we release in "finally"
      try
      {
         // Ensure we got the PM lock, else fail the test
         TestCase.assertTrue("Test was not able to immediately get the lock to block the PersistenceManager", gotLock);
         log.info("Locked " + BlockingPersistenceManager.class.getSimpleName());

         /*
          * Mark our session as expired
          */

         // Mark
         cache.makeSessionEligibleForPassivation(sessionId);

         /*
          * Passivate
          */

         // Trigger Passivation
         ForceEventsCache.forcePassivation();
         log.info("Passivation forced, carrying out test");

         ForceEventsCache.PRE_PASSIVATE_BARRIER.await(5, TimeUnit.SECONDS);

         // Block until the PM is ready to passivate
         log.info("Waiting on common barrier for PM to run...");
         BlockingPersistenceManager.BARRIER.await(5, TimeUnit.SECONDS);
         log.info("PM and test have met barrier, passivation running (but will be blocked to complete by test)");

         /*
          * At this point, we've told the passivation Thread to start, and have 
          * locked it from completing.  So let's try our test in another Thread
          * so we can detect a deadlock or permanent blocking after a timeout
          */
         ExecutorService executor = Executors.newFixedThreadPool(1);
         Future<Boolean> futureResult = executor.submit(testThread);
         boolean result = futureResult.get(5, TimeUnit.SECONDS);

         /*
          * Assert on the result
          */

         // If the test has not succeeded
         if (!result)
         {
            // Fail
            TestCase.fail("The test has completed without success");
         }

         // If the test has been successful
         else
         {
            log.info("Test Succeeded");
            testSucceeded = true;
         }
      }
      finally
      {

         // Allow the Persistence Manager to finish up
         log.info("Letting the PM perform passivation...");
         BlockingPersistenceManager.PASSIVATION_LOCK.unlock();
      }

      // We need to allow time to let the Cache finish passivation, so block until it's done
      log.info("Waiting on Cache to tell us passivation is completed...");
      ForceEventsCache.POST_PASSIVATE_BARRIER.await(5, TimeUnit.SECONDS);
      log.info("Test sees Cache reports passivation completed.");

      /*
       * Here we ensure that the session was removed from the internal cacheMap
       */
      boolean beanIsInCache = cache.doesCacheMapContainKey(sessionId);
      assertFalse("bean " + sessionId + " was not removed from cache", beanIsInCache);

      // Ensure we're good
      TestCase.assertTrue("The test did not succeed", testSucceeded);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      // Perform generic setup of MC, default beans, etc
      AbstractEJB3TestCase.beforeClass();

      // Add the Blocking PersistenceManager
      PersistenceManagerFactoryRegistry pmFactoryRegistry = Ejb3RegistrarLocator.locateRegistrar().lookup(
            "EJB3PersistenceManagerFactoryRegistry", PersistenceManagerFactoryRegistry.class);
      pmFactoryRegistry.getFactories().put(BlockingPersistenceManagerFactory.REGISTRY_BIND_NAME,
            BlockingPersistenceManagerFactory.class);

      // Add the Force Passivation Cache
      CacheFactoryRegistry cacheFactoryRegistry = Ejb3RegistrarLocator.locateRegistrar().lookup(
            "EJB3CacheFactoryRegistry", CacheFactoryRegistry.class);
      String forcePassivationCacheRegistryName = ForceEventsCacheFactory.REGISTRY_BIND_NAME;
      cacheFactoryRegistry.getFactories().put(forcePassivationCacheRegistryName, ForceEventsCacheFactory.class);
      log.info("Added " + forcePassivationCacheRegistryName);

      // Deploy the test SFSB
      Class<?> ejbImplClass = MyStatefulBean.class;
      log.info("Deploying SFSB: " + ejbImplClass.getName());
      container = (StatefulContainer) deploySessionEjb(ejbImplClass);
   }

   @After
   public void after()
   {
      log.info("Resetting all barriers and clearing the cache...");
      ForceEventsCache.POST_PASSIVATE_BARRIER.reset();
      ForceEventsCache.PRE_PASSIVATE_BARRIER.reset();
      ForceEventsCache.PRE_REMOVE_BARRIER.reset();
      BlockingPersistenceManager.BARRIER.reset();
      ForceEventsCache cache = (ForceEventsCache) container.getCache();
      cache.clear();
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      // Undeploy the test SFSB
      undeployEjb(container);

      AbstractEJB3TestCase.afterClass();
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns the Session ID of the specified proxy
    */
   private Serializable getSessionId(MyStatefulLocal bean)
   {
      // Get our bean's Session ID
      StatefulLocalProxyInvocationHandler handler = (StatefulLocalProxyInvocationHandler) Proxy
            .getInvocationHandler(bean);
      Serializable sessionId = (Serializable) handler.getTarget();
      return sessionId;
   }
}
