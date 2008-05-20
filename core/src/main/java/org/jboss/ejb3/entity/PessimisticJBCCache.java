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

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.QueryKey;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.config.Option;
import org.jboss.cache.lock.TimeoutException;
import org.jboss.ejb3.entity.OptimisticJBCCache.NonLockingDataVersion;

/**
 * {@link Cache} implementation that uses a 2.x or later release of 
 * JBoss Cache configured for pessimistic locking.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class PessimisticJBCCache extends JBCCacheBase implements Cache
{
   public PessimisticJBCCache(org.jboss.cache.Cache<Object, Object> cache, String regionName, 
                              String regionPrefix, TransactionManager transactionManager,
                              Properties properties) 
   throws CacheException {
       super(cache, regionName, regionPrefix, transactionManager, properties);
   }

   @Override
   protected void establishRegionRootNode()
   {            
      // For pessimistic locking, we just want to toss out our ref
      // to any old invalid root node and get the latest (may be null)            
      regionRoot = cache.getRoot().getChild( regionFqn );               
   }

   @Override
   protected Node<Object, Object> createRegionRootNode()
   {
      Node<Object, Object> root = cache.getRoot();
      Node<Object, Object> targetNode = root.getChild( regionFqn );
      if (targetNode == null || !targetNode.isValid()) {
         cache.getInvocationContext().getOptionOverrides().setDataVersion(NonLockingDataVersion.INSTANCE);          
         targetNode = root.addChild( regionFqn );    
      }

      // Never evict this node
      targetNode.setResident(true);

      return targetNode;
   }

    public Object get(Object key) throws CacheException {
        Transaction tx = suspend();
        try {
            ensureRegionRootExists();
            if (key instanceof QueryKey) {
               cache.getInvocationContext().getOptionOverrides().setLockAcquisitionTimeout(0);  
            }
            return cache.get( new Fqn( regionFqn, key ), ITEM );
        }
        catch (TimeoutException e) {
           if (key instanceof QueryKey)
              return null;
           throw SecondLevelCacheUtil.convertToHibernateException(e);
        }
        finally {
            resume( tx );
        }
    }
    
    public Object read(Object key) throws CacheException {
        try {
            ensureRegionRootExists();
            return cache.get( new Fqn( regionFqn, key ), ITEM );
        }
        catch (Exception e) {
            throw SecondLevelCacheUtil.convertToHibernateException(e);
        }
    }

    public void update(Object key, Object value) throws CacheException {
        try {
            ensureRegionRootExists();
        	if (localOnlyQueries && key instanceof QueryKey) {
               cache.getInvocationContext().getOptionOverrides().setCacheModeLocal(true);
            }
            cache.put( new Fqn( regionFqn, key ), ITEM, value );
        }
        catch (Exception e) {
            throw SecondLevelCacheUtil.convertToHibernateException(e);
        }
    }

    public void put(Object key, Object value) throws CacheException {
        Transaction tx = null;
        try {
            if (forTimestamps) {
               // For timestamps, we suspend and send async
               tx = suspend();
               ensureRegionRootExists();
               // Timestamps don't use putForExternalRead, but are async
               if (forceAsync) // else the cache already is async or local
               {
                  cache.getInvocationContext().getOptionOverrides().setForceAsynchronous(true);
               }
               cache.put(new Fqn( regionFqn, key ), ITEM, value);               
            }
            else if (key instanceof QueryKey) {
               // For queries we use a short timeout and ignore failures
               ensureRegionRootExists();
               Option option = new Option();
               option.setCacheModeLocal(localOnlyQueries);
               option.setLockAcquisitionTimeout(2);
               cache.getInvocationContext().setOptionOverrides(option);
               cache.put(new Fqn( regionFqn, key ), ITEM, value);
            }
            else {
               ensureRegionRootExists();
               cache.putForExternalRead(new Fqn( regionFqn, key ), ITEM, value);
            }
        }
        catch (TimeoutException te) {
           if (!(key instanceof QueryKey))
              throw SecondLevelCacheUtil.convertToHibernateException(te);
        }
        catch (Exception e) {
            throw SecondLevelCacheUtil.convertToHibernateException(e);
        }
        finally {
            resume( tx );
        }
    }

    public void remove(Object key) throws CacheException {
        try {
           ensureRegionRootExists();
           if (localOnlyQueries && key instanceof QueryKey) {
              cache.getInvocationContext().getOptionOverrides().setCacheModeLocal(true);
           }
           cache.removeNode( new Fqn( regionFqn, key ) );
        }
        catch (Exception e) {
            throw SecondLevelCacheUtil.convertToHibernateException(e);
        }
    }

    public void clear() throws CacheException {
        try {
            cache.removeNode( regionFqn );
        }
        catch (Exception e) {
            throw SecondLevelCacheUtil.convertToHibernateException(e);
        }
    }

    public void destroy() throws CacheException {
        try {
            // NOTE : evict() operates locally only (i.e., does not propogate
            // to any other nodes in the potential cluster).  This is
            // exactly what is needed when we destroy() here; destroy() is used
            // as part of the process of shutting down a SessionFactory; thus
            // these removals should not be propogated
           // FIXME NPE bug in 2.0.0.ALPHA1, so we don't use evict 'til fixed
//            cache.evict( regionFqn, true );
           cache.getInvocationContext().getOptionOverrides().setCacheModeLocal(true);
           cache.removeNode( regionFqn );
           
           if (cache.getConfiguration().isUseRegionBasedMarshalling() 
                 && !SecondLevelCacheUtil.isSharedClassLoaderRegion(regionName))
           {
              inactivateCacheRegion();
           }
        }
        catch( Exception e ) {
            throw SecondLevelCacheUtil.convertToHibernateException( e );
        }
    }
    
    public String toString() {
        return "PessimisticJBCCache(" + regionName + ')';
    }
}
