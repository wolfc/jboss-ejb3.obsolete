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

package org.jboss.ejb3.cache.spi.impl;

import java.util.Map;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.StatefulCacheFactory;
import org.jboss.ejb3.cache.spi.PassivationExpirationCoordinator;
import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;

/**
 * Abstract superclass of {@link StatefulCacheFactory} implementations.
 * 
 * @author Brian Stansberry
 */
public abstract class AbstractStatefulCacheFactory<T extends CacheItem>
   implements StatefulCacheFactory<T>
{
   public static final int DEFAULT_PASSIVATION_EXPIRATION_INTERVAL = 10;
   
   private TransactionManager transactionManager;
   private SynchronizationCoordinator synchronizationCoordinator;
   private PassivationExpirationCoordinator passivationExpirationCoordinator;
   private int defaultPassivationExpirationInterval = DEFAULT_PASSIVATION_EXPIRATION_INTERVAL;
   private String defaultCacheConfigName;
   private Map<String, String> cacheConfigAliases;
   private boolean strictGroups = true;
   
   // -------------------------------------------------------------  Properties
   
   /**
    * Gets the transaction manager to be used by the caches.
    */
   public TransactionManager getTransactionManager()
   {
      return transactionManager;
   }
   
   /**
    * Sets the transaction manager to be used by the caches. Must be
    * set for caching to work.
    */
   public void setTransactionManager(TransactionManager transactionManager)
   {
      this.transactionManager = transactionManager;
   }
   
   /**
    * Gets the {@link SynchronizationCoordinator} used by this factory.
    */   
   public SynchronizationCoordinator getSynchronizationCoordinator()
   {
      return synchronizationCoordinator;
   }

   /**
    * Sets the {@link SynchronizationCoordinator} used by this factory.
    */   
   public void setSynchronizationCoordinator(SynchronizationCoordinator synchronizationCoordinator)
   {
      this.synchronizationCoordinator = synchronizationCoordinator;
   }

   /**
    * Gets the coordinator of passivation/expiration processes. If 
    * <code>null</code>, each cache will manager passivation/expiration
    * with their own thread.
    * 
    * @return the coordinator. May be <code>null</code>.
    */
   public PassivationExpirationCoordinator getPassivationExpirationCoordinator()
   {
      return passivationExpirationCoordinator;
   }
   
   /**
    * Sets the coordinator of passivation/expiration processes.
    */
   public void setPassivationExpirationCoordinator(PassivationExpirationCoordinator coordinator)
   {
      this.passivationExpirationCoordinator = coordinator;
   }

   /**
    * Gets the interval at which passivation/expiration tasks will execute
    * if no {@link PassivationExpirationCoordinator} is provided.
    * 
    * @return the interval, in seconds
    */
   public int getDefaultPassivationExpirationInterval()
   {
      return defaultPassivationExpirationInterval;
   }

   /**
    * Sets the interval at which passivation/expiration tasks will execute
    * if no {@link PassivationExpirationCoordinator} is provided.
    * 
    * @param interval the interval, in seconds
    */
   public void setDefaultPassivationExpirationInterval(int interval)
   {
      this.defaultPassivationExpirationInterval = interval;
   }

   /**
    * Gets the value to internally substitute for {@link CacheConfig#name()} 
    * if that attribute returns an empty string.
    * 
    * @return the substitute value. May be <code>null</code>
    */
   public String getDefaultCacheConfigName()
   {
      return defaultCacheConfigName;
   }

   /**
    * Sets the value to internally substitute for {@link CacheConfig#name()} 
    * if that attribute returns an empty string.
    */
   public void setDefaultCacheConfigName(String defaultCacheConfigName)
   {
      this.defaultCacheConfigName = defaultCacheConfigName;
   }

   /**
    * Gets a map of substitute (alias) values for {@link CacheConfig#name()}
    * values.  Map key is the {@link CacheConfig#name()}, value is the substitute
    * name to use instead.
    * 
    * @return the aliases. May be <code>null</code>.
    */
   public Map<String, String> getCacheConfigAliases()
   {
      return cacheConfigAliases;
   }

   /**
    * Sets a map of substitute (alias) values for {@link CacheConfig#name()}
    * values.  Map key is the {@link CacheConfig#name()}, value is the substitute
    * name to use instead.
    */
   public void setCacheConfigAliases(Map<String, String> cacheConfigAliases)
   {
      this.cacheConfigAliases = cacheConfigAliases;
   }
   
   public boolean getStrictGroups()
   {
      return strictGroups;
   }

   public void setStrictGroups(boolean strictGroups)
   {
      this.strictGroups = strictGroups;
   }
   
   
   // -----------------------------------------------------------------  Public

   public String getCacheConfigName(CacheConfig cacheConfig)
   {
      String name = cacheConfig.name();
      String substitute = null;
      if (cacheConfigAliases != null)
      {
         substitute = cacheConfigAliases.get(name);
      }
      if (substitute == null && name.length() == 0)
      {
         substitute = defaultCacheConfigName;
      }
      
      return substitute == null ? name : substitute;
   }
   
   public void start()
   {
      assert transactionManager != null : "transactionManager is null";
      
      if (getSynchronizationCoordinator() == null)
         setSynchronizationCoordinator(new SynchronizationCoordinatorImpl());
   }
   
   public void stop()
   {
      // no-op
   }

}
