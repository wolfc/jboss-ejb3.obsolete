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
 * An object that can be managed by a {@link BackingCache}.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public interface BackingCacheEntry<T extends CacheItem> 
   extends CacheItem
{  
   /**
    * Gets the underlying object that should be serialized as part of 
    * serialization of the group.
    * 
    * @return
    */
   T getUnderlyingItem();
   
   /**
    * Gets whether this object is in use by a caller.
    */
   boolean isInUse();
   
   /**
    * Sets whether this object is in use by a caller.
    * 
    * @param inUse
    */
   void setInUse(boolean inUse);
   
   /**
    * Gets the timestamp of the last time this object was in use.
    * 
    * @return
    */
   long getLastUsed();
   
   /**
    * Gets whether the entry can be passivated without invoking
    * any callbacks on the underlying item.
    */
   boolean isPrePassivated();
   
   /**
    * Sets whether the entry can be passivated without invoking 
    * any callbacks on the underlying item.
    */
   void setPrePassivated(boolean prePassivated);
}
