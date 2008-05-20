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
package org.jboss.ejb3.entity;

import java.util.Properties;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.jboss.cache.CacheManager;
import org.jboss.cache.CacheStatus;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.ha.framework.server.CacheManagerLocator;
import org.jboss.logging.Logger;

/**
 * Concrete implementation of TransactionalCacheFactory meant for use with
 * JBoss Cache 2.x
 * 
 * @author Brian Stansberry
 */
class JBCCacheFactory extends TransactionalCacheFactory
{
   private static final Logger log = Logger.getLogger(JBCCacheFactory.class);
   
   private CacheManager cacheManager;
   private String cacheName;
   private org.jboss.cache.Cache<Object, Object> cache;
   private boolean optimistic;
   
   JBCCacheFactory()
   {
       
   }
   
   protected void configure(Properties hibernateConfig)
   {
       try
       {
          cacheManager = CacheManagerLocator.getCacheManagerLocator().getCacheManager(null);
          
          cacheName = (String) hibernateConfig.get(TreeCacheProviderHook.HIBERNATE_CACHE_CONFIG_NAME_PROPERTY);
          if (cacheName == null)
          {
             cacheName = (String) hibernateConfig.get(TreeCacheProviderHook.HIBERNATE_CACHE_OBJECT_NAME_PROPERTY);
          }
          if (cacheName == null)
          {
             cacheName = TreeCacheProviderHook.DEFAULT_MBEAN_OBJECT_NAME;
          }          
       }
       catch (Exception e)
       {
          throw new CacheException(e);
       }      
   }
   
   public void start()
   {
      try
      {
         cache = cacheManager.getCache(cacheName, true);
         optimistic = cache.getConfiguration().isNodeLockingOptimistic();
         if (cache.getCacheStatus() != CacheStatus.STARTED)
         {
            if (cache.getCacheStatus() != CacheStatus.CREATED)
            {
               cache.create();
            }
            
            if (cache.getConfiguration().getRuntimeConfig().getTransactionManager() == null
                  && cache.getConfiguration().getTransactionManagerLookupClass() == null)
            {
               cache.getConfiguration().getRuntimeConfig().setTransactionManager(TxUtil.getTransactionManager());
            }
            cache.start();
         }
      }
      catch (Exception e)
      {
         throw new CacheException("Problem accessing cache " + cacheName, e);
      }
   }
   
   public Cache buildCache(String regionName, Properties properties) throws CacheException
   {
      String regionPrefix = properties.getProperty(SecondLevelCacheUtil.HIBERNATE_CACHE_REGION_PREFIX);
      
      if (optimistic)
      {
         return new OptimisticJBCCache(cache, regionName, regionPrefix,
                                       TxUtil.getTransactionManager(),
                                       properties);
      }
      else
      {
         return new PessimisticJBCCache(cache, regionName, regionPrefix,
                                        TxUtil.getTransactionManager(),
                                        properties);
      }
   }
   
   public void stop()
   {
      if (cache != null)
      {
         cacheManager.releaseCache(cacheName);
         log.debug("Cache " + cacheName + " released");
      }
   }
   
   public boolean isOptimistic()
   {
      return optimistic;
   }

}
