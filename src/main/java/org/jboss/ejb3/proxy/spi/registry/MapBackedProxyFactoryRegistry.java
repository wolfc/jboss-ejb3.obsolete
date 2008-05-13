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
package org.jboss.ejb3.proxy.spi.registry;

import java.util.Map;

import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.logging.Logger;

/**
 * MapBackedProxyFactoryRegistry
 *
 * Base upon which Proxy Factory Registry implementations may build.
 * 
 * The type parameter T defines the implementation class to be used as the backing 
 * resource for holding the Proxy Factories.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class MapBackedProxyFactoryRegistry<T extends Map<String, ProxyFactory>>
      implements
         ProxyFactoryRegistry
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(MapBackedProxyFactoryRegistry.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private T proxyFactories;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create a new registry backed by an object of same type 
    * as the specified mock object
    * 
    * @param mockFactories An object to denote the type 
    * to be used in holding the backing registry
    */
   @SuppressWarnings("unchecked")
   public MapBackedProxyFactoryRegistry(T mockFactories)
   {
      // Log
      log.debug("Using " + this.getClass().getName() + " implementation of " + ProxyFactoryRegistry.class.getName());

      try
      {
         this.setProxyFactories((T) mockFactories.getClass().newInstance());
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Determines whether a ProxyFactory is registered at the specified key
    * 
    * @param key
    * @return
    */
   public boolean isRegistered(String key)
   {
      return this.getProxyFactories().containsKey(key);
   }

   /**
    * Obtains the ProxyFactory registered with the specified key
    * 
    * @param key
    * @return
    * @throws ProxyFactoryNotRegisteredException If no Proxy Factory is registered under the specified key
    */
   public ProxyFactory getProxyFactory(String key) throws ProxyFactoryNotRegisteredException
   {
      // Check is exists
      boolean registered = this.isRegistered(key);

      // Ensure a ProxyFactory was found for this key
      if (!registered)
      {
         throw new ProxyFactoryNotRegisteredException("No " + ProxyFactory.class.getSimpleName()
               + " could not be found registered under the key\"" + key + "\"");
      }

      // Return
      ProxyFactory factory = this.getProxyFactories().get(key);
      assert factory != null : ProxyFactory.class.getSimpleName() + " is marked as registered under key \"" + key
            + "\" but returned null on lookup";
      return factory;
   }

   /**
    * Register the specified Proxy Factory with the 
    * specified key.  Is additionally responsible for calling the
    * ProxyFactory.start()
    * 
    * @param key
    * @param value
    * @throws ProxyFactoryAlreadyRegisteredException When a proxy factory is already registered under the
    *       specified key
    */
   public void registerProxyFactory(String key, ProxyFactory value) throws ProxyFactoryAlreadyRegisteredException
   {
      // Ensure not already registered
      if (this.getProxyFactories().containsKey(key))
      {
         throw new ProxyFactoryAlreadyRegisteredException("Cannot register " + value + " as there is already a "
               + value.getClass().getSimpleName() + " associated with key \"" + key + "\"");
      }

      // Start
      try
      {
         value.start();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error in starting " + value, e);
      }

      // Place in the registry
      this.getProxyFactories().put(key, value);
   }

   /**
    * Deregister the specified Proxy Factory with the 
    * specified key.  Is additionally responsible for calling
    * ProxyFactory.stop()
    * 
    * @param key
    * @throws ProxyFactoryNotRegisteredException If no Proxy Factory is registered under the specified key 
    */
   public void deregisterProxyFactory(String key) throws ProxyFactoryNotRegisteredException
   {
      // Deregister
      ProxyFactory proxyFactory = this.getProxyFactories().remove(key);

      // Ensure registered
      if (proxyFactory == null)
      {
         throw new ProxyFactoryNotRegisteredException("Cannot deregister a " + ProxyFactory.class.getName()
               + " associated with key \"" + key + "\" as the key is currently not registered");
      }

      // Stop
      try
      {
         proxyFactory.stop();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public T getProxyFactories()
   {
      return proxyFactories;
   }

   private void setProxyFactories(T proxyFactories)
   {
      this.proxyFactories = proxyFactories;
   }

}
