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

package org.jboss.ejb3.cache.spi.impl;

import org.jboss.ejb3.cache.api.Identifiable;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.IntegratedObjectStore;

/**
 * Encapsulation of the {@link Identifiable#getId() id} and
 * {@link BackingCacheEntry#getLastUsed() last used timestamp} of
 * a cached {@link BackingCacheEntry}.
 * <p>
 * Implements <code>Comparable</code> to make it easy to sort
 * for LRU comparisons.
 * </p>
 * 
 * @see IntegratedObjectStore#getInMemoryEntries()
 * @see IntegratedObjectStore#getPassivatedEntries()
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class CacheableTimestamp 
   implements Identifiable, Comparable<CacheableTimestamp>
{
   private Object id;
   private long lastUsed;
   
   public CacheableTimestamp(Object id, long lastUsed)
   {
      assert id != null : "id cannot be null";
      assert lastUsed > 0 : "lastUsed must be positive";
      
      this.id = id;
      this.lastUsed = lastUsed;
   }
   
   public Object getId()
   {
      return id;
   }
   
   public long getLastUsed()
   {
      return lastUsed;
   }

   /**
    * Compares based on {@link #getLastUsed() last used}, returning
    * -1 for earlier timestamps.
    */
   public int compareTo(CacheableTimestamp o)
   {
      if (this.lastUsed < o.lastUsed)
         return -1;
      else if (this.lastUsed > o.lastUsed)
         return 1;
      return 0;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      
      if (obj instanceof CacheableTimestamp)
      {
         return this.id.equals(((CacheableTimestamp) obj).id);
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      return id.hashCode();
   }   
}
