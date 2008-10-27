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

import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;

import junit.framework.TestCase;

import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactoryRegistry;
import org.jboss.ejb3.cache.simple.SimpleStatefulCache;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1549.BlockingPersistenceManager;
import org.jboss.ejb3.core.test.ejbthree1549.BlockingPersistenceManagerFactory;
import org.jboss.ejb3.core.test.ejbthree1549.ForcePassivationCache;
import org.jboss.ejb3.core.test.ejbthree1549.ForcePassivationCacheFactory;
import org.jboss.ejb3.core.test.ejbthree1549.MyStatefulBean;
import org.jboss.ejb3.core.test.ejbthree1549.MyStatefulLocal;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulLocalProxyInvocationHandler;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.logging.Logger;
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

   private static SessionContainer container;

   private static final Logger log = Logger.getLogger(PassivationDoesNotPreventNewActivityUnitTestCase.class);

   private static Map getCacheMap(SimpleStatefulCache cache)
   {
      try
      {
         Field f = SimpleStatefulCache.class.getDeclaredField("cacheMap");
         f.setAccessible(true);
         return (Map) f.get(cache);
      }
      catch(SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch(NoSuchFieldException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that a new session may be created while another is being passivated
    */
   @Test
   public void testNewSessionMayBeCreatedDuringPassivation() throws Throwable
   {
      // Initialize
      final String sfsbJndiName = MyStatefulLocal.JNDI_NAME;

      // Define the task to lookup a new session
      TestThread lookupNewSessionTest = new TestThread("Lookup New Session Test Thread")
      {
         /**
          * Lookup a new session, and set that the test succeeded if we're able
          */
         public void run()
         {
            try
            {
               // Create a new session, which should be allowed during passivation
               MyStatefulLocal bean2 = (MyStatefulLocal) container.getInitialContext().lookup(sfsbJndiName);

               // If we've got a new session
               if (bean2 != null)
               {
                  // Test is good
                  this.setTestSucceeded(true);
               }

            }
            // We can't fail the unit test from here, so log the error
            catch (Exception e)
            {
               log.error("Test encountered an error", e);
            }
         }
      };

      // Run the Test
      this.runTest(lookupNewSessionTest);
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
      TestThread invokeDuringPasssivationTest = new TestThread("Invoke During Passivation Test Thread")
      {
         /**
          * Lookup a new session, invoke upon it, and set that the test succeeded if we're able
          */
         public void run()
         {
            try
            {
               // Create a new session, which should be allowed during passivation
               MyStatefulLocal bean2 = (MyStatefulLocal) container.getInitialContext().lookup(sfsbJndiName);

               // If we've got nothing
               if (bean2 == null)
               {
                  // Let test fail, logging along the wayF
                  log.error("Lookup did not succeed");
                  return;
               }

               // Invoke upon the new Session
               int next = bean2.getNextCounter();

               // Test the value
               if (next == 0)
               {
                  this.setTestSucceeded(true);
                  return;
               }

               // Value was not expected, let the test fail and log
               log.error("Invocation succeeded, but expected next counter of 0 and got: " + next);

            }
            // We can't fail the unit test from here, so log the error
            catch (Exception e)
            {
               log.error("Test encountered an error", e);
            }
         }
      };

      // Run the Test
      this.runTest(invokeDuringPasssivationTest);
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
   protected void runTest(TestThread testThread) throws Throwable
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

      // Get the lock to block the PM, now
      boolean gotLock = BlockingPersistenceManager.LOCK.tryLock();

      // Once PM lock is acquired, everything is in "try" so we release in "finally"
      try
      {
         // Ensure we got the PM lock, else fail the test
         TestCase.assertTrue("Test was not able to immediately get the lock to block the PersistenceManager", gotLock);
         log.info("Locked " + BlockingPersistenceManager.class.getSimpleName());

         // Wait to allow bean to be eligible for passivation
         long sleepTime = MyStatefulLocal.PASSIVATION_TIMEOUT * 1000 + 1000; // Add 1/2 a second to the configured passivation timeout
         Thread.sleep(sleepTime);

         // Trigger Passivation
         ForcePassivationCache.forcePassivation();
         log.info("Passivation forced, carrying out test");

         // Wait to allow passivation to actually start
         Thread.sleep(2000);

         StatefulLocalProxyInvocationHandler handler = (StatefulLocalProxyInvocationHandler) Proxy.getInvocationHandler(bean1);
         Serializable sessionId = handler.getSessionId();
         assertFalse("bean was not removed from cache", getCacheMap((SimpleStatefulCache) ((StatefulContainer) container).getCache()).containsKey(sessionId));
         
         /*
          * At this point, we've told the passivation Thread to start, and have 
          * locked it from completing.  So let's try our test in another Thread
          * so we can detect a deadlock or permanent blocking after a timeout
          */

         // Start up the test Thread
         testThread.start();

         // Define a timeout for the Test Thread to complete
         int timeoutSeconds = 5;
         long startTime = System.currentTimeMillis();
         long endTime = timeoutSeconds * 1000 + startTime;

         // Loop until timeout
         while (System.currentTimeMillis() < endTime)
         {
            // If the test has not succeeded, and the test Thread is dead
            if (!testThread.testSucceeded && !testThread.isAlive())
            {
               // Fail
               TestCase.fail("The test has completed without success");
               break;
            }

            // If the test has been successful
            if (testThread.testSucceeded)
            {
               log.info("Test Succeeded");
               testSucceeded = true;
               break;
            }

            // Wait a little before looping again
            Thread.sleep(50);
         }
      }
      finally
      {

         // Allow the Persistence Manager to finish up
         BlockingPersistenceManager.LOCK.unlock();
      }

      // Ensure we're good
      TestCase.assertTrue("The test did not succeed", testSucceeded);
   }

   // --------------------------------------------------------------------------------||
   // Inner Classes ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Defines a test task to run alongside main test execution, providing
    * hooks to test whether the task was successful or not
    */
   protected static class TestThread extends Thread
   {
      /**
       * Whether or not the test succeeded
       */
      private boolean testSucceeded;

      public TestThread(String threadName)
      {
         super(threadName);
      }

      public boolean isTestSucceeded()
      {
         return testSucceeded;
      }

      public void setTestSucceeded(boolean testSucceeded)
      {
         this.testSucceeded = testSucceeded;
      }
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
      String forcePassivationCacheRegistryName = ForcePassivationCacheFactory.REGISTRY_BIND_NAME;
      cacheFactoryRegistry.getFactories().put(forcePassivationCacheRegistryName, ForcePassivationCacheFactory.class);
      log.info("Added " + forcePassivationCacheRegistryName);

      // Deploy the test SFSB
      Class<?> ejbImplClass = MyStatefulBean.class;
      log.info("Deploying SFSB: " + ejbImplClass.getName());
      container = deploySessionEjb(ejbImplClass);
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      // Undeploy the test SFSB
      undeployEjb(container);

      AbstractEJB3TestCase.afterClass();
   }

}
