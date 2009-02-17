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

package org.jboss.ejb3.test.cache.jbc2.mock;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.impl.backing.jbc2.JBCBackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.spi.impl.AbstractStatefulCacheFactory;
import org.jboss.ejb3.test.cache.mock.CacheFactoryNotRegisteredException;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.MockEjb3System;
import org.jboss.logging.Logger;

/**
 * @author Brian Stansberry
 */
public class JBC2MockClusterMember extends MockEjb3System
{
   private static final Logger log = Logger.getLogger(JBC2MockClusterMember.class);   
   
   public static final String DEFAULT_JBC_CONFIG = "standard-session";
   private static final Map<String, String> DEFAULT_ALIAS_MAP = new HashMap<String, String>();
   
   static
   {
      DEFAULT_ALIAS_MAP.put(CacheConfig.DEFAULT_CLUSTERED_OBJECT_NAME, DEFAULT_JBC_CONFIG);
   }
   
   public static Map<String, String> getAliasMap()
   {
      return new HashMap<String, String>(DEFAULT_ALIAS_MAP);
   }
   
   private ClassLoader localClassLoader;
   private JBCBackingCacheEntryStoreSource<MockBeanContext> storeSource;
   
   public JBC2MockClusterMember(TransactionManager tm, 
                            boolean useCoordinator, 
                            CacheType cacheType,
                            JBCBackingCacheEntryStoreSource<MockBeanContext> storeSource)
   {
      this(tm, useCoordinator, new CacheType[] {cacheType}, storeSource);
   }
   
   public JBC2MockClusterMember(TransactionManager tm, 
                            boolean useCoordinator, 
                            CacheType[] cacheTypes,
                            JBCBackingCacheEntryStoreSource<MockBeanContext> storeSource)
   {
      this(tm, useCoordinator, cacheTypes, storeSource, DEFAULT_JBC_CONFIG, getAliasMap());
   }

   public JBC2MockClusterMember(TransactionManager tm, 
         boolean useCoordinator, 
         CacheType[] availableTypes,
         JBCBackingCacheEntryStoreSource<MockBeanContext> storeSource,
         String defaultJBCConfig,
         Map<String, String> aliases)
   {
      super(tm, useCoordinator, availableTypes);
      this.storeSource = storeSource;
      
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      localClassLoader = new ClassLoader(tccl) {};
      
      // Kluge. Rebuild the distributed factory, as the superclass
      // didn't have the maps available when it did it
      for (CacheType type : availableTypes)
      {
         if (type == CacheType.DISTRIBUTED)
            getCacheFactoryRegistry().addCacheFactory(type.mapKey(), buildCacheFactory(type));
      }
      
      try
      {
         AbstractStatefulCacheFactory<MockBeanContext> distFactory = (AbstractStatefulCacheFactory<MockBeanContext>) getCacheFactoryRegistry().getCacheFactory(CacheType.DISTRIBUTED.mapKey());
         distFactory.setDefaultCacheConfigName(defaultJBCConfig);
         distFactory.setCacheConfigAliases(aliases);
      }
      catch (CacheFactoryNotRegisteredException e)
      {
        // no distributed cache!
      }
   }

   public ClassLoader getLocalClassLoader()
   {
      return localClassLoader;
   }   
   
   public boolean setTCCL()
   {
      if (localClassLoader == Thread.currentThread().getContextClassLoader())
         return false;
      
      log.debug("Setting TCCL to " + localClassLoader);
      Thread.currentThread().setContextClassLoader(localClassLoader);
      return true;
   }
   
   public void restoreTCCL()
   {
      ClassLoader current = Thread.currentThread().getContextClassLoader();
      if (current == localClassLoader)
      {         
         Thread.currentThread().setContextClassLoader(localClassLoader.getParent());
         log.debug("Restored TCCL to " + localClassLoader.getParent());
      }
      else if (current != localClassLoader.getParent())
      {
         throw new IllegalStateException("Current TCCL is neither localClassLoader nor its parent");
      }
   }

   @Override
   protected BackingCacheEntryStoreSource<MockBeanContext> getDistributedStoreSource()
   {
      return storeSource;
   }

   @Override
   public MockBeanContainer deployBeanContainer(String containerName, String parentContainerName,
         CacheType cacheType, MockCacheConfig cacheConfig, String xpc) throws Exception
   {
      boolean tcclSet = setTCCL();
      try
      {
         return super.deployBeanContainer(containerName, parentContainerName, cacheType, cacheConfig, xpc);
      }
      finally
      {
         if (tcclSet)
            restoreTCCL();
      }
   }

   @Override
   public MockBeanContainer getMockBeanContainer(String containerName)
   {
      boolean tcclSet = setTCCL();
      try
      {
         return super.getMockBeanContainer(containerName);
      }
      finally
      {
         if (tcclSet)
            restoreTCCL();
      }
   }
   
}
