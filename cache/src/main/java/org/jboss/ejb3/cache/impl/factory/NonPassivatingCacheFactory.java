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

package org.jboss.ejb3.cache.impl.factory;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulCacheFactory;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.impl.TransactionalCache;
import org.jboss.ejb3.cache.impl.backing.NonPassivatingBackingCacheEntry;
import org.jboss.ejb3.cache.impl.backing.NonPassivatingBackingCacheImpl;
import org.jboss.ejb3.cache.spi.impl.AbstractStatefulCacheFactory;

/**
 * {@link StatefulCacheFactory} implementation that will return a 
 * non-group-aware cache that doesn't support passivation.
 * 
 * @see TransactionalCache
 * @see NonPassivatingBackingCacheImpl
 * 
 * @author Brian Stansberry
 */
public class NonPassivatingCacheFactory<T extends CacheItem> 
   extends AbstractStatefulCacheFactory<T>
{

   public Cache<T> createCache(String containerName, 
                               StatefulObjectFactory<T> factory, 
                               PassivationManager<T> passivationManager,
                               CacheConfig config)
   {
      NonPassivatingBackingCacheImpl<T> backingCache = 
         new NonPassivatingBackingCacheImpl<T>(factory, containerName);
     
      // Make sure passivation/expiration occurs periodically
      if (getPassivationExpirationCoordinator() != null)
      {
         // Let our coordinator manage this
         getPassivationExpirationCoordinator().addPassivationExpirationProcessor(backingCache);
      }
      else 
      {
         // Tell the store to manage this itself, using our default interval
         backingCache.setInterval(getDefaultPassivationExpirationInterval());
      }
      
      return new TransactionalCache<T, NonPassivatingBackingCacheEntry<T>>(backingCache, getTransactionManager(), getSynchronizationCoordinator(), getStrictGroups());
   }

}
