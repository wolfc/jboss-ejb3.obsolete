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
package org.jboss.ejb3.cache.spi.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.Identifiable;
import org.jboss.ejb3.cache.SerializationGroup;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

/**
 * Defines a group of serializable objects which must be serialized in
 * one unit of work.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * @version $Revision: $
 */
public class SerializationGroupImpl<T extends CacheItem>  
   extends AbstractBackingCacheEntry<T>
   implements SerializationGroup<T>, BackingCacheEntry<T>
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6181048392582344057L;

   private static final Logger log = Logger.getLogger(SerializationGroupImpl.class);

   private Object id = new GUID();
   
   /** 
    * The actual underlying objects passed in via addMember(). We store them 
    * here so they aren't lost when they are cleared from the values
    * stored in the "members" map.
    */
   private Map<Object, T> memberObjects = new ConcurrentHashMap<Object, T>();
   
   /** 
    * The active group members.  We don't serialize these. Rather, it is
    * the responsibility of members to reassociate themselves with the 
    * group upon deserialization (via addActive())
    */
   private transient Map<Object, SerializationGroupMember<T>> active = 
         new HashMap<Object, SerializationGroupMember<T>>();
   
   /**
    * Set of keys passed to {@link #addInUse(Object)}
    */
   private transient Set<Object> inUseKeys = new HashSet<Object>();
   
   /** Transient ref to our group cache; used to validate compatibility */
   private transient PassivatingBackingCache<T, SerializationGroupImpl<T>> groupCache;
   
   /** Is this object used in a clustered cache? */
   private boolean clustered;

   private transient boolean invalid;
   
   public Object getId()
   {
      return id;
   }
   
   /**
    * Gets whether this groups supports (and requires) clustering functionality
    * from its members.
    * 
    * @return <code>true</code> if clustering is supported, <code>false</code>
    *         otherwise
    */
   public boolean isClustered()
   {
      return clustered;
   }
   
   /**
    * Sets whether this groups supports (and requires) clustering functionality
    * from its members.
    * 
    * @return
    */
   public void setClustered(boolean clustered)
   {
      this.clustered = clustered;
   }
   
   /**
    * Initially associates a new member with the group. Also
    * {@link #addActive(SerializationGroupMember) marks the member as
    * active}.
    * 
    * @param member
    * 
    * @throws IllegalStateException if <code>member</code> was previously
    *                               added to the group
    * @throws IllegalArgumentException if the 
    *   {@link SerializationGroupMember#isClustered() member's support for clustering}
    *   does not match {@link #isClustered() our own}.
    */
   public void addMember(SerializationGroupMember<T> member)
   {
      Object key = member.getId();
      if (memberObjects.containsKey(key))
         throw new IllegalStateException(member + " is already a member");
      log.trace("add member " + key + ", " + member);
      memberObjects.put(key, member.getUnderlyingItem());
      active.put(key, member);
   }
   
   /**
    * Remove the specified member from the group.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    */
   public void removeMember(Object key)
   {
      removeActive(key);
      memberObjects.remove(key);
   }
   
   /**
    * Gets the number of group members.
    */
   public int size()
   {
      return memberObjects.size();
   }
   
   /**
    * Returns the {@link SerializationGroupMember#getUnderlyingItem() member object}
    * associated with the member whose {@link Identifiable#getId() id}
    * matches <code>key</code>.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    * 
    * @return the object associated with the member, or <code>null</code> if
    *         <code>key</code> does not identify a member.
    */
   public T getMemberObject(Object key)
   {
      return memberObjects.get(key);
   }
   
   public Iterator<T> iterator()
   {
      return new UnmodifiableIterator<T>(memberObjects.values().iterator());
   }
   
   /**
    * Prepare members for passivation.
    */
   public void prePassivate()
   {
      for(SerializationGroupMember<T> member : active.values())
      {
         member.releaseReferences();
      }
      active.clear();
   }
   
   /**
    * Notification that the group has been activated from a passivated state.
    */
   public void postActivate()
   {
      invalid = false;
   }
   
   /**
    * Prepare members for replication.
    */
   public void preReplicate()
   {
      for(SerializationGroupMember<T> member : active.values())
      {
         member.releaseReferences();
      }
      active.clear();
   }
   
   /**
    * Notification that the previously replicated group has been retrieved from 
    * a clustered cache.
    */
   public void postReplicate()
   {
      invalid = false;
   }
   
   /**
    * Records that the given member is "active"; i.e. needs to have
    * @PrePassivate callbacks invoked before serialization.
    * 
    * @param member the member
    * 
    * @throws IllegalStateException if <code>member</code> wasn't previously
    *                               added to the group via
    *                               {@link #addMember(SerializationGroupMember)}
    */
   public void addActive(SerializationGroupMember<T> member)
   {
      Object key = member.getId();
      if (!memberObjects.containsKey(key))
         throw new IllegalStateException(member + " is not a member of " + this);
      active.put(key, member);
   }
   
   /**
    * Records that the given member is no longer "active"; i.e. does not need
    * to have @PrePassivate callbacks invoked before serialization.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    * 
    * @throws IllegalStateException if <code>member</code> wasn't previously
    *                               added to the group via
    *                               {@link #addMember(SerializationGroupMember)}
    */
   public void removeActive(Object key)
   {
      active.remove(key);
   }

   /**
    * Notification that the given member is "in use", and therefore the
    * group should not be serialized.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    * 
    * @throws IllegalStateException if <code>member</code> wasn't previously
    *                               added to the group via
    *                               {@link #addMember(SerializationGroupMember)}
    */
   public void addInUse(Object key)
   {
      if (!memberObjects.containsKey(key))
         throw new IllegalStateException(key + " is not a member of " + this);
      inUseKeys.add(key);
      setInUse(true);
   }

   /**
    * Notification that the given member is no longer "in use", and therefore 
    * should not prevent the group being serialized.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    * 
    * @throws IllegalStateException if <code>member</code> wasn't previously
    *                               added to the group via
    *                               {@link #addMember(SerializationGroupMember)}
    */
   public void removeInUse(Object key)
   {
      if (inUseKeys.remove(key))
      {
         setLastUsed(System.currentTimeMillis());
      }
      else if (!memberObjects.containsKey(key))
      {
            throw new IllegalStateException(key + " is not a member of " + this);
      }      
   }
   
   /**
    * Gets the number of members currently in use.
    */
   public int getInUseCount()
   {
      return inUseKeys.size();
   }
   
   /**
    * Always returns <code>true</code>.
    */
   public boolean isModified()
   {
      return true;
   }
   
   /**
    * Returns true if this object has been passivated (meaning whoever
    * holds a ref to it is holding an out-of-date object)
    * 
    * @return
    */
   public boolean isInvalid()
   {
      return invalid;
   }
   
   public void setInvalid(boolean invalid)
   {
      this.invalid = invalid;
   }
   
   /**
    * FIXME -- returns null; what should it do?
    */
   public T getUnderlyingItem()
   {
      return null;
   }

   public PassivatingBackingCache<T, SerializationGroupImpl<T>> getGroupCache()
   {
      return groupCache;
   }

   @Override
   public String toString()
   {
      return super.toString() + "{id=" + id + "}";
   }

   public void setGroupCache(PassivatingBackingCache<T, SerializationGroupImpl<T>> groupCache)
   {
      this.groupCache = groupCache;
   }

   private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      active = new HashMap<Object, SerializationGroupMember<T>>();
      inUseKeys = new HashSet<Object>();
   }   
   
   private class UnmodifiableIterator<C extends CacheItem & Serializable> implements Iterator<C>
   {
      private Iterator<C> backingIterator;
      
      public UnmodifiableIterator(Iterator<C> backingIterator)
      {
         assert backingIterator != null : "backingIterator is null";
         
         this.backingIterator = backingIterator;
      }

      public boolean hasNext()
      {
         return backingIterator.hasNext();
      }

      public C next()
      {
         return backingIterator.next();
      }

      public void remove()
      {
         throw new UnsupportedOperationException("remove is not supported");         
      }
   }
   
   
}
