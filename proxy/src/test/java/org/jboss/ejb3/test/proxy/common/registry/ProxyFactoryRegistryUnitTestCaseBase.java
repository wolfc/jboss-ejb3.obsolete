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
package org.jboss.ejb3.test.proxy.common.registry;

import java.util.UUID;

import junit.framework.TestCase;

import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryAlreadyRegisteredException;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryNotRegisteredException;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryRegistry;
import org.jboss.ejb3.test.proxy.common.EmbeddedTestMcBootstrap;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyFactoryRegistryUnitTestCaseBase
 * 
 * Base upon which Proxy Factory Registry tests may build
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class ProxyFactoryRegistryUnitTestCaseBase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ProxyFactoryRegistryUnitTestCaseBase.class);

   public static final String MC_BEAN_NAME_PROXY_FACTORY_REGISTRY = "org.jboss.ejb3.ProxyFactoryRegistry";

   private static final String REGISTRY_KEY_PREFIX = "TestRegistrationKey-";

   private static EmbeddedTestMcBootstrap bootstrap;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that the Registry was successfully installed by performing a lookup
    * via MC.
    */
   @Test
   public void testJndiProxyFactoryRegistryInstall() throws Throwable
   {
      ProxyFactoryRegistry registry = this.getProxyFactoryRegistry();
      TestCase.assertNotNull(registry);
   }

   /**
    * Tests that a Proxy Factory may be registered and looked up, a 
    * full round trip.  The object obtained from the registry must be 
    * referencially equal to the object registered.
    */
   @Test
   public void testRegistrationAndLookup() throws Throwable
   {
      // Initialize
      String key = ProxyFactoryRegistryUnitTestCaseBase.REGISTRY_KEY_PREFIX + UUID.randomUUID();

      // Create a new Proxy Factory
      ProxyFactory factory = this.createMockProxyFactory();

      // Get the registry
      ProxyFactoryRegistry registry = this.getProxyFactoryRegistry();

      // Lookup (Should Fail)
      try
      {
         Object obj = registry.getProxyFactory(key);
         TestCase.fail("Obtaining Proxy Factory " + obj + " under key " + key + " should have failed with "
               + ProxyFactoryNotRegisteredException.class.getName());
      }
      catch (ProxyFactoryNotRegisteredException e)
      {
         log.info("As expected, registry does not yet contain key " + key);
      }

      // Register
      try
      {
         registry.registerProxyFactory(key, factory);
         log.info(factory + " stored into " + registry + " under key " + key);
      }
      catch (ProxyFactoryAlreadyRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory + " should be able to be registered with " + registry + " under key " + key);
      }

      // Lookup
      ProxyFactory lookup = null;
      try
      {
         lookup = registry.getProxyFactory(key);
         log.info(lookup + " obtained from  " + registry + " under key " + key);
      }
      catch (ProxyFactoryNotRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory + " should have been found in " + registry + " under key " + key + ", but instead "
               + e.getClass().getName() + " was encountered with message:\n" + e.getMessage());
      }

      // Ensure the factory looked up references the same object as the one placed in
      TestCase.assertTrue("The factory obtained from the registry, " + lookup
            + ", is not the same as the factory put in, " + factory, factory == lookup);
   }

   /**
    * Tests that a Proxy Factory may be registered and deregistered, 
    * checking along the way that testing that "isRegistered" works
    * as expected
    */
   @Test
   public void testRegistrationAndDeregistration() throws Throwable
   {
      // Initialize
      String key = ProxyFactoryRegistryUnitTestCaseBase.REGISTRY_KEY_PREFIX + UUID.randomUUID();

      // Create a new Proxy Factory
      ProxyFactory factory = this.createMockProxyFactory();

      // Get the registry
      ProxyFactoryRegistry registry = this.getProxyFactoryRegistry();

      // Ensure Proxy Factory is not registered
      TestCase.assertEquals("Key " + key + " should not be registered yet in " + registry, false, registry
            .isRegistered(key));

      // Register
      try
      {
         registry.registerProxyFactory(key, factory);
         log.info(factory + " stored into " + registry + " under key " + key);
      }
      catch (ProxyFactoryAlreadyRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory + " should be able to be registered with " + registry + " under key " + key);
      }

      // Ensure Proxy Factory is Registered
      TestCase.assertTrue("Registry " + registry + " should contain " + factory + " under key " + key
            + " but does not appear as registered", registry.isRegistered(key));

      // Deregister
      try
      {
         registry.deregisterProxyFactory(key);
      }
      catch (ProxyFactoryNotRegisteredException e)
      {
         log.error(e);
         TestCase.fail("Key " + key + " should have been valid for registry " + registry
               + ", but could not be deregistered.");
      }

      // Ensure Proxy Factory is no longer registered
      TestCase.assertEquals("Key " + key + " for registry " + registry
            + " has been deregistered, but has come up as registered", false, registry.isRegistered(key));

   }

   /**
    * Tests that doubly-registering under the same key 
    * fails as expected
    */
   @Test
   public void testDuplicateKeyRegistrationFails() throws Throwable
   {
      // Initialize
      String key = ProxyFactoryRegistryUnitTestCaseBase.REGISTRY_KEY_PREFIX + UUID.randomUUID();

      // Create new Proxy Factories
      ProxyFactory factory1 = this.createMockProxyFactory();
      ProxyFactory factory2 = this.createMockProxyFactory();

      // Get the registry
      ProxyFactoryRegistry registry = this.getProxyFactoryRegistry();

      // Register 1
      try
      {
         registry.registerProxyFactory(key, factory1);
         log.info(factory1 + " stored into " + registry + " under key " + key);
      }
      catch (ProxyFactoryAlreadyRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory1 + " should be able to be registered with " + registry + " under key " + key);
      }

      // Register 2
      try
      {
         registry.registerProxyFactory(key, factory2);
      }
      // Expected
      catch (ProxyFactoryAlreadyRegisteredException e)
      {
         log.info(factory2 + " could not be stored into " + registry + " under key " + key
               + " as expected because of duplicate key");
         return;
      }

      // Not not be reached
      TestCase.fail("Registry " + registry + " should not be able to register factories under the same key, " + key
            + ", twice");
   }

   /**
    * Tests that the same factory may be registered under
    * 2 different keys
    */
   @Test
   public void testDuplicateFactoryRegistrationOK() throws Throwable
   {
      // Initialize
      String key1 = ProxyFactoryRegistryUnitTestCaseBase.REGISTRY_KEY_PREFIX + UUID.randomUUID();
      String key2 = ProxyFactoryRegistryUnitTestCaseBase.REGISTRY_KEY_PREFIX + UUID.randomUUID();

      // Create Proxy Factory
      ProxyFactory factory = this.createMockProxyFactory();

      // Get the registry
      ProxyFactoryRegistry registry = this.getProxyFactoryRegistry();

      // Register with key 1
      try
      {
         registry.registerProxyFactory(key1, factory);
         log.info(factory + " stored into " + registry + " under key " + key1);
      }
      catch (ProxyFactoryAlreadyRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory + " should be able to be registered with " + registry + " under key " + key1);
      }

      // Register 2
      try
      {
         registry.registerProxyFactory(key2, factory);
      }
      catch (ProxyFactoryAlreadyRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory + " should be able to be registered with " + registry + " under key " + key2);
      }

      // Lookup 1
      ProxyFactory lookup1 = null;
      try
      {
         lookup1 = registry.getProxyFactory(key1);
         log.info(lookup1 + " obtained from  " + registry + " under key " + key1);
      }
      catch (ProxyFactoryNotRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory + " should have been found in " + registry + " under key " + key1 + ", but instead "
               + e.getClass().getName() + " was encountered with message:\n" + e.getMessage());
      }

      // Lookup 2
      ProxyFactory lookup2 = null;
      try
      {
         lookup2 = registry.getProxyFactory(key2);
         log.info(lookup2 + " obtained from  " + registry + " under key " + key2);
      }
      catch (ProxyFactoryNotRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory + " should have been found in " + registry + " under key " + key2 + ", but instead "
               + e.getClass().getName() + " was encountered with message:\n" + e.getMessage());
      }

      // Ensure all factories are equal
      String message = "Object placed in registry should be equal to object obtained from lookup";
      TestCase.assertTrue(message, factory == lookup1);
      TestCase.assertTrue(message, lookup1 == lookup2); // Transitive (if A==B && B==C then A==C)

   }

   /**
    * Tests that the registry properly calls the lifecycle methods of
    * the ProxyFactory on registration/deregistration
    * 
    * @throws Throwable
    */
   @Test
   public void testRegistryInvokesProxyFactoryLifecycle() throws Throwable
   {
      // Initialize
      String key = ProxyFactoryRegistryUnitTestCaseBase.REGISTRY_KEY_PREFIX + UUID.randomUUID();

      // Create a new Proxy Factory
      MockLifecycleSessionProxyFactory factory = new MockLifecycleSessionProxyFactory();

      // Get the registry
      ProxyFactoryRegistry registry = this.getProxyFactoryRegistry();

      // Test Lifecycle Created
      TestCase.assertEquals("Lifecycle of " + factory + " should be " + MockLifecycleSessionProxyFactory.State.CREATED
            + " before registered", MockLifecycleSessionProxyFactory.State.CREATED, factory.getState());

      // Register
      try
      {
         registry.registerProxyFactory(key, factory);
         log.info(factory + " stored into " + registry + " under key " + key);
      }
      catch (ProxyFactoryAlreadyRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory + " should be able to be registered with " + registry + " under key " + key);
      }

      // Lookup
      ProxyFactory lookup = null;
      try
      {
         lookup = registry.getProxyFactory(key);
         log.info(lookup + " obtained from  " + registry + " under key " + key);
      }
      catch (ProxyFactoryNotRegisteredException e)
      {
         log.error(e);
         TestCase.fail(factory + " should have been found in " + registry + " under key " + key + ", but instead "
               + e.getClass().getName() + " was encountered with message:\n" + e.getMessage());
      }

      // Ensure the reference looked up and the reference placed in have state of STARTED
      TestCase.assertEquals("Lifecycle of " + lookup + " should be " + MockLifecycleSessionProxyFactory.State.STARTED
            + " before registered", MockLifecycleSessionProxyFactory.State.STARTED,
            MockLifecycleSessionProxyFactory.class.cast(lookup).getState());
      TestCase.assertEquals("Lifecycle of " + factory + " should be " + MockLifecycleSessionProxyFactory.State.STARTED
            + " before registered", MockLifecycleSessionProxyFactory.State.STARTED, factory.getState());
      
      // Deregister
      registry.deregisterProxyFactory(key);
      
      // Ensure the reference looked up and the reference placed in have state of STOPPED
      TestCase.assertEquals("Lifecycle of " + lookup + " should be " + MockLifecycleSessionProxyFactory.State.STOPPED
            + " before registered", MockLifecycleSessionProxyFactory.State.STOPPED,
            MockLifecycleSessionProxyFactory.class.cast(lookup).getState());
      TestCase.assertEquals("Lifecycle of " + factory + " should be " + MockLifecycleSessionProxyFactory.State.STOPPED
            + " before registered", MockLifecycleSessionProxyFactory.State.STOPPED, factory.getState());
   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the ProxyFactoryRegistry from the MC
    */
   private ProxyFactoryRegistry getProxyFactoryRegistry() throws Throwable
   {
      ProxyFactoryRegistry registry = ProxyFactoryRegistryUnitTestCaseBase.getBootstrap().lookup(
            ProxyFactoryRegistryUnitTestCaseBase.MC_BEAN_NAME_PROXY_FACTORY_REGISTRY, ProxyFactoryRegistry.class);
      return registry;
   }

   /**
    * Creates and returns a new Mock ProxyFactory
    * @return
    */
   private ProxyFactory createMockProxyFactory()
   {
      return new MockSessionProxyFactory();
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass()
   {
      // Create and set a new MC Bootstrap
      ProxyFactoryRegistryUnitTestCaseBase.setBootstrap(EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap());
   }

   @Before
   public void beforeTest() throws Exception
   {
      // Deploy the default MC XML
      ProxyFactoryRegistryUnitTestCaseBase.getBootstrap().deploy(this.getClass());
   }

   @After
   public void afterTest() throws Exception
   {
      // Undeploy the default MC XML
      ProxyFactoryRegistryUnitTestCaseBase.getBootstrap().undeploy(this.getClass());
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public static EmbeddedTestMcBootstrap getBootstrap()
   {
      return ProxyFactoryRegistryUnitTestCaseBase.bootstrap;
   }

   public static void setBootstrap(EmbeddedTestMcBootstrap bootstrap)
   {
      ProxyFactoryRegistryUnitTestCaseBase.bootstrap = bootstrap;
   }

}
