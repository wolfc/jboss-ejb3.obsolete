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

import org.jboss.ejb3.cache.api.Cache;
import org.jboss.ejb3.cache.api.CacheItem;

/**
 * An {@link IntegratedObjectStore} that is able to use its knowledge of
 * when objects are accessed to coordinate the passivation and removal of 
 * cached objects.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public interface PassivatingIntegratedObjectStore<C extends CacheItem, T extends BackingCacheEntry<C>>
   extends IntegratedObjectStore<T>, PassivationExpirationProcessor
{
   /**
    * Gets how often, in seconds, this object should process 
    * {@link #runPassivation() passivations} and
    * {@link #runExpiration() expirations}.
    * 
    * @return  interval, in seconds, at which passivations and expirations
    *          are processed. A value of less than 1 means this object will 
    *          not itself initiate processing, depending instead on an external 
    *          caller to do so.
    */
   int getInterval();
   
   /**
    * Sets how often, in seconds, this object should process 
    * {@link #runPassivation() passivations} and
    * {@link #runExpiration() expirations}.
    * 
    * @param seconds  interval, in seconds, at which passivations and
    *                 expirations should be processed. A value of less than 1 
    *                 means this object will not itself initiate processing, 
    *                 depending instead on an external caller to do so.
    */
   void setInterval(int seconds);
   
   /**
    * Handback provided by the controlling {@link PassivatingBackingCache} to
    * allow the actual {@link PassivatingBackingCache#passivate(Object) passivate}
    * and {@link Cache#remove(Object) remove} calls.
    * 
    * @param cache
    */
   void setPassivatingCache(PassivatingBackingCache<C, T> cache);
   
   // TODO determine what the standard configurations are
   
//   int getPassivationTimeout();
//   void setPassivationTimeout(int timeout);
//   
//   int getRemovalTimeout();   
//   void setRemovalTimeout(int timeout);
}
