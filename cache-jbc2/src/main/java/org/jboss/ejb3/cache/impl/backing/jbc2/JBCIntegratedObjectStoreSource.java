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

package org.jboss.ejb3.cache.impl.backing.jbc2;

import javax.transaction.TransactionManager;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheManager;
import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.api.CacheItem;
import org.jboss.ejb3.cache.spi.IntegratedObjectStoreSource;
import org.jboss.ejb3.cache.spi.PassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;
import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;

/**
 * {@link IntegratedObjectStoreSource} that provides instances of
 * {@link JBCIntegratedObjectStore}.
 * 
 * @author Brian Stansberry
 */
public class JBCIntegratedObjectStoreSource<T extends CacheItem> 
   implements IntegratedObjectStoreSource<T>
{
   private CacheManager cacheManager;
   
   public PassivatingIntegratedObjectStore<T, SerializationGroup<T>>  createGroupIntegratedObjectStore(String containerName,
         String cacheConfigName, CacheConfig cacheConfig, TransactionManager transactionManager, SynchronizationCoordinator synchronizationCoordinator)
   {
      Cache<Object, Object> jbc = getJBossCache(cacheConfigName);
      
      String keyBaseSuffix = (containerName == null || containerName.length() == 0) ? "" : "-" + containerName;
      String keyBase = "GroupCache" + keyBaseSuffix;
      return new JBCIntegratedObjectStore<T, SerializationGroup<T>>(jbc, cacheConfig, keyBase, keyBase, true);
   }

   public PassivatingIntegratedObjectStore<T, SerializationGroupMember<T>>  createIntegratedObjectStore(String containerName, String cacheConfigName,
         CacheConfig cacheConfig, TransactionManager transactionManager, SynchronizationCoordinator synchronizationCoordinator)
   {
      Cache<Object, Object> jbc = getJBossCache(cacheConfigName);
      
      return new JBCIntegratedObjectStore<T, SerializationGroupMember<T>>(jbc, cacheConfig, containerName, containerName, false);
   }

   public CacheManager getCacheManager()
   {
      return cacheManager;
   }

   public void setCacheManager(CacheManager cacheManager)
   {
      this.cacheManager = cacheManager;
   }
   
   private Cache<Object, Object> getJBossCache(String cacheConfigName)
   {
      if (cacheManager == null)
         throw new IllegalStateException("CacheManager not installed");
      
      try
      {
         return cacheManager.getCache(cacheConfigName, true);
      }
      catch (RuntimeException re)
      {
         throw re;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to get cache " + cacheConfigName, e);
      }
   }

}
