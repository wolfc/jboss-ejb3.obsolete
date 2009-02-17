/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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

import java.util.HashSet;
import java.util.Set;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCache;
import org.jboss.ejb3.cache.spi.BackingCacheLifecycleListener;
import org.jboss.ejb3.cache.spi.BackingCacheLifecycleListener.LifecycleState;
import org.jboss.logging.Logger;

/**
 * Abstract superclass of {@link BackingCache} implementations.
 * Basically provides support for working with 
 * {@link BackingCacheLifecycleListener}s.
 * 
 * @author Brian Stansberry
 *
 * @param <T> the type of item being managed by the cache
 */
public abstract class AbstractBackingCache<T extends CacheItem>
{
   protected Logger log = Logger.getLogger(getClass().getName());
   
   private final Set<BackingCacheLifecycleListener> listeners;

   protected AbstractBackingCache()
   {
      this.listeners = new HashSet<BackingCacheLifecycleListener>();
   }

   public void addLifecycleListener(BackingCacheLifecycleListener listener)
   {
      assert listener != null : "listener is null";
      
      synchronized (listeners)
      {
         listeners.add(listener);
      }      
   }

   public void removeLifecycleListener(BackingCacheLifecycleListener listener)
   {  
      synchronized (listeners)
      {
         listeners.remove(listener);
      } 
   }

   protected void notifyLifecycleListeners(LifecycleState newState)
   {
      RuntimeException re = null;
      synchronized (listeners)
      {
         for (BackingCacheLifecycleListener listener : listeners)
         {
            try
            {
               listener.lifecycleChange(newState);
            }
            catch (RuntimeException e)
            {
               log.error("Listener " + listener + " threw Exception during notification of " + newState, e);
               if (re != null)
                  re = e;
            }
         }
      }
      
      if (re != null)
         throw re;
   }

}