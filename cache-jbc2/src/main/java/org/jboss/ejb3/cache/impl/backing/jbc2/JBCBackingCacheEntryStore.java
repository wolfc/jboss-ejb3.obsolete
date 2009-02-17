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

package org.jboss.ejb3.cache.impl.backing.jbc2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.CacheManager;
import org.jboss.cache.CacheStatus;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.Region;
import org.jboss.cache.buddyreplication.BuddyManager;
import org.jboss.cache.config.BuddyReplicationConfig;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.notifications.event.NodeActivatedEvent;
import org.jboss.cache.notifications.event.NodeModifiedEvent;
import org.jboss.cache.notifications.event.NodePassivatedEvent;
import org.jboss.cache.notifications.event.NodeRemovedEvent;
import org.jboss.cache.notifications.event.NodeVisitedEvent;
import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.GroupCompatibilityChecker;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.impl.AbstractBackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.impl.CacheableTimestamp;
import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

/**
 * JBoss Cache-based implementation of {@link PassivatingIntegratedObjectStore}.
 * 
 * @author Brian Stansberry
 */
public class JBCBackingCacheEntryStore<C extends CacheItem, T extends BackingCacheEntry<C>>
   extends AbstractBackingCacheEntryStore<C, T, OwnedItem>
{
   /** First element in EJB3 SFSB Fqns */
   private static final String FQN_BASE = "sfsb";
   /** Key under which items are stored in cache nodes */
   private static final String KEY = "item";
   
   /** Hack used to avoid having to make 2 calls to remove an entry */
   @SuppressWarnings("unchecked")
   private static final ThreadLocal removedItem = new ThreadLocal();
   
   /** Depth of fqn element where we store the entry. */ 
   static final int FQN_SIZE = 4;
   /**  
    * Number of "buckets" under the region root -- used to increase
    * the number of items FileCacheLoader can store w/o hitting
    * filesystem limits.
    */
   private static final int DEFAULT_BUCKET_COUNT = 100;
   /** The names of the aforementioned buckets */
   private static final String[] DEFAULT_HASH_BUCKETS = new String[DEFAULT_BUCKET_COUNT];

   static
   {
      for (int i = 0; i < DEFAULT_HASH_BUCKETS.length; i++)
      {
         DEFAULT_HASH_BUCKETS[i] = String.valueOf(i);
      }
   }
   
   /** The CacheManager from which we obtain our JBC instance */
   private final CacheManager cacheManager;
   /** The name of the config to request from the CacheManager */
   private final String cacheConfigName;
   
   /** The underlying JBC instance */
   private Cache<Object, Object> jbc;

   /** Qualifier used to scope our Fqns */
   private final Object keyBase;

   /** Fqn of region where we store items */
   private Fqn<Object> regionRootFqn;
   /** The region where we store items */
   private Region region;
   /** Listener for cache event notifications */
   private ClusteredCacheListener listener;
   /** Handler for listener events related to our region */
   private RegionHandlerImpl regionHandler;
   /** Our "buckets". See above */
   private String[] hashBuckets = DEFAULT_HASH_BUCKETS;
   /** Whether our cache is using buddy replication */
   private boolean usingBuddyRepl;
   /** Whether our RegionHandlerImpl should track visits */
   private boolean trackVisits;
   /** Last-use timestamps of in-memory items.  Used for passivation and expiration*/
   private final ConcurrentMap<OwnedItem, OwnedItem> inMemoryItems;
   /** Last-use timestamps of passivated items.  Used for expiration*/
   private final ConcurrentMap<OwnedItem, OwnedItem> passivatedItems;
   
   /**
    * Create a new JBCBackingCacheEntryStore.
    * 
    * @param cacheManager Source for our JBoss Cache instance
    * @param cacheConfigName name of config to request from CacheManager
    * @param cacheConfig configuration metadata
    * @param name our name
    * @param forGroups <code>true</code> if this cache is used for caching
    *                  {@link SerializationGroup}s, <code>false</code> otherwise
    */
   public JBCBackingCacheEntryStore(CacheManager cacheManager,
                                   String cacheConfigName,
                                   CacheConfig cacheConfig,
                                   String name,
                                   boolean forGroups)
   {     
      super(cacheConfig, name, forGroups);
      
      assert cacheManager != null : "cacheManager is null";
      assert cacheConfigName != null : "cacheConfigName is null";
      
      this.cacheManager = cacheManager;
      this.cacheConfigName = cacheConfigName;
      this.keyBase = name;

      this.log = Logger.getLogger(getClass().getName() + "-" + name);
      this.regionRootFqn = Fqn.fromElements(new Object[] { FQN_BASE, this.keyBase });
      
      this.inMemoryItems = new ConcurrentHashMap<OwnedItem, OwnedItem>();
      this.passivatedItems = new ConcurrentHashMap<OwnedItem, OwnedItem>();
   }

   public boolean isClustered()
   {
      if (jbc == null)
         return true; // assume yes
      else
         return jbc.getConfiguration().getCacheMode() != Configuration.CacheMode.LOCAL;
   }

   
   public T get(Object key)
   {
      T entry = null;
      Fqn<Object> id = getFqn(key, false);
      try
      {
         // If need be, gravitate
         if (usingBuddyRepl)
         {
            jbc.getInvocationContext().getOptionOverrides().setForceDataGravitation(true);
         }
         @SuppressWarnings("unchecked")
         T val = (T) jbc.get(id, KEY);
         entry = val;
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }

      if(log.isTraceEnabled())
      {
         log.trace("get: retrieved bean with cache id " +id.toString());
      }

      return entry;
   }

   public void insert(T entry)
   {
      try
      {
         if (log.isTraceEnabled())
            log.trace("insert: " + entry.getId());
         
         jbc.put(getFqn(entry.getId(), false), KEY, entry);
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
   }

   public void passivate(T entry)
   {
      if (log.isTraceEnabled())
         log.trace("passivate: " + entry.getId());
      
      Fqn<Object> id = getFqn(entry.getId(), false);
      jbc.evict(id);
   }

   @SuppressWarnings("unchecked")
   public T remove(Object key)
   {
      Fqn<Object> id = getFqn(key, false);
      try
      {
         if(log.isTraceEnabled())
         {
            log.trace("remove: cache id " +id.toString());
         }
         
         if (usingBuddyRepl)
         {
            jbc.getInvocationContext().getOptionOverrides().setForceDataGravitation(true);
         }
         jbc.removeNode(id);

         // Hack! Our cache listener has access to the removed node's data map
         // so it passes the removed item to us via a thread local.
         // Otherwise we'd have to do a remove(id, KEY) followed by removeNode(id)
         T removed = (T) removedItem.get();
         removedItem.set(null);
         
         return removed;
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
   }

   public void update(T entry, boolean modified)
   {
      if (log.isTraceEnabled())
         log.trace("update: " + entry.getId() + 
                   (modified ? " modified" : " unmodified"));
      
      if (modified)
      {
         try
         {
            jbc.put(getFqn(entry.getId(), false), KEY, entry);
         }
         catch (CacheException e)
         {
            RuntimeException re = convertToRuntimeException(e);
            throw re;
         }
      }
      else
      {
         OwnedItem oi = new OwnedItem(entry.getId(), regionRootFqn);
         oi.setLastUsed(entry.getLastUsed());
         inMemoryItems.put(oi, oi);
      }
   }

   public int getInMemoryCount()
   {
      return inMemoryItems.size();
   }

   public int getPassivatedCount()
   {
       return passivatedItems.size();
   }

   @SuppressWarnings("unchecked")
   public boolean isCompatibleWith(GroupCompatibilityChecker other)
   {
      if (other instanceof JBCBackingCacheEntryStore)
      {
         JBCBackingCacheEntryStore jbc2 = (JBCBackingCacheEntryStore) other;
         return this.cacheManager == jbc2.cacheManager
                 && this.cacheConfigName.equals(jbc2.cacheConfigName);
      }
      return false;
   }

   protected void internalStart()
   {      
      initializeJBossCache();
      
      region = jbc.getRegion(regionRootFqn, true);
      
      // Try to create an eviction region per ejb
      // BES 2008/03/12 No, let's handle passivation ourselves
      // since JBC doesn't properly track the buddy-backup region
//      EvictionPolicyConfig epc = getEvictionPolicyConfig();
//      region.setEvictionPolicy(epc);

      // JBCACHE-1136.  There's no reason to have state in an inactive region
      cleanBeanRegion();

      // Transfer over the state for the region
      region.registerContextClassLoader(Thread.currentThread().getContextClassLoader());
      region.activate();
      
      // register to listen for cache events

      for (Object listener : jbc.getCacheListeners())
      {
         if (listener instanceof ClusteredCacheListener)
         {
            this.listener = (ClusteredCacheListener) listener;
            break;
         }
      }
      
      if (listener == null)
      {
         listener = new ClusteredCacheListener(usingBuddyRepl);
         jbc.addCacheListener(listener);
      }
      
      regionHandler = new RegionHandlerImpl();
      listener.addRegionHandler(regionRootFqn, regionHandler);
      
      initializeTrackingMaps();
      
      super.internalStart();
   }
   
//   @SuppressWarnings("unchecked")
   private void initializeJBossCache()
   {
      try
      {
         this.jbc = cacheManager.getCache(cacheConfigName, true);
      }
      catch (CacheException e)
      {
         throw convertToRuntimeException(e);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Problem getting cache from CacheManager", e);
      }
      
      if (this.jbc.getCacheStatus() != CacheStatus.STARTED)
      {
         if (this.jbc.getCacheStatus() != CacheStatus.CREATED)
         {
            this.jbc.create();
         }
         
         this.jbc.start();            
      }
      
      BuddyReplicationConfig brc = jbc.getConfiguration().getBuddyReplicationConfig();
      this.usingBuddyRepl = brc != null && brc.isEnabled();
   }

   private void initializeTrackingMaps()
   {      
      // First the main tree
      Node<Object, Object> parent = jbc.getNode(regionRootFqn);
      analyzeRegionContent(parent);
     
      // Now any buddy regions
      if (usingBuddyRepl)
      {
         Node<Object, Object> bbRoot = jbc.getNode(BuddyManager.BUDDY_BACKUP_SUBTREE_FQN);
         if (bbRoot != null)
         {
            for (Node<Object, Object> bbRegion : bbRoot.getChildren())
            {
               Node<Object, Object> ourPart = bbRegion.getChild(regionRootFqn);
               if (ourPart != null)
               {
                  analyzeRegionContent(ourPart);
               }
            }
         }
      }
      
      // Now, we know any node visits will be from users, so
      // lets start monitoring them
      trackVisits = true;
   }

   private void analyzeRegionContent(Node<Object, Object> parent)
   {
      for (int i = 0; i < hashBuckets.length; i++)
      {
         Node<Object, Object> bucket = parent.getChild(hashBuckets[i]);
         if (bucket == null)
            continue;
         Set<Object> childrenNames = bucket.getChildrenNames();
         for (Object name : childrenNames)
         {
            Node<Object, Object> child = bucket.getChild(name);
            if (child == null)
               continue;
            @SuppressWarnings("unchecked")
            T entry = (T) child.get(KEY);
            if (entry != null)
            {
               boolean localEvent = false; // we don't own any of these
               OwnedItem oi = OwnedItem.getOwnedItem(child.getFqn(), usingBuddyRepl, localEvent);
               if (entry.isPrePassivated())
               {
                  jbc.evict(child.getFqn()); // we'll get a listener event for this
               }
               else
               {
                  oi.setLastUsed(entry.getLastUsed() == 0 ? System.currentTimeMillis() : entry.getLastUsed());
                  // Use putIfAbsent so we don't overwrite listener events
                  inMemoryItems.putIfAbsent(oi, oi);
               }
            }
         }
      }
   }
   
   protected void internalStop()
   {
      super.internalStop();
      
      if (jbc != null)
      {
         try
         {
            // Remove the listener
            if (listener != null && regionHandler != null && listener.removeRegionHandler(regionRootFqn))
            {
               // No more regions registered, so remove the listener
               jbc.removeCacheListener(listener);
            }
            
            // Null the listener so we have to re-check the cache is we start() again
            listener = null;
   
            // Remove locally. We do this to clean up the persistent store,
            // which is not affected by the inactivateRegion call below.
            cleanBeanRegion();
   
            if (region != null)
            {
               region.deactivate();
               region.unregisterContextClassLoader();
   
               jbc.removeRegion(region.getFqn());
               region = null;
            }
         }
         finally
         {
            cacheManager.releaseCache(cacheConfigName);
            jbc = null;
         }         
      }
      
      inMemoryItems.clear();
      passivatedItems.clear();
   }

   @Override
   protected void processExpiration(OwnedItem ownedItem, long lastUse)
   {
      if (!isRunning())
         return;
      
      // We only remove items we control
      if (OwnedItem.LOCAL_OWNER.equals(ownedItem.getOwner()))
      {
         getPassivatingCache().remove(ownedItem.getId());
      }
      
      // TODO -- add some mechanism for *eventually* removing items we
      // don't control, in case their owner died. Eventually meaning
      // after a day or so.
   }

   @Override
   protected void processPassivation(OwnedItem ownedItem, long lastUse)
   {
      if (!isRunning())
         return;
      
      synchronized (ownedItem)
      {
         String owner = ownedItem.getOwner();
         if (OwnedItem.LOCAL_OWNER.equals(owner))
         {
            getPassivatingCache().passivate(ownedItem.getId());
         }
         else
         {
            // No callbacks; the item is serialized and under the control of 
            // another cache. We just want it out of our memory
            
            // FIXME race condition here; we might have taken ownership!
            
            Fqn<Object> id = null;
            if (OwnedItem.REMOTE_OWNER.equals(owner))
            {
               id = getFqn(ownedItem.getId(), false);
            }
            else {
               id = getBuddyFqn(ownedItem.getId(), ownedItem.getOwner());
            }
            
            if (log.isTraceEnabled())
               log.trace("evicting remotely owned item " + ownedItem.getId());
            
            jbc.evict(id);
         }
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   protected CacheableTimestamp<OwnedItem>[] getInMemoryEntries()
   {     
      Set<CacheableTimestamp<OwnedItem>> set = new HashSet<CacheableTimestamp<OwnedItem>>();
      for (OwnedItem ownedItem : inMemoryItems.values())
      {
         set.add(new CacheableTimestamp<OwnedItem>(ownedItem, ownedItem.getLastUsed()));
      }
      CacheableTimestamp<OwnedItem>[] array = new CacheableTimestamp[set.size()];
      array = set.toArray(array);
      Arrays.sort(array);
      return array;
   }

   @Override
   @SuppressWarnings("unchecked")
   protected CacheableTimestamp<OwnedItem>[] getAllEntries()
   {     
      Set<CacheableTimestamp<OwnedItem>> set = new HashSet<CacheableTimestamp<OwnedItem>>();
      for (OwnedItem ownedItem : inMemoryItems.values())
      {
         set.add(new CacheableTimestamp<OwnedItem>(ownedItem, ownedItem.getLastUsed()));
      }
      CacheableTimestamp<Object>[] inMemory = new CacheableTimestamp[set.size()];
      inMemory = set.toArray(inMemory);   
      
      set = new HashSet<CacheableTimestamp<OwnedItem>>();
      for (OwnedItem ownedItem : passivatedItems.values())
      {
         set.add(new CacheableTimestamp<OwnedItem>(ownedItem, ownedItem.getLastUsed()));
      }
      CacheableTimestamp<OwnedItem>[] passivated = new CacheableTimestamp[set.size()];
      passivated = set.toArray(passivated);

      CacheableTimestamp<OwnedItem>[] all = new CacheableTimestamp[passivated.length + inMemory.length];
      System.arraycopy(passivated, 0, all, 0, passivated.length);
      System.arraycopy(inMemory, 0, all, passivated.length, inMemory.length);
      Arrays.sort(all);
      return all;
   }

   private Fqn<Object> getFqn(Object id, boolean regionRelative)
   {
      String beanId = id.toString();
      int index = getIndexForId(id, beanId);

      if (regionRelative)
         return Fqn.fromElements( new Object[] {hashBuckets[index], beanId} );
      else
         return Fqn.fromRelativeElements(regionRootFqn, hashBuckets[index], beanId);
   }
   
   private Fqn<Object> getBuddyFqn(Object id, Object owner)
   {
      assert owner != null : "owner cannot be null for a buddy backup Fqn";
      
      return Fqn.fromElements(new Object[] { BuddyManager.BUDDY_BACKUP_SUBTREE, owner, FQN_BASE, keyBase, id});
   }
   
   private int getIndexForId(Object id, String stringForm)
   {
      int index;
      if (id instanceof GUID)
      {
         index = (id.hashCode()& 0x7FFFFFFF) % hashBuckets.length;
      }
      else
      {
         index = (stringForm.hashCode()& 0x7FFFFFFF) % hashBuckets.length;
      }
      
      return index;
   }

   /**
    * Creates a RuntimeException, but doesn't pass CacheException as the cause
    * as it is a type that likely doesn't exist on a client.
    * Instead creates a RuntimeException with the original exception's
    * stack trace.
    */
   private RuntimeException convertToRuntimeException(CacheException e)
   {
      RuntimeException re = new RuntimeException(e.getClass().getName() + " " + e.getMessage());
      re.setStackTrace(e.getStackTrace());
      return re;
   }

   private void cleanBeanRegion()
   {
      try {
         // Remove locally.
         jbc.getInvocationContext().getOptionOverrides().setCacheModeLocal(true);
         jbc.removeNode(regionRootFqn);
      }
      catch (CacheException e)
      {
         log.error("Can't clean region " + regionRootFqn + " in the underlying distributed cache", e);
      }
   }

   /**
    * A CacheListener that allows us to get notifications of passivations and
    * activations and thus notify the cached StatefulBeanContext.
    */
   public class RegionHandlerImpl implements ClusteredCacheListener.RegionHandler
   {
      public void nodeVisited(OwnedItem oi, NodeVisitedEvent event)
      {
         if (trackVisits)
         {
            long now = System.currentTimeMillis();
            oi.setLastUsed(now);
            OwnedItem existing = inMemoryItems.putIfAbsent(oi, oi);
            if (existing != null && !existing.isPassivating())
            {
               existing.setLastUsed(now);
               existing.setLocallyOwned(event.isOriginLocal());
            }               
         }
      }
      
      public void nodeModified(OwnedItem oi, NodeModifiedEvent event)
      {
         @SuppressWarnings("unchecked")
         T entry = (T) event.getData().get(KEY);
         if (entry != null)
         {
            long lastUsed = entry.getLastUsed();
            lastUsed = (lastUsed == 0 ? System.currentTimeMillis() : lastUsed);
            oi.setLastUsed(lastUsed);
            OwnedItem existing = inMemoryItems.putIfAbsent(oi, oi);
            if (existing != null && !existing.isPassivating())
            {
               existing.setLastUsed(lastUsed);
               existing.setLocallyOwned(event.isOriginLocal());
            }               
            
            if (log.isTraceEnabled())
               log.trace(oi + " modified " + (event.isOriginLocal() ? " locally" : "remotely") );
         }       
      }
      
      @SuppressWarnings("unchecked")
      public void nodeRemoved(OwnedItem oi, NodeRemovedEvent event)
      {
         inMemoryItems.remove(oi);
         passivatedItems.remove(oi);
         
         // Hack! We have access to the data map here; so pass the removed
         // item to our remove() method via a thread local
         removedItem.set(event.getData().get(KEY));
         
         if (log.isTraceEnabled())
            log.trace(oi + " removed " + (event.isOriginLocal() ? " locally" : "remotely") );
      }
      
      public void nodeActivated(OwnedItem oi, NodeActivatedEvent event)
      {
         @SuppressWarnings("unchecked")
         T entry = (T) event.getData().get(KEY);
         if (entry != null)
         {
            long lastUsed = entry.getLastUsed();
            oi.setLastUsed(lastUsed == 0 ? System.currentTimeMillis() : lastUsed);
            OwnedItem existing = inMemoryItems.putIfAbsent(oi, oi);
            if (existing == null)
            {
               passivatedItems.remove(oi);
            }
            else if (!existing.isPassivating())
            {
               existing.setLocallyOwned(event.isOriginLocal());
            }
            
            if (log.isTraceEnabled())
               log.trace(oi + " activated -- " + (event.isOriginLocal() ? " local" : "remote") );
         }        
      }

      public void nodePassivated(OwnedItem oi, NodePassivatedEvent event)
      {
         @SuppressWarnings("unchecked")
         T entry = (T) event.getData().get(KEY);
         if (entry != null)
         {
            long lastUsed = entry.getLastUsed();
            oi.setLastUsed(lastUsed == 0 ? System.currentTimeMillis() : lastUsed);
            OwnedItem existing = passivatedItems.putIfAbsent(oi, oi);
            if (existing == null)
            {
               inMemoryItems.remove(oi);
            }
            
            if (log.isTraceEnabled())
               log.trace(oi + " passivated");
         } 
      }
   }

}
