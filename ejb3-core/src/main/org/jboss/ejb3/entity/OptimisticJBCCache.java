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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.OptimisticCache;
import org.hibernate.cache.OptimisticCacheSource;
import org.hibernate.cache.StandardQueryCache;
import org.hibernate.cache.UpdateTimestampsCache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.Region;
import org.jboss.cache.optimistic.DataVersion;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.config.Option;
import org.jboss.cache.lock.TimeoutException;

/**
 * Represents a particular region within the given JBossCache TreeCache
 * utilizing TreeCache's optimistic locking capabilities.
 *
 * @see org.hibernate.cache.OptimisticTreeCacheProvider for more details
 *
 * @author Steve Ebersole
 * @author Brian Stansberry
 */
public class OptimisticJBCCache implements OptimisticCache {

	// todo : eventually merge this with TreeCache and just add optional opt-lock support there.

	private static final Log log = LogFactory.getLog( OptimisticJBCCache.class);

	private static final String ITEM = "item";

	private org.jboss.cache.Cache cache;
	private final String regionName;
	private final Fqn regionFqn;
	private OptimisticCacheSource source;
    private boolean localWritesOnly;

	public OptimisticJBCCache(org.jboss.cache.Cache cache, String regionName)
	throws CacheException {
		this.cache = cache;
		this.regionName = regionName;
		this.regionFqn = Fqn.fromString( regionName.replace( '.', '/' ) );
        Configuration config = cache.getConfiguration();
        if (config.isUseRegionBasedMarshalling())
        {           
           localWritesOnly = StandardQueryCache.class.getName().equals(regionName);
           
           boolean fetchState = config.isFetchInMemoryState();
           try
           {
              // We don't want a state transfer for the StandardQueryCache,
              // as it can include classes from multiple scoped classloaders
              if (localWritesOnly)
                 config.setFetchInMemoryState(false);
              
              // We always activate
              activateCacheRegion(regionFqn.toString());
           }
           finally
           {
              // Restore the normal state transfer setting
              if (localWritesOnly)
                 config.setFetchInMemoryState(fetchState);              
           }
        }
        else
        {
           log.debug("TreeCache is not configured for region based marshalling");
        }
	}


	// OptimisticCache impl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void setSource(OptimisticCacheSource source) {
		this.source = source;
	}

	public void writeInsert(Object key, Object value, Object currentVersion) {
		writeUpdate( key, value, currentVersion, null );
	}

	public void writeUpdate(Object key, Object value, Object currentVersion, Object previousVersion) {
		try {
			Option option = new Option();
			DataVersion dv = ( source != null && source.isVersioned() )
			                 ? new DataVersionAdapter( currentVersion, previousVersion, source.getVersionComparator(), source.toString() )
			                 : NonLockingDataVersion.INSTANCE;
			option.setDataVersion( dv );
            option.setCacheModeLocal(localWritesOnly);
            cache.getInvocationContext().setOptionOverrides(option);
			cache.put( new Fqn( regionFqn, key ), ITEM, value );
		}
		catch ( Exception e ) {
			throw new CacheException( e );
		}
	}

	public void writeLoad(Object key, Object value, Object currentVersion) {
		try {
			Option option = new Option();
			option.setFailSilently( true );
			option.setDataVersion( NonLockingDataVersion.INSTANCE );
            option.setCacheModeLocal(localWritesOnly);
            cache.getInvocationContext().setOptionOverrides(option);
			cache.remove( new Fqn( regionFqn, key ), "ITEM" );

			option = new Option();
			option.setFailSilently( true );
			DataVersion dv = ( source != null && source.isVersioned() )
			                 ? new DataVersionAdapter( currentVersion, currentVersion, source.getVersionComparator(), source.toString() )
			                 : NonLockingDataVersion.INSTANCE;
			option.setDataVersion( dv );
            option.setCacheModeLocal(localWritesOnly);
            cache.getInvocationContext().setOptionOverrides(option);
			cache.put( new Fqn( regionFqn, key ), ITEM, value );
		}
		catch (Exception e) {
			throw new CacheException(e);
		}
	}


	// Cache impl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public Object get(Object key) throws CacheException {
		try {
			Option option = new Option();
			option.setFailSilently( true );
//			option.setDataVersion( NonLockingDataVersion.INSTANCE );
            cache.getInvocationContext().setOptionOverrides(option);
			return cache.get( new Fqn( regionFqn, key ), ITEM );
		}
		catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public Object read(Object key) throws CacheException {
		try {
			return cache.get( new Fqn( regionFqn, key ), ITEM );
		}
		catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public void update(Object key, Object value) throws CacheException {
		try {
			Option option = new Option();
			option.setDataVersion( NonLockingDataVersion.INSTANCE );
            option.setCacheModeLocal(localWritesOnly);
            cache.getInvocationContext().setOptionOverrides(option);
			cache.put( new Fqn( regionFqn, key ), ITEM, value );
		}
		catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public void put(Object key, Object value) throws CacheException {
		try {
			log.trace( "performing put() into region [" + regionName + "]" );
			// do the put outside the scope of the JTA txn
			Option option = new Option();
			option.setFailSilently( true );
			option.setDataVersion( NonLockingDataVersion.INSTANCE );
            option.setCacheModeLocal(localWritesOnly);
            cache.getInvocationContext().setOptionOverrides(option);
			cache.put( new Fqn( regionFqn, key ), ITEM, value );
		}
		catch (TimeoutException te) {
			//ignore!
			log.debug("ignoring write lock acquisition failure");
		}
		catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public void remove(Object key) throws CacheException {
		try {
			// tree cache in optimistic mode seems to have as very difficult
			// time with remove calls on non-existent nodes (NPEs)...
			if ( cache.get( new Fqn( regionFqn, key ), ITEM ) != null ) {
				Option option = new Option();
				option.setDataVersion( NonLockingDataVersion.INSTANCE );
                option.setCacheModeLocal(localWritesOnly);
                cache.getInvocationContext().setOptionOverrides(option);
				cache.removeNode( new Fqn( regionFqn, key ) );
			}
			else {
				log.trace( "skipping remove() call as the underlying node did not seem to exist" );
			}
		}
		catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public void clear() throws CacheException {
		try {
			Option option = new Option();
			option.setDataVersion( NonLockingDataVersion.INSTANCE );
            option.setCacheModeLocal(localWritesOnly);
            cache.getInvocationContext().setOptionOverrides(option);
			cache.removeNode( regionFqn );
		}
		catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public void destroy() throws CacheException {
		try {
			Option option = new Option();
			option.setCacheModeLocal( true );
			option.setFailSilently( true );
			option.setDataVersion( NonLockingDataVersion.INSTANCE );
            cache.getInvocationContext().setOptionOverrides(option);
			cache.removeNode( regionFqn );
            
            if (cache.getConfiguration().isUseRegionBasedMarshalling() 
                  && !isSharedClassLoaderRegion(regionName))
            {
               inactivateCacheRegion();
            }
		}
		catch( Exception e ) {
			throw new CacheException( e );
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
			Set children = getChildrenNames();
			return children == null ? 0 : children.size();
		}
		catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public long getElementCountOnDisk() {
		return 0;
	}

	public Map toMap() {
		try {
			Map result = new HashMap();
			Set childrenNames = getChildrenNames();
			if (childrenNames != null) {
				Iterator iter = childrenNames.iterator();
				while ( iter.hasNext() ) {
					Object key = iter.next();
					result.put(
							key,
					        cache.get( new Fqn( regionFqn, key ), ITEM )
						);
				}
			}
			return result;
		}
		catch (Exception e) {
			throw new CacheException(e);
		}
	}
    
    private Set getChildrenNames()
    {
       try {
          Node base = cache.getRoot().getChild( regionFqn );
          return base == null ? null : base.getChildrenNames();
       }
       catch (Exception e) {
          throw new CacheException(e);
       }   
    }

	public String toString() {
		return "OptimisticTreeCache(" + regionName + ')';
	}
       
    private boolean isSharedClassLoaderRegion(String regionName)
    {
       return (StandardQueryCache.class.getName().equals(regionName) 
                || UpdateTimestampsCache.class.getName().equals(regionName));
    }
    
    private void activateCacheRegion(String regionName) throws CacheException
    {
       Region region = cache.getRegion(regionFqn, true);
       if (region.isActive() == false)
       {
          try
          {
             // Only register the classloader if it's not a shared region.  
             // If it's shared, no single classloader is valid
             if (!isSharedClassLoaderRegion(regionName))
             {
                region.registerContextClassLoader(Thread.currentThread().getContextClassLoader());
             }
             region.activate();
          }
          catch (Exception e)
          {
             throw new CacheException("Problem activating region " + regionFqn, e);
          }
       }
    }
    
    private void inactivateCacheRegion() throws CacheException
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
             throw new CacheException("Problem inactivating region " + regionFqn, e);
          }
       }        
    }   

	public static class DataVersionAdapter implements DataVersion 
    {
        private static final long serialVersionUID = 5564692336076405571L;
		private final Object currentVersion;
		private final Object previousVersion;
		private final Comparator versionComparator;
		private final String sourceIdentifer;

		public DataVersionAdapter(Object currentVersion, Object previousVersion, Comparator versionComparator, String sourceIdentifer) {
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
