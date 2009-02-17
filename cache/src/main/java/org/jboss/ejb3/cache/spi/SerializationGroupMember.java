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

package org.jboss.ejb3.cache.spi;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.Identifiable;

/**
 * Specialized {@link BackingCacheEntry} that wraps an 
 * {@link #setUnderlyingItem(CacheItem) underlying CacheItem} 
 * and represents it as a strong>potential</strong> member of a 
 * {@link SerializationGroup}. Note that a particular instance need not actually 
 * be a member of a group; such an instance will return <code>null</code> 
 * from {@link #getGroupId()}.
 * 
 * @author Brian Stansberry
 *
 * @param <T> the type of the underlying item
 */
public interface SerializationGroupMember<T extends CacheItem>
  extends BackingCacheEntry<T>
{  
   /**
    * {@inheritDoc}
    * 
    * @return the id of the {@link BackingCacheEntry#getUnderlyingItem() underlying item}.
    *         Cannot be <code>null</code>.
    */
   Object getId();
   
   /**
    * Gets the group of which this object is a member.
    * 
    * @return the group. May return <code>null</code> if the instance is
    *         not a member of the group (use {@link #getGroupId()} to check
    *         for this) or does not currently have a reference to its group.
    */
   SerializationGroup<T> getGroup();
   
   /**
    * Sets the group to which this object belongs. The first time a non-null
    * value is passed to this method, a member should set its 
    * {@link #getGroupId() groupId}.
    * 
    * @param group  May be <code>null</code>, which does not mean the member
    *               is no longer part of a group, but rather that any
    *               reference to the group should be released.
    */
   void setGroup(SerializationGroup<T> group);
   
   /**
    * Gets the {@link Identifiable#getId()} of this object's 
    * {@link SerializationGroup}. Will return <code>null</code> if this
    * object has not yet been 
    * {@link #setGroup(SerializationGroup) assigned to a group}.
    *  
    * @return the group id, or <code>null</code>
    */
   Object getGroupId();
   
   /**
    * Instructs this group member to ensure any prePassivate callbacks are 
    * invoked on the underlying item and any internal references to its
    * group and underlying item are released.  Called in preparation for 
    * serialization of the group. Clearing references is necessary in order to 
    * ensure no stale references to group content are retained outside of 
    * the {@link SerializationGroup} itself.
    */
   void prePassivate();
   
   /**
    * Instructs this group member to ensure any preReplicate callbacks are 
    * invoked on the underlying item and any internal references to its
    * group and underlying item are released.  Called in preparation for 
    * serialization of the group. Clearing references is necessary in order to 
    * ensure no stale references to group content are retained outside of 
    * the {@link SerializationGroup} itself.
    */
   void preReplicate();
   
   /**
    * Provides a reference to the cache that is managing this item.
    * 
    * @param cache the cache. May be <code>null</code>.
    */
   void setPassivatingCache(GroupAwareBackingCache<T, SerializationGroupMember<T>> cache);
   
   /**
    * Sets this object's underlying item.
    * 
    * @param obj the underlying item.  Cannot be <code>null</code>.
    */
   void setUnderlyingItem(T obj);
   
   /**
    * Gets whether pre-replication callbacks have been invoked on
    * the underlying item.
    */
   boolean isPreReplicated();
   
   /**
    * Sets whether pre-replication callbacks have been invoked on the 
    * underlying item.
    */
   void setPreReplicated(boolean preReplicated);
   
   /**
    * {@inheritDoc}
    * <p>
    * If the lock on this member can be acquired and the member has 
    * {@link #getGroup() a reference to a group}, 
    * <code>SerializationGroup.tryLock()</code> will also be invoked. If the
    * group lock cannot be obtained, the member lock will be released and
    * <code>false</code> returned.
    * </p>
    */
   boolean tryLock();
   
   /**
    * {@inheritDoc}
    * <p>
    * If the lock on this member is acquired and the member has 
    * {@link #getGroup() a reference to a group}, 
    * <code>SerializationGroup.lock()</code> will also be invoked. If the
    * thread is interrupted waiting for the group lock, the member lock will be 
    * released.
    * </p>
    */
   void lock();
   
   /**
    * {@inheritDoc}
    * <p>
    * If the member has {@link #getGroup() a reference to a group}, 
    * <code>SerializationGroup.unlock()</code> will first be invoked.
    * </p>
    */
   void unlock();
}