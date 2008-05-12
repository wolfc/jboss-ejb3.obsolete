package org.jboss.ejb3.entity;

import java.util.Properties;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.jboss.cache.CacheManager;
import org.jboss.cache.CacheStatus;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.ha.framework.server.CacheManagerLocator;

class JBCCacheFactory extends TransactionalCacheFactory
{
   private CacheManager cacheManager;
   private String cacheName;
   private org.jboss.cache.Cache cache;
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
         return new OptimisticJBCCache(cache, regionName, regionPrefix);
      }
      else
      {
         return new JBCCache(cache, regionName, regionPrefix,
                             TxUtil.getTransactionManager());
      }
   }
   
   public void stop()
   {
      if (cache != null)
         cacheManager.releaseCache(cacheName);
   }
   
   public boolean isOptimistic()
   {
      return optimistic;
   }

}
