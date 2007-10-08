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
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.grouped.GroupedPassivatingCache;
import org.jboss.ejb3.cache.grouped.SerializationGroup;
import org.jboss.ejb3.cache.grouped.SerializationGroupMember;
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
   private SimplePassivatingCache2<Entry<T>> delegate;
   
   /** 
    * Do we support clustering? This field is really just a minor
    * optimization to avoid calling through to the underlying 
    * IntegratedObjectStore all the time.
    */
   private boolean clustered;
   
   public class Entry<C extends Cacheable & Serializable> implements Cacheable, SerializationGroupMember, Serializable
   {
      private static final long serialVersionUID = 1L;
      
      Object id;
      /**
       * The underlying object (e.g. bean context).
       * Preferably, this field would be transient. It isn't now because it is 
       * possible this entry will never be assigned to a PassivationGroup,
       * in which case we need to serialize obj.
       * TODO Relying on nulling this field is fragile. Can we make this
       * field transient by ensuring we only use this cache class with bean 
       * classes that are sure to be part of a group?
       */
      C obj;
      /** The group. Never serialize the group; only the groupCache does that */
      transient SerializationGroup group;
      Object groupId;
      long lastUsed;
      
      Entry(C obj)
      {
         assert obj != null : "obj is null";
         
         this.obj = obj;
         this.id = obj.getId();
      }
      
      public Object getId()
      {
         return id;
      }
      
      public boolean isClustered()
      {
         // Value from the containing cache
         return clustered;
      }
      
      @SuppressWarnings("unchecked")
      public C getSerializableObject()
      {
         return obj;
      }
      
      // Called by PassivationGroup prior to its passivating
      public void prePassivate()
      {
         // make sure we don't passivate the group twice
         group = null;
         // null out obj so when delegate passivates this entry
         // we don't serialize it. It serializes with the PassivationGroup only
         obj = null;
         
         delegate.passivate(this.id);
      }
      
      // Called by PassivationGroup prior to its replicating
      public void preReplicate()
      {
         // make sure we don't replicate the group twice
         group = null;
         // null out obj so when delegate passivates this entry
         // we don't serialize it. It serializes with the PassivationGroup only
         obj = null;
         
         delegate.replicate(this.id);
      }
      
      public long getLastUsed()
      {
         return obj == null ? lastUsed : obj.getLastUsed();
      }

      public boolean isInUse()
      {
         return obj == null ? false : obj.isInUse();
      }

      public void setInUse(boolean inUse)
      {
         if (obj != null)
         {
            obj.setInUse(inUse);
            lastUsed = obj.getLastUsed();
         }
      }

      @Override
      public String toString()
      {
         return super.toString() + "{id=" + id + ",obj=" + obj + ",groupId=" + groupId + ",group=" + group + "}";
      }
   }
   
   private class EntryContainer   
      implements StatefulObjectFactory<Entry<T>>, PassivationManager<Entry<T>>, IntegratedObjectStore<Entry<T>>
   {
      private StatefulObjectFactory<T> factory;
      private PassivationManager<T> passivationManager;
      private IntegratedObjectStore<Entry<T>> store;
      
      EntryContainer(StatefulObjectFactory<T> factory, PassivationManager<T> passivationManager, IntegratedObjectStore<Entry<T>> store)
      {
         this.factory = factory;
         this.passivationManager = passivationManager;
         this.store = store;
      }
      
      public Entry<T> create(Class<?>[] initTypes, Object[] initValues)
      {
         return new Entry<T>(factory.create(initTypes, initValues));
      }

      public void destroy(Entry<T> entry)
      {
         factory.destroy(entry.obj);
         if (entry.group != null) 
         {
            entry.group.removeMember(entry.id);
            if (entry.group.size() == 0)
            {
               groupCache.remove(entry.groupId);
            }
         }
      }
      
      @SuppressWarnings("unchecked")
      public void postActivate(Entry<T> entry)
      {
         log.trace("post activate " + entry);
         
         // Restore the entry's ref to the group and object
         if(entry.obj == null)
         {
            if(entry.group == null)
            {
               // TODO: peek or get?
               // BES 2007/10/06 I think peek is better; no
               // sense marking the group as in-use and then having
               // to release it or something
               entry.group = groupCache.peek(entry.groupId);
            }
            entry.obj = (T) entry.group.getMemberObject(entry.id);
         }
         
         // Notify the group that this entry is active
         entry.group.addActive(entry);
         
         // Invoke callbacks on the underlying object
         passivationManager.postActivate(entry.obj);
      }
      
      public void prePassivate(Entry<T> entry)
      {
         log.trace("pre-passivate " + entry);
         
         // entry.obj may or may not get serialized (depends on if group
         // is in use) but it's ok to invoke callbacks now. If a caller
         // requests this entry again and the obj hadn't been serialized with
         // the group, we'll just call postActivate on it then, which is OK.
         // By always invoking the callbacks here, we avoid possible bugs
         // where they sometimes don't get called.
         passivationManager.prePassivate(entry.obj);
         
         // If this call is coming via delegate.passivate(), entry.group will
         // *not* be null.  In that case we are the controller for the
         // group passivation. If the call is coming via Entry.prePassivate(), 
         // entry.group *will* be null. In that case we are not the controller 
         // of the passivation and can just return.
         if(entry.group != null)
         {
            // Remove ourself from group's active list so we don't get
            // called again via Entry.prePassivate()            
            entry.group.removeActive(entry.id);
            
            // Only tell the group to passivate if no members are in use
            if (!entry.group.isInUse())
            {
               // Tell group to prePassivate other active members
               entry.group.prePassivate();
               // Go ahead and do the real passivation
               groupCache.passivate(entry.groupId);
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
            entry.group = null;
            entry.obj = null;
         }
      }
      
      public void preReplicate(Entry<T> entry)
      {         
         // This method follows the same conceptual logic as prePassivate.
         // See the detailed comments in that method.
         
         log.trace("pre-replicate " + entry);
         
         passivationManager.preReplicate(entry.obj);
         
         if(entry.group != null)
         {
            entry.group.removeActive(entry.id);
            
            try
            {
               if (!entry.group.isInUse())
               {
                  entry.group.preReplicate();
                  groupCache.replicate(entry.groupId);
               }
            }
            finally
            {
               // Here we differ from prePassivate!!
               // Restore the entry as "active" so it can get
               // passivation callbacks
               entry.group.addActive(entry);
            }
            
            entry.group = null;
            entry.obj = null;
         }
      }
      
      @SuppressWarnings("unchecked")
      public void postReplicate(Entry<T> entry)
      {
         log.trace("postreplicate " + entry);
         
         // Restore the entry's ref to the group and object
         if(entry.obj == null)
         {
            if(entry.group == null)
            {
               // TODO: peek or get?
               // BES 2007/10/06 I think peek is better; no
               // sense marking the group as in-use and then having
               // to release it or something
               entry.group = groupCache.peek(entry.groupId);
            }
            entry.obj = (T) entry.group.getMemberObject(entry.id);
         }
         
         // Notify the group that this entry is active
         entry.group.addActive(entry);
         
         // Invoke callbacks on the underlying object
         passivationManager.postReplicate(entry.obj);
      }

      public void replicate(Entry<T> entry)
      {
         store.replicate(entry);
      }

      public boolean isClustered()
      {
         // Use value from containing cache; containing cache c'tor ensures
         // the underlying store matches this
         return clustered;
      }
      
      public Entry<T> get(Object key)
      {
         return store.get(key);
      }

      public void passivate(Entry<T> entry)
      {
         store.passivate(entry);         
      }

      public void insert(Entry<T> entry)
      {
         store.insert(entry);         
      }

      public Entry<T> remove(Object key)
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
   
   public GroupedPassivatingCacheImpl2(StatefulObjectFactory<T> factory, PassivationManager<T> passivationManager, IntegratedObjectStore<Entry<T>> store, PassivatingCache<SerializationGroup> groupCache)
   {
      assert groupCache != null : "groupCache is null";
      assert passivationManager != null : "passivationManager is null";
      assert store != null : "store is null";
      assert groupCache.isClustered() == store.isClustered(): "incompatible clustering support between groupCache and store";
      assert groupCache.isClustered() == passivationManager.isClustered(): "incompatible clustering support between groupCache and passivationManager";

      this.clustered = store.isClustered();
      this.groupCache = groupCache;
      EntryContainer container = new EntryContainer(factory, passivationManager, store);
      this.delegate = new SimplePassivatingCache2<Entry<T>>(container, container, container);
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
      return delegate.create(initTypes, initValues).obj;
   }

   public T get(Object key) throws NoSuchEJBException
   {
      Entry<T> entry = delegate.get(key);
      if (entry.group != null)
      {
         entry.group.addInUse(key);
      }
      return entry.obj;
   }

   public T peek(Object key) throws NoSuchEJBException
   {
      return delegate.peek(key).obj;
   }

   public void release(T obj)
   {
      Object key = obj.getId();
      Entry<T> entry = delegate.releaseByKey(key);
      if (entry.group != null)
      {
         entry.group.removeInUse(key);
      }      
   }

   public void remove(Object key)
   {
      delegate.remove(key);
   }
   
   public void setGroup(T obj, SerializationGroup group)
   {
      Entry<T> entry;
      Object key = obj.getId();
      entry = delegate.peek(key);
      if(entry.group != null)
         throw new IllegalStateException("object " + key + " already associated with a passivation group");
      entry.group = group;
      entry.groupId = group.getId();
      entry.group.addMember(entry);
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
