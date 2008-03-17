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

package org.jboss.ejb3.cache.impl.backing;

import java.util.Map;

import org.jboss.ejb3.cache.api.CacheItem;
import org.jboss.ejb3.cache.api.PassivationManager;
import org.jboss.ejb3.cache.api.StatefulObjectFactory;
import org.jboss.ejb3.cache.spi.GroupAwareBackingCache;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.PassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;
import org.jboss.logging.Logger;

/**
 * @author Brian Stansberry
 *
 */
public class SerializationGroupMemberContainer<C extends CacheItem>
   implements StatefulObjectFactory<SerializationGroupMember<C>>, 
              PassivationManager<SerializationGroupMember<C>>, 
              PassivatingIntegratedObjectStore<C, SerializationGroupMember<C>>
{
   private static final Logger log = Logger.getLogger(SerializationGroupMemberContainer.class);
   
   private StatefulObjectFactory<C> factory;
   private PassivationManager<C> passivationManager;
   private PassivatingIntegratedObjectStore<C, SerializationGroupMember<C>> store;
   private GroupAwareBackingCache<C, SerializationGroupMember<C>> delegate;
   
   /**
    * Cache that's managing the PassivationGroup
    */
   private PassivatingBackingCache<C, SerializationGroup<C>> groupCache;
   
   
   public SerializationGroupMemberContainer(StatefulObjectFactory<C> factory, 
                                            PassivationManager<C> passivationManager, 
                                            PassivatingIntegratedObjectStore<C, SerializationGroupMember<C>> store,
                                            PassivatingBackingCache<C, SerializationGroup<C>> groupCache)
   {
      assert factory != null : "factory is null";
      assert passivationManager != null : "passivationManager is null";
      assert store != null : "store is null";
      assert groupCache != null : "groupCache is null";
      
      this.factory = factory;
      this.passivationManager = passivationManager;
      this.store = store;
      this.groupCache = groupCache;
   }
   
   public SerializationGroupMember<C> create(Class<?>[] initTypes, Object[] initValues, Map<Object, Object> sharedState)
   {
      SerializationGroupMemberImpl<C> member = 
         new SerializationGroupMemberImpl<C>(factory.create(initTypes, initValues, sharedState), 
                                         delegate);
      return member;
   }

   public void destroy(SerializationGroupMember<C> entry)
   {
      factory.destroy(entry.getUnderlyingItem());
      SerializationGroup<C> group = entry.getGroup();
      if (group != null) 
      {
         group.removeMember(entry.getId());
         if (group.size() == 0)
         {
            groupCache.remove(group.getId());
         }
      }
   }
   
   public void postActivate(SerializationGroupMember<C> entry)
   {
      log.trace("postActivate(): " + entry);
      
      boolean groupOK = false;
      while (!groupOK)
      {
         // Restore the entry's ref to the group and object
         SerializationGroup<C> group = entry.getGroup();
         if(group == null && entry.getGroupId() != null)
         {
            // TODO: peek or get?
            // BES 2007/10/06 I think peek is better; no
            // sense marking the group as in-use and then having
            // to release it or something
//            group = groupCache.peek(entry.getGroupId());
            group = groupCache.get(entry.getGroupId());
         }
         
         if(group != null)
         {
            group.lock();
            try
            {
               if (!group.isInvalid())
               {
                  entry.setGroup(group);
                  entry.setUnderlyingItem(group.getMemberObject(entry.getId()));                  
                  // Notify the group that this entry is active
                  group.addActive(entry);
                  groupOK = true;
               }
               // else groupOK == false and we loop again
            }
            finally
            {
               group.unlock();
            }
         }
         else
         {
            groupOK = true;
         }
      }
      
      // Invoke callbacks on the underlying object
      if (entry.isPrePassivated())
      {
         passivationManager.postActivate(entry.getUnderlyingItem());
         entry.setPrePassivated(false);
      }
   }
   
   public void prePassivate(SerializationGroupMember<C> entry)
   {
      log.trace("pre-passivate " + entry);
      
      // entry.obj may or may not get serialized (depends on if group
      // is in use) but it's ok to invoke callbacks now. If a caller
      // requests this entry again and the obj hadn't been serialized with
      // the group, we'll just call postActivate on it then, which is OK.
      if (!entry.isPrePassivated())
      {
         passivationManager.prePassivate(entry.getUnderlyingItem());
      }
      
      // If this call is coming via PassivatingBackingCache.passivate(), 
      // entry.group will *not* be null.  In that case we are the controller 
      // for the group passivation. If the call is coming via entry.prePassivate(), 
      // entry.group *will* be null. In that case we are not the controller 
      // of the passivation and can just return.
      SerializationGroup<C> group = entry.getGroup();
      if(group != null)
      {
         if (!group.tryLock())
            throw new IllegalStateException("Cannot obtain lock on " + group.getId() +  " to passivate " + entry);
         try
         {
            if (!group.isInvalid())
            {
               // Remove ourself from group's active list so we don't get
               // called again via entry.prePassivate()            
               group.removeActive(entry.getId());
               
               // Only tell the group to passivate if no members are in use
               if (group.getInUseCount() == 0)
               {
                  // Go ahead and do the real passivation.
                  groupCache.passivate(group.getId());
                  // Mark the group as invalid so if another thread is
                  // blocking on the above sync lock, when they enter
                  // we realize they have a ref to an out-of-date group
                  group.setInvalid(true);
               }         
               else {
                  // this turns into a pretty meaningless exercise of just
                  // passivating an empty entry. TODO consider throwing 
                  // ItemInUseException here, thus aborting everything.  Need to
                  // be sure that doesn't lead to problems as the exception propagates
                  if (log.isTraceEnabled())
                  {
                     log.trace("Group " + group.getId() + " has " + 
                                group.getInUseCount() + " in-use members; " + 
                                "not passivating group for " + entry.getId());
                  }
               }
               
               // This call didn't come through entry.prePassivate() (which nulls
               // group and obj) so we have to do it ourselves. Otherwise
               // when this call returns, delegate may serialize the entry
               // with a ref to group and obj.            
               entry.setGroup(null);
               entry.setUnderlyingItem(null);
            }
         }
         finally
         {
            group.unlock();
         }
      }
   }
   
   public void preReplicate(SerializationGroupMember<C> entry)
   {         
      // This method follows the same conceptual logic as prePassivate.
      // See the detailed comments in that method.
      
      log.trace("pre-replicate " + entry);
      
      if (!entry.isPreReplicated())
      {
         passivationManager.preReplicate(entry.getUnderlyingItem());      
         entry.setPreReplicated(true);
      }
      
      SerializationGroup<C> group = entry.getGroup();
      if(group != null)
      {
         group.lock();
         try
         {
            // Remove ourself from group's active list so we don't get
            // called again via entry.prePassivate()            
            group.removeActive(entry.getId());
            
            try
            {
               if (group.getInUseCount() == 0)
               {
                  group.getGroupCache().release(group.getId());
               }
            }
            finally
            {
               // Here we differ from prePassivate!!
               // Restore the entry as "active" so it can get
               // passivation callbacks
               group.addActive(entry);
            }
         
//            entry.setGroup(null);
//            entry.setUnderlyingItem(null);
         }
         finally
         {
            group.unlock();
         }
      }
   }
   
   public void postReplicate(SerializationGroupMember<C> entry)
   {
      log.trace("postReplicate(): " + entry);
      
      boolean groupOK = false;
      while (!groupOK)
      {
         // Restore the entry's ref to the group and object
         SerializationGroup<C> group = entry.getGroup();
         if(group == null && entry.getGroupId() != null)
         {
            // TODO: peek or get?
            // BES 2007/10/06 peek is better; you'll get multiple calls to
            // this as different members are accessed, and the cache
            // will throw an ISE if we use get() since we're already in use
//            group = groupCache.peek(entry.getGroupId());
            group = groupCache.get(entry.getGroupId());
         }
         
         if(group != null)
         {
            group.lock();
            try
            {
               if (!group.isInvalid())
               {
                  entry.setGroup(group);
                  entry.setUnderlyingItem(group.getMemberObject(entry.getId()));
            
                  // Notify the group that this entry is active
                  group.addActive(entry);
                  groupOK = true;
               }
               // else groupOK == false and we loop again
            }
            finally
            {
               group.unlock();
            }
         }
         else
         {
            groupOK = true;
         }
      }
      
      // Invoke callbacks on the underlying object
      if (entry.isPreReplicated())
      {
         passivationManager.postReplicate(entry.getUnderlyingItem());      
         entry.setPreReplicated(false);
      }
   }

   public void update(SerializationGroupMember<C> entry, boolean modified)
   {
      store.update(entry, modified);
   }

   public boolean isClustered()
   {
      return groupCache.isClustered();
   }
   
   public SerializationGroupMember<C> get(Object key)
   {
      SerializationGroupMember<C> entry = store.get(key);
      // In case it was deserialized, make sure it has a ref to us
      if (entry != null)
         entry.setPassivatingCache(delegate);
      return entry;
   }

   public void passivate(SerializationGroupMember<C> entry)
   {
      store.passivate(entry);         
   }

   public void insert(SerializationGroupMember<C> entry)
   {
      store.insert(entry);         
   }

   public SerializationGroupMember<C> remove(Object key)
   {
      return store.remove(key);
   }

   public void start()
   {         
      store.start();
   }

   public void stop()
   {
      store.stop();
   }

   public int getInterval()
   {
      return store.getInterval();
   }

   public void setInterval(int seconds)
   {
      store.setInterval(seconds);    
   }

   public void setPassivatingCache(PassivatingBackingCache<C, SerializationGroupMember<C>> cache)
   {
      if (! (cache instanceof GroupAwareBackingCache))
      {
         throw new IllegalArgumentException("cache must implement GroupAwareBackingCache");
      }
      
      this.delegate= (GroupAwareBackingCache<C, SerializationGroupMember<C>>) cache;
      this.store.setPassivatingCache(delegate);
   }

   public boolean isPassivationExpirationSelfManaged()
   {
      return store.isPassivationExpirationSelfManaged();
   }

   public void processPassivationExpiration()
   {         
      store.processPassivationExpiration();
   }      
}
