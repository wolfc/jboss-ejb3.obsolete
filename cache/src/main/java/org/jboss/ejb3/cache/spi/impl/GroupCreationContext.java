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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.spi.SerializationGroup;


/**
 * Stores contextual information about a set of {@link CacheItem}s that are 
 * being created as members of a {@link SerializationGroup}. Implementation is 
 * based on a <code>ThreadLocal</code>.
 * 
 * @author Brian Stansberry
 */
public class GroupCreationContext
{
   private static final ThreadLocal<GroupCreationContext> groupCreationContext = new ThreadLocal<GroupCreationContext>();
   
   private final List<ItemCachePair> pairs;
   private Map<Object, Object> sharedState;
   private final boolean strict;

   /**
    * Gets the GroupCreationContext associated with the thread.
    * 
    * @return the context. May return <code>null</code>.
    */
   public static GroupCreationContext getGroupCreationContext()
   {
      return groupCreationContext.get();
   }
   
   /**
    * Create a new GroupCreationContext associated with the thread.
    * 
    * @param strict <code>true</code> if other caches associated with the context
    *               should strictly check compatibility with a 
    *               {@link SerializationGroup}, <code>false</code> if not.
    *               
    * @return the context. Will not return <code>null</code>.
    * 
    * @throws IllegalStateException if a context is already bound to the thread
    */
   public static GroupCreationContext startGroupCreationContext(boolean strict)
   {
      if (groupCreationContext.get() != null)
         throw new IllegalStateException("GroupCreationContext already exists");
      GroupCreationContext started = new GroupCreationContext(strict);
      groupCreationContext.set(started);
      return started;
   }
   
   /**
    * Clears the association of any GroupCreationContext with the current thread.
    *
    */
   public static void clearGroupCreationContext()
   {
      groupCreationContext.set(null);
   }
   
   /**
    * Prevent external instantiation. 
    */
   private GroupCreationContext(boolean strict) 
   {
      this.pairs = new ArrayList<ItemCachePair>();
      this.strict = strict;
   }

   /**
    * Gets the list of cache items and associated caches that currently
    * comprise the group.
    * 
    * @return a list of cache items and associated caches.  
    *         Will not return <code>null</code>.
    */
   public List<ItemCachePair> getPairs()
   {
      return pairs;
   }

   /**
    * Gets any shared state map that should be passed to
    * {@link StatefulObjectFactory#create(Class[], Object[], Map)} for
    * each member of the group.
    * 
    * @return the shared state map.  May be <code>null</code>
    */
   public Map<Object, Object> getSharedState()
   {
      return sharedState;
   }  
   
   /**
    * Sets any shared state map that should be passed to
    * {@link StatefulObjectFactory#create(Class[], Object[], Map)} for
    * each member of the group.
    * 
    * @param sharedState the shared state map.  Cannot be <code>null</code>
    * 
    * @throws IllegalStateException if shared state has already been set
    */
   public void setSharedState(Map<Object, Object> sharedState)
   {
      assert sharedState != null : "sharedState is null";
      
      if (this.sharedState != null)
         throw new IllegalStateException("Shared state already set");
      this.sharedState = sharedState;
   }
   
   /**
    * Gets whether the cache that initialized creation of the context has
    * specified that all other caches must strictly check whether they
    * are compatible with the group.
    */
   public boolean isStrict()
   {
      return strict;
   }
}
