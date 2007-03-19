package org.jboss.ejb3.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.StandardQueryCache;
import org.hibernate.cache.UpdateTimestampsCache;
import org.jboss.cache.Fqn;
import org.jboss.cache.InvocationContext;
import org.jboss.cache.Node;
import org.jboss.cache.Region;
import org.jboss.cache.config.Option;
import org.jboss.cache.lock.TimeoutException;

/**
 * {@link Cache} implementation that uses a 2.x or later release of JBoss Cache.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class JBCCache implements Cache
{
    
    private static final Log log = LogFactory.getLog(JBCCache.class);

    private static final String ITEM = "item";

    private org.jboss.cache.Cache cache;
    private final String regionName;
    private final Fqn regionFqn;
    private final TransactionManager transactionManager;
    private boolean localWritesOnly;

    public JBCCache(org.jboss.cache.Cache cache, String regionName, TransactionManager transactionManager) 
    throws CacheException {
        this.cache = cache;
        this.regionName = regionName;
        this.regionFqn = Fqn.fromString( regionName.replace( '.', '/' ) );
        this.transactionManager = transactionManager;
        if (cache.getConfiguration().isUseRegionBasedMarshalling())
        {           
           localWritesOnly = StandardQueryCache.class.getName().equals(regionName);
           
           boolean fetchState = cache.getConfiguration().isFetchInMemoryState();
           try
           {
              // We don't want a state transfer for the StandardQueryCache,
              // as it can include classes from multiple scoped classloaders
              if (localWritesOnly)
                 cache.getConfiguration().setFetchInMemoryState(false);
              
              // We always activate
              activateCacheRegion(regionFqn.toString());
           }
           finally
           {
              // Restore the normal state transfer setting
              if (localWritesOnly)
                 cache.getConfiguration().setFetchInMemoryState(fetchState);              
           }
        }
    }

    public Object get(Object key) throws CacheException {
        Transaction tx = suspend();
        try {
            return read(key);
        }
        finally {
            resume( tx );
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
        	if (localWritesOnly) {
               Option option = new Option();
               option.setCacheModeLocal(true);
               cache.getInvocationContext().setOptionOverrides(option);
               cache.put( new Fqn( regionFqn, key ), ITEM, value );
            }
            else {                
            	cache.put( new Fqn( regionFqn, key ), ITEM, value );
            }
        }
        catch (Exception e) {
            throw new CacheException(e);
        }
    }

    public void put(Object key, Object value) throws CacheException {
        Transaction tx = suspend();
        try {
        	if (localWritesOnly) {
               Option option = new Option();
               option.setCacheModeLocal(true);
               cache.getInvocationContext().setOptionOverrides(option);
               //do the failfast put outside the scope of the JTA txn               
               cache.putForExternalRead(new Fqn( regionFqn, key ), ITEM, value);
            }
            else {
               cache.putForExternalRead(new Fqn( regionFqn, key ), ITEM, value);
            }
        }
        catch (TimeoutException te) {
            //ignore!
            log.debug("ignoring write lock acquisition failure");
        }
        catch (Exception e) {
            throw new CacheException(e);
        }
        finally {
            resume( tx );
        }
    }

    private void resume(Transaction tx) {
        try {
            if (tx!=null) transactionManager.resume(tx);
        }
        catch (Exception e) {
            throw new CacheException("Could not resume transaction", e);
        }
    }

    private Transaction suspend() {
        Transaction tx = null;
        try {
            if ( transactionManager!=null ) {
                tx = transactionManager.suspend();
            }
        }
        catch (SystemException se) {
            throw new CacheException("Could not suspend transaction", se);
        }
        return tx;
    }

    public void remove(Object key) throws CacheException {
        try {
           if (localWritesOnly) {
              Option option = new Option();
              option.setCacheModeLocal(true);
              cache.getInvocationContext().setOptionOverrides(option);
              cache.removeNode( new Fqn( regionFqn, key ) );
           }
           else {           
              cache.removeNode( new Fqn( regionFqn, key ) );
           }
        }
        catch (Exception e) {
            throw new CacheException(e);
        }
    }

    public void clear() throws CacheException {
        try {
            cache.removeNode( regionFqn );
        }
        catch (Exception e) {
            throw new CacheException(e);
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
           InvocationContext ctx = cache.getInvocationContext();
           Option opt = new Option();
           opt.setCacheModeLocal(true);
           ctx.setOptionOverrides(opt);
           cache.removeNode( regionFqn );
           
           if (cache.getConfiguration().isUseRegionBasedMarshalling() && !isSharedClassLoaderRegion(regionName))
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
        return "JBCCache(" + regionName + ')';
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
}
