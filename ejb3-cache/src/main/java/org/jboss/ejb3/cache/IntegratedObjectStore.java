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

package org.jboss.ejb3.cache;

/**
 * A in-memory store for identifiable objects that integrates a persistent store. 
 * Note that this class does NOT call any callbacks.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public interface IntegratedObjectStore<T extends Cacheable>
{
   /**
    * Put a new entry into the store.
    * 
    * @param entry the object to store. Cannot be <code>null</code>.
    */
   void insert(T entry);
   
   /**
    * Gets the entry with the given id from the store.
    * 
    * @param key {@link Identifiable#getId() id} of the entry.
    *           Cannot be <code>null</code>.
    * @return the object store under <code>id</code>. May return <code>null</code>
    */
   T get(Object key);
   
   /**
    * Replicate an already cached item. Only valid for 
    * {@link #isClustered() clustered} stores, as the purpose of this
    * method is to advise the store that the state of it's locally cached copy 
    * of an entry has changed and that any other caches in the cluster should
    * be made aware of the new state.
    * 
    * @param  entry the entry to replicate
    *           
    * @throws UnsupportedOperationException if {@link #isClustered()} returns
    *                                       <code>false</code>
    */
   void replicate(T entry);
   
   /**
    * Remove the object with the given key from the store.
    * 
    * @param key {@link Identifiable#getId() id} of the entry.
    *           Cannot be <code>null</code>.
    *           
    * @return the object that was cached under <code>key</code>
    */
   T remove(Object key);
   
   /**
    * Remove the entry with the given key from any in-memory store
    * while retaining it in the persistent store.
    * 
    * @param entry the entry to passivate
    */
   void passivate(T entry);
   
   /**
    * Gets whether this store supports clustering functionality.
    * 
    * @return <code>true</code> if clustering is supported, <code>false</code>
    *         otherwise
    */
   boolean isClustered();
   
   /**
    * Perform any initialization work.
    */
   void start();
   
   /**
    * Perform any shutdown work.
    */
   void stop();
}
