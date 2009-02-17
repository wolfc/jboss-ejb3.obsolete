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
package org.jboss.ejb3.cache.impl.backing;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.Identifiable;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;
import org.jboss.ejb3.cache.spi.impl.AbstractBackingCacheEntry;
import org.jboss.logging.Logger;
import org.jboss.serial.io.MarshalledObject;
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
   implements BackingCacheEntry<T>, SerializationGroup<T>
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6181048392582344057L;

   private static final Logger log = Logger.getLogger(SerializationGroupImpl.class);

   private final Object id = new GUID();
   
   /** 
    * The actual underlying objects passed in via addMember(). We store them 
    * here so they aren't lost when they are cleared from the values
    * stored in the "members" map.
    */
   private transient Map<Object, T> memberObjects = new ConcurrentHashMap<Object, T>();
   
   /**
    * Marshalled version of memberObjects map. This is what is stored
    * after deserialization.  Transient so we can control serialization.
    */
   private transient MarshalledObject marshalledMembers;
   
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
   private transient PassivatingBackingCache<T, SerializationGroup<T>> groupCache;
   
   /** Is this object used in a clustered cache? */
   private boolean clustered;

   private transient boolean groupModified;
   
   private transient ReentrantLock lock = new ReentrantLock();
   
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
    */
   public void addMember(SerializationGroupMember<T> member)
   {
      Object key = member.getId();
      Map<Object, T> membObjs = getMemberObjects();
      if (membObjs.containsKey(key))
         throw new IllegalStateException(member + " is already a member");
      log.trace("add member " + key + ", " + member);
      membObjs.put(key, member.getUnderlyingItem());
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
      getMemberObjects().remove(key);
   }
   
   /**
    * Gets the number of group members.
    */
   public int size()
   {
      return getMemberObjects().size();
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
      return getMemberObjects().get(key);
   }
   
   /**
    * Prepare members for passivation.
    */
   public void prePassivate()
   {
      for(Iterator<SerializationGroupMember<T>> it = active.values().iterator(); it.hasNext();)
      {
         SerializationGroupMember<T> member = it.next();
         member.prePassivate();
         it.remove();
      }
   }
   
   /**
    * Notification that the group has been activated from a passivated state.
    */
   public void postActivate()
   {
   }
   
   /**
    * Prepare members for replication.
    */
   public void preReplicate()
   {
      for(Iterator<SerializationGroupMember<T>> it = active.values().iterator(); it.hasNext();)
      {
         SerializationGroupMember<T> member = it.next();
         member.preReplicate();
         it.remove();
      }
   }
   
   /**
    * Notification that the previously replicated group has been retrieved from 
    * a clustered cache.
    */
   public void postReplicate()
   {
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
         if (inUseKeys.size() == 0)
            setInUse(false);
         else
            setLastUsed(System.currentTimeMillis());
      }
      else if (!getMemberObjects().containsKey(key))
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
   
   public boolean isModified()
   {
      boolean result = this.groupModified;
      setGroupModified(false);
      return result;
   }
   
   public boolean isGroupModified()
   {
      return this.groupModified;
   }
   
   public void setGroupModified(boolean modified)
   {
      this.groupModified = modified;
   }
   
   /**
    * FIXME -- returns null; what should it do?
    */
   public T getUnderlyingItem()
   {
      return null;
   }

   public PassivatingBackingCache<T, SerializationGroup<T>> getGroupCache()
   {
      return groupCache;
   }
     

   public void lock()
   { 
      try
      {
         lock.lockInterruptibly();
      }
      catch (InterruptedException ie)
      {
         throw new RuntimeException("interrupted waiting for lock");
      }
   }

   public boolean tryLock()
   {
     return lock.tryLock();
   }

   public void unlock()
   {
      if (lock.isHeldByCurrentThread())
         lock.unlock();
      
   }

   @Override
   public String toString()
   {
      return super.toString() + "{id=" + id + "}";
   }

   public void setGroupCache(PassivatingBackingCache<T, SerializationGroup<T>> groupCache)
   {
      this.groupCache = groupCache;
   }
   
   @SuppressWarnings("unchecked")
   private Map<Object, T> getMemberObjects()
   {
      // Use our id as a lock object 
      synchronized (id)
      {
         if (memberObjects == null && marshalledMembers != null)
         {
            try
            {
               memberObjects = (Map<Object, T>) marshalledMembers.get();
               marshalledMembers = null;
            }
            catch (Exception e)
            {
               throw new RuntimeException("Cannot unmarshalled members of group " + id, e);
            }
         }
         return memberObjects;
      }
   }

   private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      lock = new ReentrantLock();
      marshalledMembers= (MarshalledObject) in.readObject();
      active = new HashMap<Object, SerializationGroupMember<T>>();
      inUseKeys = new HashSet<Object>();
   }   
   
   private void writeObject(java.io.ObjectOutputStream out)
      throws IOException
   {
      out.defaultWriteObject();
      MarshalledObject toWrite = marshalledMembers == null ? new MarshalledObject(memberObjects) 
                                                           : marshalledMembers;
      out.writeObject(toWrite);
   }
}
