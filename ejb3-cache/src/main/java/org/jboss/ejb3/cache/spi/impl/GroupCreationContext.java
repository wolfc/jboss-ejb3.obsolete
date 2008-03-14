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


/**
 * @author Brian Stansberry
 *
 */
public class GroupCreationContext
{
   @SuppressWarnings("unchecked")
   private static final ThreadLocal<GroupCreationContext> groupCreationContext = new ThreadLocal<GroupCreationContext>();
   
   private final List<ItemCachePair> pairs;
   private Map<Object, Object> sharedState;
   private final boolean strict;
   
   /**
    * Prevent external instantiation. 
    */
   private GroupCreationContext(boolean strict) 
   {
      this.pairs = new ArrayList<ItemCachePair>();
      this.strict = strict;
   }

   public List<ItemCachePair> getPairs()
   {
      return pairs;
   }

   public Map<Object, Object> getSharedState()
   {
      return sharedState;
   }  
   
   public void setSharedState(Map<Object, Object> sharedState)
   {
      if (this.sharedState != null)
         throw new IllegalStateException("Shared state already set");
      this.sharedState = sharedState;
   }
   
   public boolean isStrict()
   {
      return strict;
   }

   public static GroupCreationContext getGroupCreationContext()
   {
      return groupCreationContext.get();
   }
   
   public static GroupCreationContext startGroupCreationContext(boolean strict)
   {
      if (groupCreationContext.get() != null)
         throw new IllegalStateException("GroupCreationContext already exists");
      GroupCreationContext started = new GroupCreationContext(strict);
      groupCreationContext.set(started);
      return started;
   }
   
   public static void clearGroupCreationContext()
   {
      groupCreationContext.set(null);
   }
}
