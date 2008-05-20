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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.StandardQueryCache;
import org.hibernate.cache.UpdateTimestampsCache;
import org.hibernate.util.PropertiesHelper;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.Region;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.config.Configuration.CacheMode;

/**
 * Base superclass for a {@link Cache} implementation that uses a 2.x or later 
 * release of JBoss Cache.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public abstract class JBCCacheBase
{
   public static final String QUERY_CACHE_LOCAL_ONLY_PROP = "hibernate.cache.region.jbc2.query.localonly";
   
   protected static final String ITEM = "item";
 
   protected org.jboss.cache.Cache<Object, Object> cache;
   protected final String regionName;
   protected final Fqn<String> regionFqn;
   protected final TransactionManager transactionManager;
   protected boolean localOnlyQueries;
   protected boolean forTimestamps;
   protected boolean forceAsync;
   protected Node<Object, Object> regionRoot;
   protected final Object regionRootMutex = new Object();


   public JBCCacheBase(org.jboss.cache.Cache<Object, Object> cache, String regionName, 
                       String regionPrefix, TransactionManager transactionManager,
                       Properties properties) 
   throws CacheException 
   {
       this.cache = cache;
       this.regionName = regionName;
       this.regionFqn = Fqn.fromString(SecondLevelCacheUtil.createRegionFqn(regionName, regionPrefix));
       this.transactionManager = transactionManager;
       this.forTimestamps = regionName.contains(UpdateTimestampsCache.class.getName());
       CacheMode mode = cache.getConfiguration().getCacheMode();
       if (forTimestamps)
       {          
          if (mode == CacheMode.INVALIDATION_ASYNC || mode == CacheMode.INVALIDATION_SYNC)
          {
             throw new IllegalStateException("Cache is configured for " + mode + 
                                             "; not supported for a timestamps cache");
          }
          
          forceAsync = (mode == CacheMode.REPL_SYNC);
       }
       
       // We don't want to waste effort setting an option if JBC is
       // already in LOCAL mode. If JBC is REPL_(A)SYNC then check
       // if they passed a config option to disable query replication
       if (mode != CacheMode.LOCAL)
       {
          localOnlyQueries = PropertiesHelper.getBoolean(QUERY_CACHE_LOCAL_ONLY_PROP, properties, false);
          // If its the standard query region with no prefix, its possibly shared
          // between classloaders, so we make it local only
          localOnlyQueries = localOnlyQueries || 
                             StandardQueryCache.class.getName().equals(regionName);
       }
       
       activateLocalClusterNode();
   }
   
   private void activateLocalClusterNode()
   {       
      // Regions can get instantiated in the course of normal work (e.g.
      // a named query region will be created the first time the query is
      // executed), so suspend any ongoing tx
      Transaction tx = suspend();
      try {
         Configuration cfg = cache.getConfiguration();
         if (cfg.isUseRegionBasedMarshalling()) {                

            Region jbcRegion = cache.getRegion(regionFqn, true);

            // Only register the classloader if it's not a shared region.  
            // If it's shared, no single classloader is valid
            boolean shared = SecondLevelCacheUtil.isSharedClassLoaderRegion(regionName);
            if (!shared)
            {
               ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
               if (classLoader == null) {
                  classLoader = getClass().getClassLoader();
               }
               jbcRegion.registerContextClassLoader(classLoader);
            }

            if ( !jbcRegion.isActive() ) 
            {
               boolean fetchState = cfg.isFetchInMemoryState();
               try
               {
                  // We don't want a state transfer for a shared region,
                  // as it can include classes from multiple scoped classloaders
                  if (shared)
                     cfg.setFetchInMemoryState(false);

                  jbcRegion.activate();
               }
               finally
               {
                  // Restore the normal state transfer setting
                  if (shared)
                     cfg.setFetchInMemoryState(fetchState);              
               }

            }
         }

         regionRoot = createRegionRootNode();
      }
      catch (Exception e)
      {
         throw SecondLevelCacheUtil.convertToHibernateException(e);
      }
      finally {
         resume(tx);
      }        
    }

   protected abstract void establishRegionRootNode();

   protected abstract Node<Object, Object> createRegionRootNode();

   /**
     * Checks for the validity of the root cache node for this region,
     * creating a new one if it does not exist or is invalid, and also
     * ensuring that the root node is marked as resident.  Suspends any 
     * transaction while doing this to ensure no transactional locks are held 
     * on the region root.
     * 
     * TODO remove this once JBCACHE-1250 is resolved.
     */
   protected void ensureRegionRootExists()
   {       
       if (regionRoot == null || !regionRoot.isValid())
       {
          synchronized (regionRootMutex)
          {
             // If we've been blocking for the mutex, perhaps another
             // thread has already reestablished the root.
             // In case the node was reestablised via replication, confirm it's 
             // marked "resident" (a status which doesn't replicate)
             if (regionRoot != null && regionRoot.isValid()) {
                return;
             }
             
             establishRegionRootNode();
          }
       }
       
       // Fix up the resident flag
       if (regionRoot != null && regionRoot.isValid() && !regionRoot.isResident())
          regionRoot.setResident(true);
    }

   protected void resume(Transaction tx)
   {
      if (tx != null)
      {
         try {
            transactionManager.resume(tx);
         }
         catch (Exception e) {
            throw new CacheException("Could not resume transaction", e);
         }
      }
   }

   protected Transaction suspend()
   {
      Transaction tx = null;
      if (transactionManager != null)
      {
         try {
            tx = transactionManager.suspend();
         }
         catch (SystemException se) {
            throw new CacheException("Could not suspend transaction", se);
         }
      }
      return tx;
   }
   
   protected void inactivateCacheRegion() throws CacheException
   {
      Region region = cache.getRegion(regionFqn, false);
      if (region != null && region.isActive())
      {
         try
         {
            region.deactivate();
            region.unregisterContextClassLoader();
         }
         catch (Exception e)
         {
            throw SecondLevelCacheUtil.convertToHibernateException(e);
         }
      }        
   }

   public void lock(Object key) throws CacheException {
       throw new UnsupportedOperationException( "TreeCache is a fully transactional cache" + regionName );
   }

   public void unlock(Object key) throws CacheException {
       throw new UnsupportedOperationException( "TreeCache is a fully transactional cache: " + regionName );
   }

   public long nextTimestamp() {
       return System.currentTimeMillis() / 100;
   }

   public int getTimeout() {
       return 600; //60 seconds
   }

   public String getRegionName() {
       return regionName;
   }

   public long getSizeInMemory() {
       return -1;
   }

   public long getElementCountInMemory() {
       try {
           return getChildrenNames().size();
       }
       catch (Exception e) {
           throw SecondLevelCacheUtil.convertToHibernateException(e);
       }
   }

   public long getElementCountOnDisk() {
       return 0;
   }

   public Map<Object, Object> toMap() {
       try {
           Map<Object, Object> result = new HashMap<Object, Object>();
           for (Object key : getChildrenNames() ) {
               result.put(key, cache.get( new Fqn( regionFqn, key ), ITEM ));
           }
           return result;
       }
       catch (Exception e) {
           throw SecondLevelCacheUtil.convertToHibernateException(e);
       }
   }
   
   private Set<Object> getChildrenNames()
   {
      try {
         return regionRoot == null ? new HashSet<Object>() : regionRoot.getChildrenNames();
      }
      catch (Exception e) {
         throw SecondLevelCacheUtil.convertToHibernateException(e);
      }   
   }

}