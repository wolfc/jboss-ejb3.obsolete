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
package org.jboss.ejb3.cache.spi.impl;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;

/**
 * Abstract superclass for {@link BackingCacheEntry} implementations.
 * 
 * @author Brian Stansberry
 *
 * @param <T> the type of item being managed by the entry
 */
public abstract class AbstractBackingCacheEntry<T extends CacheItem>
   implements BackingCacheEntry<T>
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 4562025672441864736L;
   
   private long lastUsed = System.currentTimeMillis();
   private transient boolean inUse;
   private boolean prePassivated;
   private transient boolean invalid;

   public long getLastUsed()
   {
      return lastUsed;
   }

   public boolean isInUse()
   {
      return inUse;
   }

   public void setInUse(boolean inUse)
   {
      this.inUse = inUse;
      setLastUsed(System.currentTimeMillis());
   }

   /** 
    * Exposed as public only as an aid to unit tests. In normal use
    * this should only be invoked internally or by subclasses.
    * 
    * @param lastUsed time since epoch when last used
    */
   public void setLastUsed(long lastUsed)
   {
      this.lastUsed = lastUsed;
   }

   public boolean isPrePassivated()
   {
      return prePassivated;
   }

   public void setPrePassivated(boolean prePassivated)
   {
      this.prePassivated = prePassivated;
   }

   public void invalidate()
   {
      this.invalid = true;
   }

   public boolean isValid()
   {
      return !invalid;
   }
   
   

}