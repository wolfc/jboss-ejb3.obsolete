/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.cache.impl;

import java.io.Serializable;

import javax.ejb.NoSuchEJBException;

import org.jboss.ejb3.cache.Cacheable;
import org.jboss.ejb3.cache.IntegratedObjectStore;
import org.jboss.ejb3.cache.PassivatingCache;
import org.jboss.ejb3.cache.PassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.grouped.GroupedPassivatingCache;
import org.jboss.ejb3.cache.grouped.SerializationGroup;
import org.jboss.logging.Logger;

/**
 * {@link GroupedPassivatingCache} that uses an {@link IntegratedObjectStore}
 * to manage data.
 *
 * @author Brian Stansberry
 * @version $Revision$
 */
public class GroupedPassivatingCacheImpl2<T extends Cacheable & Serializable> implements GroupedPassivatingCache<T>
{
   private static final Logger log = Logger.getLogger(GroupedPassivatingCacheImpl2.class);
   
   /**
    * Cache that's managing the PassivationGroup
    */
   private PassivatingCache<SerializationGroup> groupCache;
   
   /**
    *  Delegate that handles the usual details; ends up calling into
    *  our EntryContainer for StatefulObjectFactory, PassivationManager
    *  and IntegratedObjectStore functions.
    */
   private SimplePassivatingCache2<SerializationGroupMemberImpl<T>> delegate;
   
   /** 
    * Do we support clustering? This field is really just a minor
    * optimization to avoid calling through to the underlying 
    * IntegratedObjectStore all the time.
    */
   private boolean clustered;
   
   private EntryContainer entryContainer;
   
   private class EntryContainer   
      implements StatefulObjectFactory<SerializationGroupMemberImpl<T>>, PassivationManager<SerializationGroupMemberImpl<T>>, IntegratedObjectStore<SerializationGroupMemberImpl<T>>
   {
      private StatefulObjectFactory<T> factory;
      private PassivationManager<T> passivationManager;
      private IntegratedObjectStore<SerializationGroupMemberImpl<T>> store;
      
      EntryContainer(StatefulObjectFactory<T> factory, PassivationManager<T> passivationManager, IntegratedObjectStore<SerializationGroupMemberImpl<T>> store)
      {
         this.factory = factory;
         this.passivationManager = passivationManager;
         this.store = store;
      }
      
      public SerializationGroupMemberImpl<T> create(Class<?>[] initTypes, Object[] initValues)
      {
         return new SerializationGroupMemberImpl<T>(factory.create(initTypes, initValues), 
                             delegate);
      }

      public void destroy(SerializationGroupMemberImpl<T> entry)
      {
         factory.destroy(entry.getSerializableObject());
         if (entry.getGroup() != null) 
         {
            entry.getGroup().removeMember(entry.getId());
            if (entry.getGroup().size() == 0)
            {
               groupCache.remove(entry.getGroupId());
            }
         }
      }
      
      @SuppressWarnings("unchecked")
      public void postActivate(SerializationGroupMemberImpl<T> entry)
      {
         log.trace("post activate " + entry);
         
         // Restore the entry's ref to the group and object
         if(entry.getGroup() == null)
         {
            // TODO: peek or get?
            // BES 2007/10/06 I think peek is better; no
            // sense marking the group as in-use and then having
            // to release it or something
            entry.setGroup(groupCache.peek(entry.getGroupId()));
         }
         
         if(entry.getGroup() != null)
         {
            entry.setSerializableObject((T) entry.getGroup().getMemberObject(entry.getId()));
            
            // Notify the group that this entry is active
            entry.getGroup().addActive(entry);
         }
         
         // Invoke callbacks on the underlying object
         passivationManager.postActivate(entry.getSerializableObject());
      }
      
      public void prePassivate(SerializationGroupMemberImpl<T> entry)
      {
         log.trace("pre-passivate " + entry);
         
         // entry.obj may or may not get serialized (depends on if group
         // is in use) but it's ok to invoke callbacks now. If a caller
         // requests this entry again and the obj hadn't been serialized with
         // the group, we'll just call postActivate on it then, which is OK.
         // By always invoking the callbacks here, we avoid possible bugs
         // where they sometimes don't get called.
         passivationManager.prePassivate(entry.getSerializableObject());
         
         // If this call is coming via delegate.passivate(), entry.group will
         // *not* be null.  In that case we are the controller for the
         // group passivation. If the call is coming via Entry.prePassivate(), 
         // entry.group *will* be null. In that case we are not the controller 
         // of the passivation and can just return.
         if(entry.getGroup() != null)
         {
            // Remove ourself from group's active list so we don't get
            // called again via Entry.prePassivate()            
            entry.getGroup().removeActive(entry.getId());
            
            // Only tell the group to passivate if no members are in use
            if (!entry.getGroup().isInUse())
            {
               // Tell group to prePassivate other active members
               entry.getGroup().prePassivate();
               // Go ahead and do the real passivation
               groupCache.passivate(entry.getGroupId());
            }
            // else {
            // this turns into a pretty meaningless exercise of just
            // passivating an empty Entry. TODO consider throwing 
            // ItemInUseException here, thus aborting everything.  Need to
            // be sure that doesn't lead to problems as the exception propagates
            // }
            
            // This call didn't come through Entry.prePassivate() (which nulls
            // group and obj) so we have to do it ourselves. Otherwise
            // when this call returns, delegate will serialize the entry
            // with a ref to group and obj.            
            entry.setGroup(null);
            entry.setSerializableObject(null);
         }
      }
      
      public void preReplicate(SerializationGroupMemberImpl<T> entry)
      {         
         // This method follows the same conceptual logic as prePassivate.
         // See the detailed comments in that method.
         
         log.trace("pre-replicate " + entry);
         
         passivationManager.preReplicate(entry.getSerializableObject());
         
         if(entry.getGroup() != null)
         {
            entry.getGroup().removeActive(entry.getId());
            
            try
            {
               if (!entry.getGroup().isInUse())
               {
                  entry.getGroup().preReplicate();
                  groupCache.replicate(entry.getGroupId());
               }
            }
            finally
            {
               // Here we differ from prePassivate!!
               // Restore the entry as "active" so it can get
               // passivation callbacks
               entry.getGroup().addActive(entry);
            }
            
            entry.setGroup(null);
            entry.setSerializableObject(null);
         }
      }
      
      @SuppressWarnings("unchecked")
      public void postReplicate(SerializationGroupMemberImpl<T> entry)
      {
         log.trace("postreplicate " + entry);
         
         // Restore the entry's ref to the group and object
         if(entry.getGroup() == null)
         {
            // TODO: peek or get?
            // BES 2007/10/06 I think peek is better; no
            // sense marking the group as in-use and then having
            // to release it or something
            entry.setGroup(groupCache.peek(entry.getGroupId()));
         }
         
         if(entry.getGroup() != null)
         {
            entry.setSerializableObject((T) entry.getGroup().getMemberObject(entry.getId()));
            
            // Notify the group that this entry is active
            entry.getGroup().addActive(entry);
         }
         
         // Invoke callbacks on the underlying object
         passivationManager.postReplicate(entry.getSerializableObject());
      }

      public void update(SerializationGroupMemberImpl<T> entry)
      {
         store.update(entry);
      }

      public boolean isClustered()
      {
         // Use value from containing cache; containing cache c'tor ensures
         // the underlying store matches this
         return clustered;
      }
      
      public SerializationGroupMemberImpl<T> get(Object key)
      {
         SerializationGroupMemberImpl<T> entry = store.get(key);
         // In case it was deserialized, make sure it has a ref to us
         entry.setPassivatingCache(delegate);
         return entry;
      }

      public void passivate(SerializationGroupMemberImpl<T> entry)
      {
         store.passivate(entry);         
      }

      public void insert(SerializationGroupMemberImpl<T> entry)
      {
         store.insert(entry);         
      }

      public SerializationGroupMemberImpl<T> remove(Object key)
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
   }
   
   public GroupedPassivatingCacheImpl2(StatefulObjectFactory<T> factory, PassivationManager<T> passivationManager, IntegratedObjectStore<SerializationGroupMemberImpl<T>> store, PassivatingCache<SerializationGroup> groupCache)
   {
      assert groupCache != null : "groupCache is null";
      assert passivationManager != null : "passivationManager is null";
      assert store != null : "store is null";
      assert groupCache.isClustered() == store.isClustered(): "incompatible clustering support between groupCache and store";
      assert groupCache.isClustered() == passivationManager.isClustered(): "incompatible clustering support between groupCache and passivationManager";

      this.clustered = store.isClustered();
      this.groupCache = groupCache;
      entryContainer = new EntryContainer(factory, passivationManager, store);
      this.delegate = new SimplePassivatingCache2<SerializationGroupMemberImpl<T>>(entryContainer, entryContainer, entryContainer);
      
      // We pass 'entryContainer' to the delegate, and that's not a PassivatingIntegratedObjectStore
      // so delegate won't provide the real store with a ref. So we do it here.
      if (store instanceof PassivatingIntegratedObjectStore)
      {
         ((PassivatingIntegratedObjectStore<SerializationGroupMemberImpl<T>>) store).setPassivatingCache(delegate);
      }
   }
   
   public boolean isClustered()
   {
      return clustered;
   }
   
   public void replicate(Object key)
   {
      delegate.replicate(key);
   }
   
   public void passivate(Object key)
   {
      delegate.passivate(key);
   }

   public T create(Class<?>[] initTypes, Object[] initValues)
   {
      return delegate.create(initTypes, initValues).getSerializableObject();
   }

   public T get(Object key) throws NoSuchEJBException
   {
      SerializationGroupMemberImpl<T> entry = delegate.get(key);
      if (entry.getGroup() != null)
      {
         entry.getGroup().addInUse(key);
      }
      return entry.getSerializableObject();
   }

   public T peek(Object key) throws NoSuchEJBException
   {
      return delegate.peek(key).getSerializableObject();
   }

   public void release(T obj)
   {
      Object key = obj.getId();
      SerializationGroupMemberImpl<T> entry = delegate.releaseByKey(key);
      if (entry.getGroup() != null)
      {
         entry.getGroup().removeInUse(key);
      }      
   }

   public void remove(Object key)
   {
      delegate.remove(key);
   }
   
   public void setGroup(T obj, SerializationGroup group)
   {
      SerializationGroupMemberImpl<T> entry;
      Object key = obj.getId();
      entry = delegate.peek(key);
      if(entry.getGroup() != null)
         throw new IllegalStateException("object " + key + " already associated with a passivation group");
      entry.setGroup(group);
      entry.setGroupId(group.getId());
      entry.getGroup().addMember(entry);
   }
   
   public void start()
   {
      delegate.start();
   }

   public void stop()
   {
      delegate.stop();
   }
}
