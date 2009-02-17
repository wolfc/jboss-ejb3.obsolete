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
package org.jboss.ejb3.cache;

import javax.ejb.NoSuchEJBException;


/**
 * Cache a stateful object and make sure any life cycle callbacks are
 * called at the appropriate time.
 * <p>
 * A cache is linked to a {@link StatefulObjectFactory} and a {@link PassivationManager}. 
 * How the link is established is left beyond scope.
 * </p>
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface Cache<T extends CacheItem>
{
   /**
    * Creates and caches a new instance of <code>T</code>.
    * 
    * @param initTypes   the types of any <code>initValues</code>. 
    *                    May be <code>null</code>.
    * @param initValues  any paramaters to pass to <code>T</code>'s constructor.
    *                    May be null, in which case a default constructor will
    *                    be used.
    * @return the new <code>T</code>'s {@link Identifiable#getId() id}. 
    */
   Object create(Class<?> initTypes[], Object initValues[]);

   /**
    * Get the specified object from cache. This will mark
    * the object as being in use.
    * 
    * @param key    the identifier of the object
    * @return       the object
    * @throws NoSuchEJBException    if the object does not exist
    * @throws IllegalStateException if the object is already in use by another
    *                               transaction or if {@link #release(CacheItem)}
    *                               has not been invoked since the last time
    *                               the object was gotten.
    */
   T get(Object key) throws NoSuchEJBException;
   
   /**
    * Signal the finish of the current operation on the object.
    * If the object was {@link #get(Object) gotten from the cache} in the 
    * course of an ongoing transaction, the object will still be regarded as in 
    * use, but <code>get()</code> can safely be invoked again by that same 
    * transaction. If there was no transaction in effect when the object was 
    * gotten from the cache, invoking this method marks the object as no
    * longer being in use.
    * 
    * @param obj object previously gotten via {@link #get(Object)}
    */
   void release(T obj);

   /**
    * Remove the specified object from cache.
    * 
    * @param key    the identifier of the object
    */
   void remove(Object key);

   /**
    * Start the cache.
    */
   void start();

   /**
    * Stop the cache.
    */
   void stop();
   
   /**
    * Whether the cache is in the started state.
    * 
    * @return <code>true</code> if started, <code>false</code> otherwise
    * @return
    */
   boolean isStarted();
   
   int getAvailableCount();
   
   int getCacheSize();
   
   int getCreateCount();
   
   int getCurrentSize();
   
   int getMaxSize();
   
   int getPassivatedCount();
   
   int getRemoveCount();
   
   int getTotalSize();
   
}
