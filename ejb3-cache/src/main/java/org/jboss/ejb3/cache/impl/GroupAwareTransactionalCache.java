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
import org.jboss.ejb3.cache.spi.GroupIncompatibilityException;

/**
 * {@link Cache#isGroupAware Group-aware} version of {@link TransactionalCache}.
 * 
 * @author Brian Stansberry
 */
public class GroupAwareTransactionalCache<C extends CacheItem, T extends BackingCacheEntry<C>> 
   extends TransactionalCache<C, T>
{
   @SuppressWarnings("unchecked")
   private static final ThreadLocal groupCreationContext = new ThreadLocal();
   
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
                                       TransactionManager tm)
   {
      super(delegate, tm);
      this.groupedCache = delegate;
   }

   @Override
   @SuppressWarnings("unchecked")
   public C create(Class<?>[] initTypes, Object[] initValues)
   {
      boolean outer = false;
      List<ItemCachePair> contextPairs = (List<ItemCachePair>) groupCreationContext.get();
      if (contextPairs == null)
      {
         contextPairs = new ArrayList<ItemCachePair>();
         groupCreationContext.set(contextPairs);
         outer = true;
      }
      
      C cacheItem = super.create(initTypes, initValues);
      
      contextPairs.add(new ItemCachePair(cacheItem, this));
            
      if (outer)
      {
         groupCreationContext.set(null);
         if (contextPairs.size() > 1)
         {
            SerializationGroup<C> group = null;
            try
            {
               boolean skipped = false;
               boolean added = false;
               for (ItemCachePair pair : contextPairs)
               {
                  if (pair.cache.isGroupAware())
                  {
                     if (skipped)
                        throw new GroupIncompatibilityException("Some caches in nested bean hierarchy are group-aware, some are not");
                     
                     if (group == null)
                     {
                        group = pair.cache.createGroup();
                     }
                     pair.cache.setGroup(pair.item, group);
                     added = true;
                  }
                  else if (added)
                  {
                     throw new GroupIncompatibilityException("Some caches in nested bean hierarchy are group-aware, some are not");
                  }
                  else 
                  {
                     skipped = true;
                  }
               }
            }
            catch (GroupIncompatibilityException e)
            {
               // Clean up
               for (ItemCachePair pair : contextPairs)
               {
                  pair.cache.remove(pair.item.getId());
               }
               throw new RuntimeException("Failed to create SerializationGroup for nested bean hierarchy", e);
            }
         }
      }
      
      return cacheItem;
   }

   @Override
   public boolean isGroupAware()
   {
      return true;
   }

   private void setGroup(C obj, SerializationGroup<C> group) throws GroupIncompatibilityException
   {
      groupedCache.setGroup(obj, group);
   }
   
   private SerializationGroup<C> createGroup() throws GroupIncompatibilityException
   {
      return groupedCache.createGroup();
   }

   @Override
   public SerializationGroup<C> getGroup(C obj)
   {
      return groupedCache.getGroup(obj);
   }
   
   private class ItemCachePair
   {
      private final C item;
      private final GroupAwareTransactionalCache<C, T> cache;
      
      ItemCachePair(C item, GroupAwareTransactionalCache<C, T> cache)
      {
         this.item = item;
         this.cache = cache;
      }
   }

}
