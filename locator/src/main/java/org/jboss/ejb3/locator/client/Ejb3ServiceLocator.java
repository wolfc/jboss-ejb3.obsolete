/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.ejb3.locator.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Ejb3ServiceLocator implements ServiceLocator
{
   // Class Members

   private static final Log logger = LogFactory.getLog(Ejb3ServiceLocator.class);

   // Instance Members

   /**
    * Object cache used for storing stubs to remote services/beans, indexed by unique business interface
    */
   private Map<Class<?>, Object> objectCache = Collections.synchronizedMap(new HashMap<Class<?>, Object>());

   // Required Implementations

   /**
    * Obtains a stub to the the SLSB service with the specified business 
    * interface.  If this is the first request for this service, it will 
    * be obtained from JNDI and placed in a cache such that subsequent 
    * requests will not require the overhead of a JNDI lookup. 
    * 
    * @param <T>
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the configured local/remote hosts, or if the
    *   specified class is no an interface 
    */
   public <T> T getStatelessBean(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException
   {
      // Log
      logger.trace("getStatelessService requesting " + clazz.getName());

      // Obtain object, from cache if possible
      return this.getObject(clazz, true);
   }

   /**
    * Obtains a stub to the the SFSB with the specified business 
    * interface.  This call will always result in a call to JNDI 
    * for a new stub; no caching will take place
    * 
    * @param <T>
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the configured local/remote hosts, or if the
    *   specified class is no an interface 
    */
   public <T> T getStatefulBean(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException
   {
      // Log
      logger.trace("getStatefulBean requesting " + clazz.getName());

      // Obtain object, never from cache (Stateful stubs must be unique)
      return this.getObject(clazz, false);
   }

   /**
    * Obtains a stub to the the JMX (MBean, Singleton) service with 
    * the specified business interface.  If this is the first 
    * request for this service, it will be obtained from JNDI and 
    * placed in a cache such that subsequent requests will not 
    * require the overhead of a JNDI lookup.  Convenience
    * method; equivalent to <code>getStatelessService</code>
    * 
    * @param <T>
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the configured local/remote hosts, or if the
    *   specified class is no an interface 
    */
   public <T> T getJmxService(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException
   {
      // Log
      logger.trace("getJmxService requesting " + clazz.getName());

      // Obtain object, from cache if possible
      return this.getObject(clazz, true);
   }

   // Internal Methods

   /**
    * Obtains the object associated with the specified business interface.  
    * This may be obtained from the cache if possible when the "useCache" 
    * flag is set, otherwise caching will be bypassed and a unique lookup 
    * will take place on each subsequent request.
    * 
    * @param <T>
    * @param clazz The business interface of the desired service
    * @param useCache Whether or not to retrieve the object from the cache, if possible.
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the configured local/remote hosts, or if the
    *   specified class is no an interface 
    */
   protected <T> T getObject(Class<T> clazz, boolean useCache) throws Ejb3NotFoundException, IllegalArgumentException
   {
      // Ensure specified business interface is an interface
      if (!clazz.isInterface())
      {
         throw new IllegalArgumentException("Specified class \"" + clazz.getName() + "\" is not an interface");
      }

      // If caching is enabled and the object exists in the cache
      if (useCache && this.isObjectCached(clazz))
      {
         // Obtain from cache
         T obj = this.getObjectFromCache(clazz);

         // Ensure implements specified interface
         if (!clazz.isAssignableFrom(obj.getClass()))
         {
            // Object was placed into cache under incorrect key; integrity of cache broken
            throw new ServiceLocatorException("Object in cache under key " + clazz.getName()
                  + " does not implement this interface; cache integrity compromised.");
         }

         // Return from cache
         return obj;
      }

      // Obtain from the remote host
      T obj = this.getObject(clazz);

      // If caching is enabled 
      if (useCache)
      {
         // Place into the cache
         this.addInterfaceAndSuperinterfacesToCache(clazz, obj);
      }

      // Return
      return obj;

   }

   /**
    * Determines whether an object with the specified business interface 
    * is currently cached
    * 
    * @param clazz
    * @return
    */
   private boolean isObjectCached(Class<?> clazz)
   {
      return this.objectCache.containsKey(clazz);
   }

   /**
    * Obtains the specified object from the cache
    * 
    * @param <T>
    * @param clazz
    * @return
    */
   @SuppressWarnings(value = "unchecked")
   private <T> T getObjectFromCache(Class<T> clazz)
   {
      // Obtain
      T obj = (T) this.objectCache.get(clazz);

      // Ensure present
      if (obj == null)
      {
         throw new ServiceLocatorException("Call to retrieve object implementing " + clazz.getName()
               + " from cache failed; object is not cached.");
      }

      // Return
      return obj;
   }

   /**
    * Adds the specified class and all superclasses to the cache of bound
    * objects
    * 
    * @param interfaze
    * @param obj
    */
   private <T> void addInterfaceAndSuperinterfacesToCache(Class<T> interfaze, T obj)
   {
      // Ensure not already cached, escape
      if (!this.isObjectCached(interfaze))
      {
         // Add the object to the list of objects implementing this
         // interface
         this.objectCache.put(interfaze, obj);
      }

      // Add all super interfaces recursively
      for (Class<T> superInterface : interfaze.getInterfaces())
      {
         this.addInterfaceAndSuperinterfacesToCache(superInterface, obj);
      }
   }

   // Contracts

   /**
    * Obtains the object associated with the specified business interface 
    * from one of the configured remote hosts.
    * 
    * @param <T>
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the configured local/remote hosts, or if the
    *   specified class is not an interface 
    */
   public abstract <T> T getObject(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException;

   /**
    * Obtains the object associated with the specified business interface 
    * from the host with the specified ID.
    * 
    * @param <T>
    * @param hostId The ID of the host from which to obtain the 
    *   object with the specified business interface
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on the specified host
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the specified host, if the
    *   specified class is not an interface, or if the specified host ID is not 
    *   valid for one of the configured hosts 
    */
   public abstract <T> T getObject(String hostId, Class<T> clazz) throws Ejb3NotFoundException,
         IllegalArgumentException;

}
