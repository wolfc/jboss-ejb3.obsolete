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

package org.jboss.ejb3.cache.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.GroupAwareBackingCache;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;
import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;
import org.jboss.ejb3.cache.spi.impl.GroupCreationContext;
import org.jboss.ejb3.cache.spi.impl.ItemCachePair;

/**
 * Group-aware version of {@link TransactionalCache}.
 * 
 * @author Brian Stansberry
 */
public class GroupAwareTransactionalCache<C extends CacheItem, T extends SerializationGroupMember<C>> 
   extends TransactionalCache<C, T>
{
   /** 
    * Another ref to super.delegate. Saves having to do casts all the time. 
    */
   private final GroupAwareBackingCache<C, T> groupedCache;
   
   /**
    * Create a new GroupAwareTransactionalCacheImpl.
    * 
    * @param delegate the backing cache
    * @param tm       the transaction manager
    */
   public GroupAwareTransactionalCache(GroupAwareBackingCache<C, T> delegate, 
                                       TransactionManager tm,
                                       SynchronizationCoordinator syncCoordinator,
                                       boolean strictGroups)
   {
      super(delegate, tm, syncCoordinator, strictGroups);
      this.groupedCache = delegate;
   }

   @Override
   @SuppressWarnings("unchecked")
   public Object create(Class<?>[] initTypes, Object[] initValues)
   {
      boolean outer = false;
      List<ItemCachePair> contextPairs = null;
      Map<Object, Object> sharedState = null;
      
      GroupCreationContext groupContext = GroupCreationContext.getGroupCreationContext();
      if (groupContext == null)
      {         
         groupContext = GroupCreationContext.startGroupCreationContext(getStrictGroups());
         sharedState = new ConcurrentHashMap<Object, Object>();
         groupContext.setSharedState(sharedState);
         contextPairs = groupContext.getPairs();
         outer = true;
      }
      else
      {
         sharedState = groupContext.getSharedState();
         if (sharedState == null)
         {
            // We're in a nested hierarchy, but so far no other cache is group-aware
            // Check if we're configured to object
            if (getStrictGroups())
            {
               throw new IllegalStateException("Incompatible cache implementations in nested hierarchy");
            }
            
            // It's OK; just set up the shared state for any other
            // later group participants to share with us.            
            sharedState = new ConcurrentHashMap<Object, Object>();
            groupContext.setSharedState(sharedState);
         }
         
         contextPairs = groupContext.getPairs();         
      }
      
      try
      {
         // Create our item. This may lead to nested calls to other caches
         C cacheItem = createInternal(initTypes, initValues, sharedState);
         
         contextPairs.add(new ItemCachePair(cacheItem, groupedCache));
               
         if (outer)
         {
            // If there is more than one item in the context, we need a group
            if (contextPairs.size() > 1)
            {            
               SerializationGroup<C> group = groupedCache.createGroup();
               for (ItemCachePair pair : contextPairs)
               {
                  C pairItem = (C) pair.getItem();
                  GroupAwareBackingCache<C, T> pairCache = 
                     (GroupAwareBackingCache<C, T>) pair.getCache();
                  pairCache.setGroup(pairItem, group);
               }
            }
         }
         return cacheItem.getId();
      }
      catch (RuntimeException e)
      {
         if (outer)
         {
            // Clean up
            for (ItemCachePair pair : contextPairs)
            {
               try
               {
                  pair.getCache().remove(pair.getItem().getId());
               }
               catch (Exception toLog)
               {
                  log.error("Caught exception removing " + pair.getItem());
               }
            }
         }
         throw e;
      }
      finally
      {
         if (outer)
            GroupCreationContext.clearGroupCreationContext();
      }      
   }

}
