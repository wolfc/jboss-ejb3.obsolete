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

package org.jboss.ejb3.cache.spi.impl;

import java.io.IOException;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;

/**
 * A member of a {@link SerializationGroupImpl}.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class SerializationGroupMember<T extends CacheItem> 
   extends AbstractBackingCacheEntry<T>
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 7268142730501106252L;

   /**
    * Identifier for our underlying object
    */
   private Object id;
   
   /**
    * The underlying object (e.g. bean context).
    * Preferably, this field would be transient. It isn't now because it is 
    * possible this entry will never be assigned to a PassivationGroup,
    * in which case we need to serialize obj.
    */
   private T obj;
   
   /**
    * Hack. We hold two refs to our object; one we clear in prePassivate,
    * one we keep, but it's transient.  getUnderlyingItem() returns
    * whichever is available, making it available for passivation callbacks.
    */
   private transient T transientObj;
   
   /** The group. Never serialize the group; only the groupCache does that */
   private transient SerializationGroupImpl<T> group;
   
   /**
    * Id for our group; serialize this so we can find our group again
    * after deserialization on a remote node.
    */
   private Object groupId;
   
   private boolean clustered;
   
   private boolean preReplicated;
   private boolean prePassivated;
   
   /** The cache that's handling us */
   private transient PassivatingBackingCache<T, SerializationGroupMember<T>> cache;
   
   public SerializationGroupMember(T obj, PassivatingBackingCache<T, SerializationGroupMember<T>> cache)
   {
      assert obj != null : "obj is null";
      assert cache != null : "cache is null";
      
      this.obj = transientObj = obj;
      this.id = obj.getId();
      this.cache = cache;
      this.clustered = cache.isClustered();
   }
   
   public Object getId()
   {
      return id;
   }
   
   public boolean isModified()
   {
      return (obj != null && obj.isModified());
   }
   
   /**
    * Gets whether this member supports clustering functionality.
    * 
    * @return <code>true</code> if clustering is supported, <code>false</code>
    *         otherwise
    */
   public boolean isClustered()
   {
      return clustered;
   }
   
   @SuppressWarnings("unchecked")
   public T getUnderlyingItem()
   {      
      return obj == null ? transientObj : obj;
   }
   
   /**
    * Sets the underlying {@link CacheItem} associated with this group member.
    * 
    * @param item the cache item
    */
   public void setUnderlyingItem(T obj)
   {
      this.obj = transientObj = obj;
   }
   
   /**
    * Gets the {@link SerializationGroupImpl} of which this object is a member.
    * 
    * @return the group. May return <code>null</code>
    */
   public SerializationGroupImpl<T> getGroup()
   {
      return (group == null || group.isInvalid()) ? null : group;
   }

   /**
    * Sets the {@link SerializationGroupImpl} of which this object is a member.
    * 
    * @param the group. May be <code>null</code>
    */
   public void setGroup(SerializationGroupImpl<T> group)
   {
      this.group = group;
      if (group != null)
         this.groupId = group.getId();
   }

   /**
    * Gets the id for the group
    * 
    * @return
    */
   public Object getGroupId()
   {
      return groupId;
   }

   /**
    * Prepare the group member for passivation. Ensure any @PrePassivate 
    * callback is invoked on the underlying object.  If we are a member of a 
    * group, ensure any reference to the 
    * {@link #getUnderlyingItem() underlying object} or to the 
    * {@link #getGroup()} is nulled.
    */
   public void prePassivate()
   {
      // make sure we don't passivate the group twice
      group = null;
      
      // null out obj so when delegate passivates this entry
      // we don't serialize it. It serializes with the PassivationGroup only  
      // We still have a ref to transientObj, so it can be retrieved
      // for passivation callbacks
      obj = null;
      
      cache.passivate(this.id);
   }
   
   public boolean isPrePassivated()
   {
      return prePassivated;
   }
   
   public void setPrePassivated(boolean prePassivated)
   {
      this.prePassivated = prePassivated;
   }
   
   /**
    * Notification that the group has been activated from a passivated state.
    */
   public void postActivate()
   {
      // no-op
   }
   
   /**
    * Prepare the group member for replication. Ensure any required callback
    * (e.g. @PreReplicaate) is invoked on the underlying object. If we are a 
    * member of a group, ensure any reference to the 
    * {@link #getUnderlyingItem() underlying object} or to the 
    * {@link #getGroup()} is nulled.
    * 
    * @throws UnsupportedOperationException if {@link #isClustered()} returns
    *                                       <code>false</code>
    */
   public void preReplicate()
   {
      // make sure we don't replicate the group twice
      group = null;
      // null out obj so when delegate serializes this entry
      // we don't serialize it. It serializes with the PassivationGroup only
      obj = null;
      
      // FIXME -- what does this do for us?
      // Nothing -- it will fail
//      cache.release(this);
   }
   
   public boolean isPreReplicated()
   {
      return preReplicated;
   }
   
   public void setPreReplicated(boolean preReplicated)
   {
      this.preReplicated = preReplicated;
   }
   
   /**
    * Notification that the previously replicated group has been retrieved from 
    * a clustered cache.
    */
   public void postReplicate()
   {
      // no-op
   }

   public void setInUse(boolean inUse)
   {
      super.setInUse(inUse);
      
      // Tell our group about it
      if (group != null)
      {
         if (inUse)
            group.addActive(this);
         else
            group.removeActive(id);
      }         
   }
   
   /**
    * Allows our controlling {@link PassivatingBackingCache} to provide
    * us a reference after deserialization.
    * 
    * @param delegate
    */
   public void setPassivatingCache(PassivatingBackingCache<T, SerializationGroupMember<T>> delegate)
   {
      assert delegate != null : "delegate is null";
      
      this.cache = delegate;
   }

   @Override
   public String toString()
   {
      return super.toString() + "{id=" + id + ",obj=" + obj + ",groupId=" + groupId + ",group=" + group + "}";
   }
   
   private void writeObject(java.io.ObjectOutputStream out) throws IOException
   {
      if (groupId != null)
      {
         group = null;
         obj = null;
      }
      out.defaultWriteObject();
   }
}
