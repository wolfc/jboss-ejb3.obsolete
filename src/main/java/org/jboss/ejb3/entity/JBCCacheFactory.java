package org.jboss.ejb3.entity;

import java.util.Properties;

import javax.management.ObjectName;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.jboss.cache.jmx.CacheJmxWrapperMBean;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;

class JBCCacheFactory extends TransactionalCacheFactory
{
   private org.jboss.cache.Cache cache;
   private boolean optimistic;
   
   JBCCacheFactory()
   {
       
   }
   
   protected void configure(Properties hibernateConfig)
   {
       try
       {
          String cacheName = (String) hibernateConfig.get(TreeCacheProviderHook.HIBERNATE_CACHE_OBJECT_NAME_PROPERTY);
          if (cacheName == null)
          {
             cacheName = TreeCacheProviderHook.DEFAULT_MBEAN_OBJECT_NAME;
          }
          ObjectName mbeanObjectName = new ObjectName(cacheName);
          CacheJmxWrapperMBean mbean = (CacheJmxWrapperMBean) MBeanProxyExt.create(CacheJmxWrapperMBean.class, mbeanObjectName, MBeanServerLocator.locateJBoss());
          cache = mbean.getCache();
          optimistic = cache.getConfiguration().isNodeLockingOptimistic();
       }
       catch (Exception e)
       {
          throw new CacheException(e);
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
   
   public boolean isOptimistic()
   {
      return optimistic;
   }

}
