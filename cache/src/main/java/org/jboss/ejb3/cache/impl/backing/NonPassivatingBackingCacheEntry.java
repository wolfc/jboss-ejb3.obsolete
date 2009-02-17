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
package org.jboss.ejb3.cache.impl.backing;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.impl.AbstractBackingCacheEntry;

/**
 * Basic {@link BasicCacheEntry} implementation for use with a non-passivating
 * {@link BackingCache}.  A wrapper for the {@link CacheItem} to allow it to 
 * be managed by the backing cache .
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class NonPassivatingBackingCacheEntry<T extends CacheItem> extends AbstractBackingCacheEntry<T> 
   implements BackingCacheEntry<T>, Serializable
{   
   /** The serialVersionUID */
   private static final long serialVersionUID = 1325918596862109742L;
   
   private T wrapped;
   private ReentrantLock lock = new ReentrantLock();
   // guarded by lock
   private boolean valid = true;
   
   /**
    * Create a new SimpleBackingCacheEntry.
    * 
    * @param wrapped the item to wrap
    */
   public NonPassivatingBackingCacheEntry(T wrapped)
   {
      this.wrapped = wrapped;
   }
   
   // -------------------------------------------------------- BackingCacheEntry

   public T getUnderlyingItem()
   {
      return wrapped;
   }
   
   public boolean isModified()
   {      
      return wrapped.isModified();
   }

   /**
    * {@inheritDoc}
    * 
    * @return the id of the {@link BackingCacheEntry#getUnderlyingItem() underlying item}.
    *         Cannot be <code>null</code>.
    */
   public Object getId()
   {
      return wrapped.getId();
   }
     

   public void lock()
   { 
      try
      {
         lock.lockInterruptibly();
      }
      catch (InterruptedException ie)
      {
         throw new RuntimeException("interrupted waiting for lock");
      }
   }

   public boolean tryLock()
   {
     return lock.tryLock();
   }

   public void unlock()
   {
      if (lock.isHeldByCurrentThread())
         lock.unlock();
      
   }

   public void invalidate()
   {
      this.valid = false;
   }

   public boolean isValid()
   {
      return valid;
   }
   
   
}
