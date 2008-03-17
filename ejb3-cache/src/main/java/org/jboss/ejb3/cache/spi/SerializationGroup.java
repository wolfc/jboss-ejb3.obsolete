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

import org.jboss.ejb3.cache.api.CacheItem;

/**
 * Specialized {@link BackingCacheEntry} that represents a group of 
 * underlying items that must always be serialized as a group and whose
 * members must have coordinated calls to passivation and replication
 * related callbacks.
 * <p>
 * The underlying items in the group are represented as instances of
 * {@link SerializationGroupMember}.
 * </p>
 * 
 * @author Brian Stansberry
 *
 * @param <T> the type of the underlying items that form the group.
 */
public interface SerializationGroup<T extends CacheItem>
   extends PassivatingBackingCacheEntry<T>
{
   /**
    * Adds a member to the group.
    * 
    * @param member the member. Cannot be <code>null</code>.
    */
   void addMember(SerializationGroupMember<T> member);
   
   /**
    * Removes a member from the group.
    * 
    * @param key the id of the member. Cannot be <code>null</code>.
    */
   void removeMember(Object key);
   
   /**
    * Gets the number of group members.
    */
   int size();
   
   /**
    * Gets whether the group should be considered invalid. A reference
    * to an invalid group should be replaced with a fresh reference gotten
    * from the group's cache.
    *  
    * @return
    */
   boolean isInvalid();
   
   /**
    * Marks the group as invalid (or once again as valid).
    */
   void setInvalid(boolean invalid);
   
   /**
    * Gets the {@link BackingCacheEntry#getUnderlyingItem() underlying item}
    * whose {@link SerializationGroupMember#getId() id} matches <code>key</code>.
    * 
    * @param key the id of the member. Cannot be <code>null</code>.
    * 
    * @return the member's underlying item.
    */
   T getMemberObject(Object key);
   
   /**
    * Marks the given member as being "active", i.e. in need of pre-passivation
    * and pre-replication callbacks before the group is passivated or replicated.
    * 
    * @param member the member. Cannot be <code>null</code>.
    */
   void addActive(SerializationGroupMember<T> member);
   /**
    * Marks the given member as no longer being "active", i.e. as no longer
    * in need of pre-passivation and pre-replication callbacks before the group 
    * is passivated or replicated.
    * 
    * @param key the id of the member. Cannot be <code>null</code>.
    */
   void removeActive(Object key);
   
   /**
    * Tells the group the given member is "in use". A group should not be
    * serialized while any members are in use.
    * 
    * @param key the id of the member. Cannot be <code>null</code>.
    */
   void addInUse(Object key);
   
   /**
    * Tells the group the given member is no longer "in use".
    * 
    * @param key the id of the member. Cannot be <code>null</code>.
    */
   void removeInUse(Object key);
   
   /**
    * Gets the number of group member's currently 
    * {@link BackingCacheEntry#setInUse(boolean) "in use"}.
    */
   int getInUseCount();
   
   /**
    * Gets the cache used to manage the group.
    * 
    * @return the cache.  Will not return <code>null</code> if the group is
    *         {@link #isInvalid() valid}.
    */
   PassivatingBackingCache<T, SerializationGroup<T>> getGroupCache();
   
   /**
    * Handback provided by the cache managing the group.
    * 
    * @param groupCache the cache. Cannot be <code>null</code>.
    */
   void setGroupCache(PassivatingBackingCache<T, SerializationGroup<T>> groupCache);
   
   /**
    * Callback that must be invoked before the group is replicated.
    */
   void preReplicate();
   
   /**
    * Callback that must be invoked some time after the group has been 
    * replicated but before a reference to any member of the group is provided
    * to an external caller.
    */
   void postReplicate();
   
   /**
    * Callback that must be invoked before the group is passivated.
    */
   void prePassivate();
   
   /**
    * Callback that must be invoked some time after the group has been 
    * activated but before a reference to any member of the group is provided
    * to an external caller.
    */
   void postActivate();
}