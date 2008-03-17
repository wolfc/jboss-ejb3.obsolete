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
package org.jboss.ejb3.cache.spi;

import org.jboss.ejb3.cache.api.CacheItem;


/**
 * A {@link BackingCache} which passivates unused objects.
 * <p>
 * A PassivatingBackingCache is linked to an ObjectStore to store the
 * passivated object and to a PassivationManager to manage lifecycle
 * callbacks on the object.
 * </p>
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * 
 * @version $Revision: 65977 $
 */
public interface PassivatingBackingCache<C extends CacheItem, T extends PassivatingBackingCacheEntry<C>>
   extends BackingCache<C, T>
{
   /**
    * Force passivation of an object. The object must not be 
    * {@link BackingCacheEntry#isInUse() in use}.
    * 
    * @param key    the identifier of the object
    * 
    * @throws IllegalStateException if the object, or another object in the 
    *                            same {@link SerializationGroup} as the object, 
    *                            is in use. 
    */
   void passivate(Object key);
}
