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
package org.jboss.ejb3.cache.impl;

import java.io.Serializable;

import javax.ejb.NoSuchEJBException;

import org.jboss.ejb3.cache.Cacheable;
import org.jboss.ejb3.cache.IntegratedObjectStore;
import org.jboss.ejb3.cache.PassivatingCache;
import org.jboss.ejb3.cache.PassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.logging.Logger;

/**
 * Non group-aware {@link PassivatingCache} that uses an {@link IntegratedObjectStore}
 * to manage data.
 *
 * @author Brian Stansberry
 * @version $Revision: 65339 $
 */
public class SimplePassivatingCache2<T extends Cacheable & Serializable> implements PassivatingCache<T>
{
   private static final Logger log = Logger.getLogger(SimplePassivatingCache2.class);
   
   private StatefulObjectFactory<T> factory;
   private PassivationManager<T> passivationManager;
   private IntegratedObjectStore<T> store;
   
   public SimplePassivatingCache2(StatefulObjectFactory<T> factory, PassivationManager<T> passivationManager, IntegratedObjectStore<T> store)
   {
      assert factory != null : "factory is null";
      assert passivationManager != null : "passivationManager is null";
      assert store != null : "store is null";
      
      this.factory = factory;
      this.passivationManager = passivationManager;
      this.store = store;
      if (store instanceof PassivatingIntegratedObjectStore)
      {
         ((PassivatingIntegratedObjectStore<T>) store).setPassivatingCache(this);
      }
   }
   
   public boolean isClustered()
   {
      return store.isClustered();
   }
   
   public void replicate(Object key)
   {
      if (!isClustered())
      {
         throw new UnsupportedOperationException("Clustering is not supported by " + 
                                                 store.getClass().getName());
      }
      
      log.trace("replicate " + key);
      synchronized (store)
      {
         T entry = store.get(key);
         
         if(entry == null)
            throw new IllegalArgumentException("entry " + key + " not found in cache " + this);
         
         if(entry.isInUse())
         {
            throw new IllegalStateException("entry " + entry + " is in use");
         }
         
         passivationManager.preReplicate(entry);
         
         store.update(entry);
      }
   }
   
   public T create(Class<?>[] initTypes, Object[] initValues)
   {
      T obj = factory.create(initTypes, initValues);
      obj.setInUse(true);
      synchronized (store)
      {
         store.insert(obj);
      }
      return obj;
   }

   public T get(Object key) throws NoSuchEJBException
   {
      synchronized (store)
      {
         T entry = store.get(key);
         if(entry == null)
            throw new NoSuchEJBException(String.valueOf(key));
         
         if (isClustered())
         {
            passivationManager.postReplicate(entry);
         }
         
         passivationManager.postActivate(entry);
         
//         if(entry.getCacheState() != Cacheable.State.READY)
//         {
//            throw new IllegalStateException("entry " + key + " is not ready");
//         }
         entry.setInUse(true);
         return entry;
      }
   }

   public void passivate(Object key)
   {
      log.trace("passivate " + key);
      synchronized (store)
      {
         T entry = store.get(key);
         
         if(entry == null)
            throw new IllegalArgumentException("entry " + key + " not found in cache " + this);
         
         if(entry.isInUse())
         {
            throw new IllegalStateException("entry " + entry + " is in use");
         }

         passivationManager.prePassivate(entry);
         
         store.passivate(entry);
      }
   }
   
   public T peek(Object key) throws NoSuchEJBException
   {
      synchronized (store)
      {
         T entry = store.get(key);
         if(entry == null)
            throw new NoSuchEJBException(String.valueOf(key));
         
         // TODO why call these in peek?  We should *always* call
         // them in get() and let the PassivationManager sort out
         // whether they really need to be called
         
//         if (isClustered())
//         {
//            passivationManager.postReplicate(entry);
//         }
//         
//         passivationManager.postActivate(entry);
         
         return entry;
      }
   }

   public void release(T obj)
   {
      releaseByKey(obj.getId());
   }

   protected T releaseByKey(Object key)
   {
      synchronized (store)
      {
         T entry = store.get(key);
         if(entry == null)
            throw new IllegalStateException("object " + key + " not from this cache");
         if(!entry.isInUse())
            throw new IllegalStateException("entry " + entry + " is not in use");
         entry.setInUse(false);
         store.update(entry);
         return entry;
      }
   }
   
   public void remove(Object key)
   {
      T entry;
      synchronized (store)
      {
         entry = store.remove(key);
         if(entry.isInUse())
            throw new IllegalStateException("entry " + entry + " is in use");
      }
      if(entry != null)
         factory.destroy(entry);
   }
   
   public void start()
   {
      store.start();
   }

   public void stop()
   {
      store.stop();
   }

}
