package org.jboss.ejb3.entity;

import java.util.Properties;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.jboss.cache.Version;

public abstract class TransactionalCacheFactory
{

   /**
    * Gets and configures a concrete TransactionalCacheFactory suitable for 
    * interoperation with the version of JBoss Cache visible on the classpath.
    * 
    * @param hibernateConfig properties to use to {@link #configure(Properties) configure}
    *                        the factory.
    * @return the factory
    * 
    * @throws CacheException
    */
   public static TransactionalCacheFactory getFactory(Properties hibernateConfig) throws CacheException
   {
      String factoryClass = null;
      short version = Version.getVersionShort();
      if (version >= Version.getVersionShort("2.0.0.GA") || version <= 0)
      {
         factoryClass = "org.jboss.ejb3.entity.JBCCacheFactory";
      }
      else
      {
         // TODO write a factory for the old hibernate stuff
         // needs to be in a separate code tree from JBCCacheFactory as 
         // TreeCacheMBean is no longer available in 2.0
         throw new IllegalStateException("Cannot create factory for JBC 1.x");
      }
      
      try
      {
         Class clazz = Thread.currentThread().getContextClassLoader().loadClass(factoryClass);
         TransactionalCacheFactory factory = (TransactionalCacheFactory) clazz.newInstance();
         factory.configure(hibernateConfig);
         
         return factory;
      }
      catch (CacheException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new CacheException(e);
      }
      
   }
   
   public abstract void start();
   
   public abstract void stop();
   
   /**
    * Construct and configure the Cache representation of a named cache region.
    *
    * @param regionName the name of the cache region
    * @param properties configuration settings
    * @return The Cache representation of the named cache region.
    * @throws org.hibernate.cache.CacheException
    *          Indicates an error building the cache region.
    */
   public abstract Cache buildCache(String regionName, Properties properties) throws CacheException;
   
   /**
    * Configures the factory using the Hibernate configuration properties.  
    * Called by {@link #getFactory(Properties)}.
    *  
    * @param hibernateConfig the Hibernate configuration properties
    */
   protected abstract void configure(Properties hibernateConfig);
   
   /**
    * Gets whether the underlying JBoss Cache instance is configured
    * for optimistic locking.
    * 
    * @return <code>true</code> if the JBoss Cache uses optimistic locking;
    *         <code>false</code> if it uses pessimistic locking
    */
   public abstract boolean isOptimistic();
}
