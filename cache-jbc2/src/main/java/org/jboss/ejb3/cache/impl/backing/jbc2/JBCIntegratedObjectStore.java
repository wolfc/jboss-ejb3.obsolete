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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
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
import org.jboss.ejb3.cache.api.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.PassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.spi.impl.AbstractPassivatingIntegratedObjectStore;
import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

/**
 * JBoss Cache-based implementation of {@link PassivatingIntegratedObjectStore}.
 * 
 * @author Brian Stansberry
 */
public class JBCIntegratedObjectStore<C extends CacheItem, T extends BackingCacheEntry<C>>
   extends AbstractPassivatingIntegratedObjectStore<C, T>
{
   public static final String FQN_BASE = "sfsb";
   
   private static final String KEY = "item";
   
   private static final ThreadLocal removedItem = new ThreadLocal();
   
   /** Depth of fqn element where we store the id. */ 
   static final int FQN_SIZE = 4; // depth of fqn that we store the session in.
   private static final int DEFAULT_BUCKET_COUNT = 100;

   private static final String[] DEFAULT_HASH_BUCKETS = new String[DEFAULT_BUCKET_COUNT];

   static
   {
      for (int i = 0; i < DEFAULT_HASH_BUCKETS.length; i++)
      {
         DEFAULT_HASH_BUCKETS[i] = String.valueOf(i);
      }
   }
   
   /** The underlying JBC instance */
   private Cache<Object, T> jbc;

   /** Qualifier used to scope our Fqns */
   private final Object keyBase;

   private Fqn<Object> cacheNode;
   private Region region;
   private ClusteredCacheListener listener;
   private RegionHandlerImpl regionHandler;

   public static long MarkInUseWaitTime = 15000;

   private final ThreadLocal<Boolean> localActivity = new ThreadLocal<Boolean>();
   private final Logger log;
   private String[] hashBuckets = DEFAULT_HASH_BUCKETS;
   private int createCount = 0;
   private int passivatedCount = 0;
   private int removeCount = 0;
   private boolean usingBuddyRepl;
   private boolean trackVisits;
   
   private final ConcurrentMap<OwnedItem, Long> inMemoryItems;
   private final ConcurrentMap<OwnedItem, Long> passivatedItems;
   
   public JBCIntegratedObjectStore(Cache jbc, 
                                   CacheConfig cacheConfig, 
                                   Object keyBase,
                                   String name,
                                   boolean forGroups)
   {     
      super(cacheConfig, name, forGroups);
      
      assert jbc != null : "jbc is null";
      assert keyBase != null : "keyBase is null";
      
      this.jbc = jbc;
      this.keyBase = keyBase;

      this.log = Logger.getLogger(getClass().getName() + "." + name);
      this.cacheNode = new Fqn<Object>(new Object[] { FQN_BASE, this.keyBase });
      BuddyReplicationConfig brc = jbc.getConfiguration().getBuddyReplicationConfig();
      this.usingBuddyRepl = brc != null && brc.isEnabled();
      
      this.inMemoryItems = new ConcurrentHashMap<OwnedItem, Long>();
      this.passivatedItems = new ConcurrentHashMap<OwnedItem, Long>();
   }

//   private EvictionPolicyConfig getEvictionPolicyConfig()
//   {
//      LRUConfiguration epc = new LRUConfiguration();
//      // Override the standard policy class
//      epc.setEvictionPolicyClass(AbortableLRUPolicy.class.getName());
//      epc.setTimeToLiveSeconds((int) getIdleTimeSeconds());
//      epc.setMaxNodes(getMaxSize());
//      return epc;
//   }

   public boolean isClustered()
   {
      return jbc.getConfiguration().getCacheMode() != Configuration.CacheMode.LOCAL;
   }

   public T get(Object key)
   {
      T entry = null;
      Fqn<Object> id = getFqn(key, false);
      Boolean active = localActivity.get();
      try
      {
         localActivity.set(Boolean.TRUE);
         // If need be, gravitate
         if (usingBuddyRepl)
         {
            jbc.getInvocationContext().getOptionOverrides().setForceDataGravitation(true);
         }
         entry = (T) jbc.get(id, KEY);
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
      finally
      {
         localActivity.set(active);
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
         putInCache(entry);
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
   }

   public void passivate(T entry)
   {
      Fqn<Object> id = getFqn(entry.getId(), false);
      jbc.evict(id);
   }

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
         
         ++removeCount;
         
         return removed;
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
   }

   public void update(T entry)
   {
      try
      {
         putInCache(entry);
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
   }

   public void start()
   {      
      region = jbc.getRegion(cacheNode, true);
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
      listener.addRegionHandler(cacheNode, regionHandler);
      
      initializeTrackingMaps();
      
      super.start();
   }

   private void initializeTrackingMaps()
   {      
      // First the main tree
      Node<Object, T> parent = jbc.getNode(cacheNode);
      analyzeRegionContent(parent);
     
      // Now any buddy regions
      if (usingBuddyRepl)
      {
         Node<Object, T> bbRoot = jbc.getNode(BuddyManager.BUDDY_BACKUP_SUBTREE_FQN);
         if (bbRoot != null)
         {
            for (Node<Object, T> bbRegion : bbRoot.getChildren())
            {
               Node<Object, T> ourPart = bbRegion.getChild(cacheNode);
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

   private void analyzeRegionContent(Node<Object, T> parent)
   {
      for (int i = 0; i < hashBuckets.length; i++)
      {
         Node<Object, T> bucket = parent.getChild(hashBuckets[i]);
         if (bucket == null)
            continue;
         Set<Object> childrenNames = bucket.getChildrenNames();
         for (Object name : childrenNames)
         {
            Node<Object, T> child = bucket.getChild(name);
            if (child == null)
               continue;
            T entry = (T) child.get(KEY);
            if (entry != null)
            {
               OwnedItem oi = OwnedItem.getOwnedItem(child.getFqn(), usingBuddyRepl);
               if (entry.isPrePassivated())
               {
                  jbc.evict(child.getFqn()); // we'll get a listener event for this
               }
               else
               {
                  long lastUsed = (entry.getLastUsed() == 0) ? System.currentTimeMillis() : entry.getLastUsed();
                  // Use putIfAbsent so we don't overwrite listener events
                  inMemoryItems.putIfAbsent(oi, lastUsed);
               }
            }
         }
      }
   }
   
   public void stop()
   {
      // FIXME get the proper ordering vis-a-vis stopping the timeout runner
      
      if (jbc != null)
      {
         // Remove the listener
         if (listener != null && regionHandler != null && listener.removeRegionHandler(cacheNode))
            jbc.removeCacheListener(listener);

         // Remove locally. We do this to clean up the persistent store,
         // which is not affected by the inactivateRegion call below.
         cleanBeanRegion();

         try {
            // Remove locally. We do this to clean up the persistent store,
            // which is not affected by the region.deactivate call below.
            jbc.getInvocationContext().getOptionOverrides().setCacheModeLocal(true);
            jbc.removeNode(cacheNode);
         }
         catch (CacheException e)
         {
            log.error("stop(): can't remove bean from the underlying distributed cache");
         }

         if (region != null)
         {
            region.deactivate();
            region.unregisterContextClassLoader();

            jbc.removeRegion(region.getFqn());
            // Clear any queues
            region.resetEvictionQueues();
            region = null;
         }
      }
      
      inMemoryItems.clear();
      passivatedItems.clear();
      
      super.stop();
   }

   public int getCacheSize()
   {
      int count = 0;
      try
      {
         Set<Object> children = null;
         for (int i = 0; i < hashBuckets.length; i++)
         {
            Node<Object, T> node = jbc.getRoot().getChild(new Fqn<Object>(cacheNode, hashBuckets[i]));
            if (node != null)
            {
               children = node.getChildrenNames();
               count += (children == null ? 0 : children.size());
            }
         }
         count = count - passivatedCount;
      }
      catch (CacheException e)
      {
         log.error("Caught exception calculating cache size", e);
         count = -1;
      }
      return count;
   }

   public int getTotalSize()
   {
      return inMemoryItems.size() + passivatedItems.size();
   }

   public int getCreateCount()
   {
       return createCount;
   }

   public int getPassivatedCount()
   {
       return passivatedItems.size();
   }

   public int getRemoveCount()
   {
      return removeCount;
   }

   public int getAvailableCount()
   {
      return -1;
   }

   public int getCurrentSize()
   {
      return inMemoryItems.size();
   }

   @Override
   protected void runExpiration()
   {
      throw new UnsupportedOperationException("implement me");
   }

   @Override
   protected void runPassivation()
   {
      throw new UnsupportedOperationException("implement me");      
   }

   private Fqn<Object> getFqn(Object id, boolean regionRelative)
   {
      String beanId = id.toString();
      int index;
      if (id instanceof GUID)
      {
         index = (id.hashCode()& 0x7FFFFFFF) % hashBuckets.length;
      }
      else
      {
         index = (beanId.hashCode()& 0x7FFFFFFF) % hashBuckets.length;
      }

      if (regionRelative)
         return new Fqn<Object>( new Object[] {hashBuckets[index], beanId} );
      else
         return new Fqn<Object>(cacheNode, hashBuckets[index], beanId);
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
         jbc.removeNode(cacheNode);
      }
      catch (CacheException e)
      {
         log.error("Can't clean region " + cacheNode + " in the underlying distributed cache", e);
      }
   }

   private void putInCache(T entry)
   {
      Boolean active = localActivity.get();
      try
      {
         localActivity.set(Boolean.TRUE);
         jbc.put(getFqn(entry.getId(), false), KEY, entry);
      }
      finally
      {
         localActivity.set(active);
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
            inMemoryItems.put(oi, System.currentTimeMillis());         
      }
      
      public void nodeModified(OwnedItem oi, NodeModifiedEvent event)
      {
         T entry = (T) event.getData().get(KEY);
         if (entry != null)
         {
            long lastUsed = entry.getLastUsed();
            lastUsed = (lastUsed == 0 ? System.currentTimeMillis() : lastUsed);
            inMemoryItems.put(oi, lastUsed);
         }       
      }
      
      public void nodeRemoved(OwnedItem oi, NodeRemovedEvent event)
      {
         inMemoryItems.remove(oi);
         passivatedItems.remove(oi);
         
         // Hack! We have access to the data map here; so pass the removed
         // item to our remove() method via a thread local
         removedItem.set(event.getData().get(KEY));
      }
      
      public void nodeActivated(OwnedItem oi, NodeActivatedEvent event)
      {
         T entry = (T) event.getData().get(KEY);
         if (entry != null)
         {
            long lastUsed = entry.getLastUsed();
            lastUsed = (lastUsed == 0 ? System.currentTimeMillis() : lastUsed);
            inMemoryItems.put(oi, lastUsed);
            passivatedItems.remove(oi);
         }        
      }

      public void nodePassivated(OwnedItem oi, NodePassivatedEvent event)
      {
         T entry = (T) event.getData().get(KEY);
         if (entry != null)
         {
            long lastUsed = entry.getLastUsed();
            lastUsed = (lastUsed == 0 ? System.currentTimeMillis() : lastUsed);
            passivatedItems.put(oi, lastUsed);
            inMemoryItems.remove(oi);
         } 
      }
   }

}
