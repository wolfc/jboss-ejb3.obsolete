/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import java.util.Comparator;
import java.util.Properties;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;


import org.hibernate.cache.CacheException;
import org.hibernate.cache.OptimisticCache;
import org.hibernate.cache.OptimisticCacheSource;
import org.hibernate.cache.QueryKey;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.NodeSPI;
import org.jboss.cache.optimistic.DataVersion;
import org.jboss.cache.config.Option;
import org.jboss.cache.lock.TimeoutException;
import org.jboss.logging.Logger;

/**
 * Represents a particular region within the given JBossCache TreeCache
 * utilizing TreeCache's optimistic locking capabilities.
 *
 * @see org.hibernate.cache.OptimisticTreeCacheProvider for more details
 *
 * @author Steve Ebersole
 * @author Brian Stansberry
 */
public class OptimisticJBCCache extends JBCCacheBase implements OptimisticCache 
{

   private static final Logger log = Logger.getLogger(OptimisticJBCCache.class);
   
   private OptimisticCacheSource source;

   public OptimisticJBCCache(org.jboss.cache.Cache<Object, Object> cache, 
         String regionName, String regionPrefix, 
         TransactionManager transactionManager,
         Properties properties) 
   throws CacheException {
      super(cache, regionName, regionPrefix, transactionManager, properties);
   }

   @Override
   protected void establishRegionRootNode()
   {
      // Don't hold a transactional lock for this 
      Transaction tx = suspend();
      Node<Object, Object> newRoot = null;
      try {
         // Make sure the root node for the region exists and 
         // has a DataVersion that never complains
         newRoot = createRegionRootNode();
      }
      finally {
         resume(tx);
         regionRoot = newRoot;
      }
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
      else if (targetNode instanceof NodeSPI) {
         // FIXME Hacky workaround to JBCACHE-1202
         if ( !( ( ( NodeSPI ) targetNode ).getVersion() instanceof NonLockingDataVersion ) ) {
              ((NodeSPI) targetNode).setVersion(NonLockingDataVersion.INSTANCE);
         }
      }

      // Never evict this node
      targetNode.setResident(true);

      return targetNode;
   }


	// OptimisticCache impl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void setSource(OptimisticCacheSource source) {
		this.source = source;
	}

	public void writeInsert(Object key, Object value, Object currentVersion) {
		writeUpdate( key, value, currentVersion, null );
	}

	@SuppressWarnings("unchecked")
	public void writeUpdate(Object key, Object value, Object currentVersion, Object previousVersion) {
		try {
            ensureRegionRootExists();
            
			Option option = new Option();
			DataVersion dv = ( source != null && source.isVersioned() )
			                 ? new DataVersionAdapter( currentVersion, previousVersion, source.getVersionComparator(), source.toString() )
			                 : NonLockingDataVersion.INSTANCE;
			option.setDataVersion( dv );
			if (localOnlyQueries && key instanceof QueryKey)
               option.setCacheModeLocal(true);
            cache.getInvocationContext().setOptionOverrides(option);
			cache.put( new Fqn( regionFqn, key ), ITEM, value );
		}
		catch ( Exception e ) {
			throw SecondLevelCacheUtil.convertToHibernateException( e );
		}
	}

	@SuppressWarnings("unchecked")
    public void writeLoad(Object key, Object value, Object currentVersion) {
	   Transaction tx = null;
	   try {
	      Option option = new Option();
	      DataVersion dv = ( source != null && source.isVersioned() )
	                           ? new DataVersionAdapter( currentVersion, currentVersion, source.getVersionComparator(), source.toString() )
	                           : NonLockingDataVersion.INSTANCE;
	      option.setDataVersion( dv );

	      if (forTimestamps) 
	      {
	         tx = suspend();

	         ensureRegionRootExists();

	         // Timestamps don't use putForExternalRead, but are async
	         if (forceAsync)
	            option.setForceAsynchronous(true);
	         cache.getInvocationContext().setOptionOverrides(option);
	         cache.put(new Fqn( regionFqn, key ), ITEM, value);               
	      }
	      else if (key instanceof QueryKey) 
	      {
	         ensureRegionRootExists();

	         option.setCacheModeLocal(localOnlyQueries);
             option.setLockAcquisitionTimeout(2);
	         cache.getInvocationContext().setOptionOverrides(option);
	         cache.put( new Fqn( regionFqn, key ), ITEM, value );
	      }
	      else
	      {
	         ensureRegionRootExists();
	         cache.getInvocationContext().setOptionOverrides(option);
	         cache.putForExternalRead( new Fqn( regionFqn, key ), ITEM, value );
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
	      resume(tx);
	   }
	}


	// Cache impl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public Object get(Object key) throws CacheException {
	    Transaction tx = suspend();
	    try {
	        ensureRegionRootExists();
		    if (key instanceof QueryKey)
		    {		       
		       cache.getInvocationContext().getOptionOverrides().setLockAcquisitionTimeout(0);		       
		    }
		    
			return cache.get( new Fqn( regionFqn, key ), ITEM );
		}
		catch (TimeoutException e) {
		   if (key instanceof QueryKey)
		      return null;
		   throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
		finally {
		   resume(tx);
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
			Option option = new Option();
			option.setDataVersion( NonLockingDataVersion.INSTANCE );
            if (localOnlyQueries && key instanceof QueryKey)
               option.setCacheModeLocal(true);
            cache.getInvocationContext().setOptionOverrides(option);
			cache.put( new Fqn( regionFqn, key ), ITEM, value );
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
	}

	public void put(Object key, Object value) throws CacheException {
	    Transaction tx = null;
		try {
            Option option = new Option();
            option.setDataVersion( NonLockingDataVersion.INSTANCE );
            if (forTimestamps) {
               tx = suspend();

               ensureRegionRootExists();
               
               // Timestamps don't use putForExternalRead, but are async
               if (forceAsync)
                  option.setForceAsynchronous(true);
               cache.getInvocationContext().setOptionOverrides(option);
               cache.put(new Fqn( regionFqn, key ), ITEM, value);               
            }
            else if (key instanceof QueryKey) { 
               
               ensureRegionRootExists();
               
               option.setCacheModeLocal(localOnlyQueries);
               option.setLockAcquisitionTimeout(2);
               cache.getInvocationContext().setOptionOverrides(option);
               cache.put(new Fqn( regionFqn, key ), ITEM, value); 
            }
            else {
               ensureRegionRootExists();
               cache.getInvocationContext().setOptionOverrides(option);
   			   cache.putForExternalRead( new Fqn( regionFqn, key ), ITEM, value );
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
		   resume(tx);
		}
	}

	public void remove(Object key) throws CacheException {
	   try {
	      ensureRegionRootExists();

	      Option option = new Option();
	      option.setDataVersion( NonLockingDataVersion.INSTANCE );
	      if (localOnlyQueries && key instanceof QueryKey)
	         option.setCacheModeLocal(true);
	      cache.getInvocationContext().setOptionOverrides(option);
	      cache.removeNode( new Fqn( regionFqn, key ) );
	   }
	   catch (Exception e) {
	      throw SecondLevelCacheUtil.convertToHibernateException(e);
	   }
	}

	public void clear() throws CacheException {
		try {
			cache.getInvocationContext().getOptionOverrides().setDataVersion( NonLockingDataVersion.INSTANCE );
			cache.removeNode( regionFqn );
		}
		catch (Exception e) {
			throw SecondLevelCacheUtil.convertToHibernateException(e);
		}
	}

	public void destroy() throws CacheException {
		try {
			Option option = new Option();
			option.setCacheModeLocal( true );
			option.setDataVersion( NonLockingDataVersion.INSTANCE );
            cache.getInvocationContext().setOptionOverrides(option);
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
		return "OptimisticJBCCache(" + regionName + ')';
	}

	public static class DataVersionAdapter implements DataVersion 
    {
        private static final long serialVersionUID = 5564692336076405571L;
		private final Object currentVersion;
		private final Object previousVersion;
		private final Comparator<Object> versionComparator;
		private final String sourceIdentifer;

		public DataVersionAdapter(Object currentVersion, Object previousVersion, Comparator<Object> versionComparator, String sourceIdentifer) {
			this.currentVersion = currentVersion;
			this.previousVersion = previousVersion;
			this.versionComparator = versionComparator;
			this.sourceIdentifer = sourceIdentifer;
			log.trace( "created " + this );
		}

		/**
		 * newerThan() call is dispatched against the DataVersion currently
		 * associated with the node; the passed dataVersion param is the
		 * DataVersion associated with the data we are trying to put into
		 * the node.
		 * <p/>
		 * we are expected to return true in the case where we (the current
		 * node DataVersion) are newer that then incoming value.  Returning
		 * true here essentially means that a optimistic lock failure has
		 * occured (because conversely, the value we are trying to put into
		 * the node is "older than" the value already there...)
		 */
		public boolean newerThan(DataVersion dataVersion) {
			log.trace( "checking [" + this + "] against [" + dataVersion + "]" );
			if ( dataVersion instanceof CircumventChecksDataVersion ) {
				log.trace( "skipping lock checks..." );
				return false;
			}
			else if ( dataVersion instanceof NonLockingDataVersion ) {
				// can happen because of the multiple ways Cache.remove()
				// can be invoked :(
				log.trace( "skipping lock checks..." );
				return false;
			}
			DataVersionAdapter other = ( DataVersionAdapter ) dataVersion;
			if ( other.previousVersion == null ) {
				log.warn( "Unexpected optimistic lock check on inserting data" );
				// work around the "feature" where tree cache is validating the
				// inserted node during the next transaction.  no idea...
				if ( this == dataVersion ) {
					log.trace( "skipping lock checks due to same DV instance" );
					return false;
				}
			}
            
            if (currentVersion == null)
            {
               // If the workspace node has null as well, OK; if not we've
               // been modified in a non-comparable manner, which we have to
               // treat as us being newer 
               return (other.previousVersion != null);
            }
            
			return versionComparator.compare( currentVersion, other.previousVersion ) >= 1;
		}

		public String toString() {
			return super.toString() + " [current=" + currentVersion + ", previous=" + previousVersion + ", src=" + sourceIdentifer + "]";
		}
	}

	/**
	 * Used in regions where no locking should ever occur.  This includes query-caches,
	 * update-timestamps caches, collection caches, and entity caches where the entity
	 * is not versioned.
	 */
	public static class NonLockingDataVersion implements DataVersion 
    {
        private static final long serialVersionUID = 7050722490368630553L;
		public static final DataVersion INSTANCE = new NonLockingDataVersion();
		public boolean newerThan(DataVersion dataVersion) {
			log.trace( "non locking lock check...");
			return false;
		}
	}

	/**
	 * Used to signal to a DataVersionAdapter to simply not perform any checks.  This
	 * is currently needed for proper handling of remove() calls for entity cache regions
	 * (we do not know the version info...).
	 */
	public static class CircumventChecksDataVersion implements DataVersion  
    {
        private static final long serialVersionUID = 7996980646166032369L;
		public static final DataVersion INSTANCE = new CircumventChecksDataVersion();
		public boolean newerThan(DataVersion dataVersion) {
			throw new CacheException( "optimistic locking checks should never happen on CircumventChecksDataVersion" );
		}
	}
}
