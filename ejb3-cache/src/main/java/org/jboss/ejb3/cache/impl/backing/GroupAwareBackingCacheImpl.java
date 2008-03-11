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

import javax.ejb.NoSuchEJBException;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.SerializationGroup;
import org.jboss.ejb3.cache.spi.GroupAwareBackingCache;
import org.jboss.ejb3.cache.spi.GroupIncompatibilityException;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.impl.SerializationGroupImpl;
import org.jboss.ejb3.cache.spi.impl.SerializationGroupMember;

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
   private PassivatingBackingCache<C, SerializationGroupImpl<C>> groupCache;
   
   /**
    * Creates a new GroupAwareCacheImpl.
    * 
    * @param memberContainer  the factory for the underlying CacheItems
    * @param groupCache  cache for the group
    */
   public GroupAwareBackingCacheImpl(SerializationGroupMemberContainer<C> memberContainer, 
                                     PassivatingBackingCache<C, SerializationGroupImpl<C>> groupCache)
   {
      super(memberContainer, memberContainer, memberContainer);
      assert groupCache != null : "groupCache is null";
      assert groupCache.isClustered() == memberContainer.isClustered(): "incompatible clustering support between groupCache and passivationManager";

      this.groupCache = groupCache;
   }
   
   public SerializationGroupImpl<C> createGroup()
   {
      return groupCache.create(null, null);
   }

   public void setGroup(C obj, SerializationGroup<C> group) throws GroupIncompatibilityException
   {     
      Object key = obj.getId();
      SerializationGroupMember<C> entry = peek(key);
      if(entry.getGroup() != null)
         throw new IllegalStateException("object " + key + " is already associated with passivation group " + entry.getGroup());
      
      // Validate we share a common groupCache with the group
      SerializationGroupImpl<C> groupImpl = (SerializationGroupImpl<C>) group;
      if (groupCache != groupImpl.getGroupCache())
         throw new GroupIncompatibilityException(obj + " and " + groupImpl + " use different group caches");
      
      entry.setGroup(groupImpl);
      entry.getGroup().addMember(entry);
   }

   public SerializationGroup<C> getGroup(C obj)
   {
      Object key = obj.getId();
      try
      {
         SerializationGroupMember<C> entry = peek(key);
         return entry.getGroup();
      }
      catch (NoSuchEJBException nsee)
      {
         return null;
      }
   }  
   
   
   
}
