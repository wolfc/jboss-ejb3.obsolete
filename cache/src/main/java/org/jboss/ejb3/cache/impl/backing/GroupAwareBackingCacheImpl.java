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

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.GroupAwareBackingCache;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;

/**
 * Group-aware  version of {@link PassivatingBackingCacheImpl}.
 *
 * @author Brian Stansberry
 * @version $Revision$
 */
public class GroupAwareBackingCacheImpl<C extends CacheItem>
   extends PassivatingBackingCacheImpl<C, SerializationGroupMember<C>>
   implements GroupAwareBackingCache<C, SerializationGroupMember<C>>
{
   /**
    * Cache that's managing the SerializationGroup
    */
   private final PassivatingBackingCache<C, SerializationGroup<C>> groupCache;
   
   /**
    * Container for the group members.
    */
   private final SerializationGroupMemberContainer<C> memberContainer;
   
   /**
    * Creates a new GroupAwareCacheImpl.
    * 
    * @param memberContainer  the factory for the underlying CacheItems
    * @param groupCache  cache for the group
    */
   public GroupAwareBackingCacheImpl(SerializationGroupMemberContainer<C> memberContainer, 
                                     PassivatingBackingCache<C, SerializationGroup<C>> groupCache)
   {
      super(memberContainer, memberContainer, memberContainer);

      assert groupCache != null : "groupCache is null";
      
      this.groupCache = groupCache;
      this.memberContainer = memberContainer;
   }
   
   public SerializationGroup<C> createGroup()
   {
      return groupCache.create(null, null, null);
   }

   public void setGroup(C obj, SerializationGroup<C> group)
   {     
      Object key = obj.getId();
      SerializationGroupMember<C> entry = peek(key);
      entry.lock();
      try
      {
         if(entry.getGroup() != null)
            throw new IllegalStateException("object " + key + " is already associated with passivation group " + entry.getGroup());
         
         // Validate we share a common groupCache with the group
         if (!memberContainer.isCompatibleWith(group))
            throw new IllegalStateException(obj + " and " + group + " are incompatible");         
         
         entry.setGroup(group);
         entry.getGroup().addMember(entry);
      }
      finally
      {
         entry.unlock();
      }
   }

   public void notifyPreReplicate(SerializationGroupMember<C> entry)
   {
      log.trace("notifyPreReplicate " + entry);
      
      if (!entry.isPreReplicated())
      {
         // We just *try* to lock; a preReplication is low priority.
         if (!entry.tryLock())
         {
            // Abort; wait until whoever has the lock is done
            throw new IllegalStateException("entry " + entry + " is in use");
         }
         
         try
         {
            if(entry.isInUse())
            {
               throw new IllegalStateException("entry " + entry + " is in use");
            }
   
            memberContainer.preReplicate(entry);
            
            entry.setPreReplicated(true);
         }
         finally
         {
            entry.unlock();
         }
      }
   }
   
}
