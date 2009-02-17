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
package org.jboss.ejb3.cache.impl.backing;

import java.util.Map;

import javax.ejb.NoSuchEJBException;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.GroupCompatibilityChecker;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.BackingCacheLifecycleListener.LifecycleState;
import org.jboss.ejb3.cache.spi.impl.AbstractBackingCache;
import org.jboss.logging.Logger;
import org.jboss.util.UnreachableStatementException;

/**
 * Non group-aware {@link PassivatingBackingCache} that uses a 
 * {@link BackingCacheEntryStore} to manage data.
 *
 * @author Brian Stansberry
 * @version $Revision: 65339 $
 */
public class PassivatingBackingCacheImpl<C extends CacheItem, T extends BackingCacheEntry<C>>
   extends AbstractBackingCache<C>  
   implements PassivatingBackingCache<C, T>
{
   protected final Logger log = Logger.getLogger(getClass().getName());
   
   private StatefulObjectFactory<T> factory;
   private PassivationManager<T> passivationManager;
   private BackingCacheEntryStore<C, T> store;
   
   public PassivatingBackingCacheImpl(StatefulObjectFactory<T> factory, 
                                     PassivationManager<T> passivationManager, 
                                     BackingCacheEntryStore<C, T> store)
   {
      assert factory != null : "factory is null";
      assert passivationManager != null : "passivationManager is null";
      assert store != null : "store is null";
      
      this.factory = factory;
      this.passivationManager = passivationManager;
      this.store = store;
      this.store.setPassivatingCache(this);
   }
   
   public boolean isClustered()
   {
      return store.isClustered();
   }
   
   public T create(Class<?>[] initTypes, Object[] initValues, Map<Object, Object> sharedState)
   {
      T obj = factory.create(initTypes, initValues, sharedState);
      store.insert(obj);
      return obj;
   }

   public T get(Object key) throws NoSuchEJBException
   {
      if (log.isTraceEnabled())
         log.trace("get(): " + key);
      
      boolean valid = false;
      while (!valid)
      {
         T entry = store.get(key);
         
         if(entry == null)
            throw new NoSuchEJBException(String.valueOf(key));
         
         entry.lock();
         try
         {
            valid = entry.isValid();
            if (valid)
            {
               if (isClustered())
               {
                  passivationManager.postReplicate(entry);
               }
               
               passivationManager.postActivate(entry);
               
               entry.setPrePassivated(false);
               
               entry.setInUse(true);
               return entry;
            }
            // else discard and reacquire
         }
         finally
         {
            entry.unlock();
         }
      }
      
      throw new UnreachableStatementException();
   }

   public void passivate(Object key)
   {
      log.trace("passivate(): " + key);
      
      T entry = store.get(key);
      
      if(entry == null)
         throw new IllegalArgumentException("entry " + key + " not found in cache " + this);
      
      // We just *try* to lock; a passivation is low priority.
      if (!entry.tryLock())
         throw new IllegalStateException("entry " + entry + " is in use");
      
      try
      {
         if(entry.isInUse())
         {
            throw new IllegalStateException("entry " + entry + " is in use");
         }

         passivationManager.prePassivate(entry);
         
         entry.setPrePassivated(true);
         
         entry.invalidate();
         
         store.passivate(entry);
      }
      finally
      {
         entry.unlock();
      }
   }
   
   public T peek(Object key) throws NoSuchEJBException
   {
      if (log.isTraceEnabled())
         log.trace("peek(): " + key);
      
      T entry = store.get(key);
      if(entry == null)
         throw new NoSuchEJBException(String.valueOf(key));         
      return entry;
   }

   public T release(Object key)
   {      
      if (log.isTraceEnabled())
         log.trace("release(): " + key);
      
      T entry = store.get(key);
      if(entry == null)
         throw new IllegalStateException("object " + key + " not from this cache");
      
      entry.lock();
      try
      {
         entry.setInUse(false);
         
         boolean modified = entry.isModified();
         if (modified)
         {
            if (isClustered())
            {
               passivationManager.preReplicate(entry);
            }
         }

         store.update(entry, modified);
         return entry;
      }
      finally
      {
         entry.unlock();
      }
   }
   
   public void remove(Object key)
   {
      if (log.isTraceEnabled())
         log.trace("remove(): " + key);
      
      T entry = store.remove(key);
      
      if(entry == null)
         throw new NoSuchEJBException(String.valueOf(key));
      
      entry.lock();
      try
      {
         if(entry.isInUse())
            entry.setInUse(false);
         factory.destroy(entry);
      }  
      finally
      {
         entry.unlock();
      }
   }
   
   public void start()
   {
      notifyLifecycleListeners(LifecycleState.STARTING);
      try
      {
         store.start();
         
         notifyLifecycleListeners(LifecycleState.STARTED);
      }
      catch (RuntimeException e)
      {
         notifyLifecycleListeners(LifecycleState.FAILED);
         throw e;
      }
   }

   public void stop()
   {
      notifyLifecycleListeners(LifecycleState.STOPPING);
      try
      {
         store.stop();
         
         notifyLifecycleListeners(LifecycleState.STOPPED);
      }
      catch (RuntimeException e)
      {
         notifyLifecycleListeners(LifecycleState.FAILED);
         throw e;
      }
   }

   public GroupCompatibilityChecker getCompatibilityChecker()
   {
      return store;
   }
   
}
