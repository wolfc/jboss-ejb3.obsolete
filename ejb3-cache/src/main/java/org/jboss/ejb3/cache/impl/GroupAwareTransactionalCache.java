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

import java.util.ArrayList;
import java.util.List;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.SerializationGroup;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.GroupAwareBackingCache;
import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;
import org.jboss.ejb3.cache.spi.impl.GroupCreationContext;
import org.jboss.ejb3.cache.spi.impl.ItemCachePair;

/**
 * {@link Cache#isGroupAware Group-aware} version of {@link TransactionalCache}.
 * 
 * @author Brian Stansberry
 */
public class GroupAwareTransactionalCache<C extends CacheItem, T extends BackingCacheEntry<C>> 
   extends TransactionalCache<C, T>
{
   /** 
    * Another ref to super.delegate. Just saves having to do casts all the time. 
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
                                       SynchronizationCoordinator syncCoordinator)
   {
      super(delegate, tm, syncCoordinator);
      this.groupedCache = delegate;
   }

   @Override
   @SuppressWarnings("unchecked")
   public Object create(Class<?>[] initTypes, Object[] initValues)
   {
      boolean outer = false;
      List<ItemCachePair> contextPairs = GroupCreationContext.getGroupCreationContext();
      if (contextPairs == null)
      {
         contextPairs = new ArrayList<ItemCachePair>();
         GroupCreationContext.setGroupCreationContext(contextPairs);
         outer = true;
      }
      
      C cacheItem = super.createInternal(initTypes, initValues);
      
      contextPairs.add(new ItemCachePair(cacheItem, groupedCache));
            
      if (outer)
      {
         GroupCreationContext.setGroupCreationContext(null);
         if (contextPairs.size() > 1)
         {
            SerializationGroup<C> group = null;
            try
            {
               for (ItemCachePair pair : contextPairs)
               {
                  if (group == null)
                  {
                     group = pair.getCache().createGroup();
                  }
                  pair.getCache().setGroup(pair.getItem(), group);
               }
            }
            catch (IllegalStateException e)
            {
               // Clean up
               for (ItemCachePair pair : contextPairs)
               {
                  pair.getCache().remove(pair.getItem().getId());
               }
               throw e;
            }
         }
      }
      
      return cacheItem.getId();
   }

   @Override
   public boolean isGroupAware()
   {
      return true;
   }

   @Override
   public SerializationGroup<C> getGroup(C obj)
   {
      return groupedCache.getGroup(obj);
   }

}
