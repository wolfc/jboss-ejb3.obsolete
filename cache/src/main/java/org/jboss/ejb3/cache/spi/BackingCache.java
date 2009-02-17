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

import java.util.Map;

import javax.ejb.NoSuchEJBException;

import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.cache.CacheItem;

/**
 * An internal cache to which an external-facing {@link Cache} delegates, either
 * directly or indirectly.
 * <p>
 * There are two key distinctions between a BackingCache and the external-facing 
 * Cache:
 * <ol>
 * <li>
 * A Cache directly handles external classes that implement the
 * limited {@link CacheItem} interface. CacheItem is deliberately limited to
 * avoid placing a implementation burden on external classes. A BackingCache 
 * works with instances of the more expressive internal interface 
 * {@link BackingCacheEntry}, and thus can directly implement more complex 
 * functionality.
 * </li>
 * <li>
 * A BackingCache does not attempt to control concurrent client access to its
 * cached {@link BackingCacheEntry} instances, beyond the simple act of
 * {@link BackingCacheEntry#setInUse(boolean) marking the entries as being
 * in or out of use}.  It assumes the external-facing Cache is preventing
 * concurrent access to a given entry and is properly coordinating calls
 * to the backing cache.
 * </li>
 * </ol>
 * </p>
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public interface BackingCache<C extends CacheItem, T extends BackingCacheEntry<C>> 
{
   /**
    * Creates and caches a new instance of <code>C</code>, wrapped by a new
    * <code>T</code>. The new <code>T</code> *is* returned, but is not 
    * regarded as being "in use". Callers *must not* attempt to use the 
    * underlying <code>C</code> without first calling {@link #get(Object)}. 
    * 
    * @param initTypes   the types of any <code>initValues</code>. 
    *                    May be <code>null</code>.
    * @param initValues  any parameters to pass to <code>T</code>'s constructor.
    *                    May be null, in which case a default constructor will
    *                    be used.
    * @param sharedState map into which any objects meant to be shared with
    *                    other members of the new item's group should be
    *                    stored. 
    * @return the new <code>T</code> 
    */
   T create(Class<?> initTypes[], Object initValues[], Map<Object, Object> sharedState);

   /**
    * Get the specified object from cache. This will mark the entry as being in use.
    * 
    * @param key    the identifier of the object
    * @return       the object
    * @throws NoSuchEJBException    if the object does not exist
    */
   T get(Object key) throws NoSuchEJBException;
   
   /**
    * Peek at an object which might be in use. Does not change the status
    * of the item in terms of whether it is in use.
    * 
    * @param key    the identifier of the object
    * @return       the object
    * @throws NoSuchEJBException    if the object does not exist
    */
   T peek(Object key) throws NoSuchEJBException;
   
   /**
    * Release the object from use.
    * 
    * @param key  the identifier of the object
    * 
    * @return the entry that was released
    */
   T release(Object key);

   /**
    * Remove the specified object from cache.
    * 
    * @param key    the identifier of the object
    */
   void remove(Object key);

   /**
    * Start the cache.
    */
   void start();

   /**
    * Stop the cache.
    */
   void stop();
   
   /**
    * Gets whether this cache supports clustering functionality.
    * 
    * @return <code>true</code> if clustering is supported, <code>false</code>
    *         otherwise
    */
   boolean isClustered();
   
   /**
    * Registers a listener for callbacks when the cache starts and stops.
    * 
    * @param listener the listener. Cannot be <code>null</code>.
    */
   void addLifecycleListener(BackingCacheLifecycleListener listener);
   
   /**
    * Removes a registered lifecycle listener.
    * 
    * @param listener the listener. Cannot be <code>null</code>.
    */
   void removeLifecycleListener(BackingCacheLifecycleListener listener);
}
