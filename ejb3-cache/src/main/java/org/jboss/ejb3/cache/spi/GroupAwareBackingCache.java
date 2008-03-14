/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * A {@link BackingCache} that can manage the relationship of its 
 * underlying entries to any {@link SerializationGroup}.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public interface GroupAwareBackingCache<C extends CacheItem, T extends BackingCacheEntry<C>>
   extends PassivatingBackingCache<C, T>
{
   /**
    * Create a {@link SerializationGroup} to contain objects cached by
    * this object.
    * 
    * @return a {@link SerializationGroup} 
    */
   SerializationGroup<C> createGroup();

   /**
    * Assign the given object to the given group.  The group will be
    * of the {@link SerializationGroup} implementation type returned
    * by {@link #createGroup()}.
    * 
    * @param obj
    * @param group
    * 
    * @throws IllegalStateException if the group's cache is incompatible
    *                                       with ourself.
    */
   void setGroup(C obj, SerializationGroup<C> group);

   /**
    * Gets the group the given object is a member of
    * 
    * @param obj the object
    * @return the group, or <code>null</code> if the object is not a member
    *         of a group
    */
   SerializationGroup<C> getGroup(C obj);
}
