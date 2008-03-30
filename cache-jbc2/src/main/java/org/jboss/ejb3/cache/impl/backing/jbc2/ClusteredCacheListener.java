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

package org.jboss.ejb3.cache.impl.backing.jbc2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.cache.Fqn;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeActivated;
import org.jboss.cache.notifications.annotation.NodeModified;
import org.jboss.cache.notifications.annotation.NodePassivated;
import org.jboss.cache.notifications.annotation.NodeRemoved;
import org.jboss.cache.notifications.annotation.NodeVisited;
import org.jboss.cache.notifications.event.NodeActivatedEvent;
import org.jboss.cache.notifications.event.NodeEvent;
import org.jboss.cache.notifications.event.NodeModifiedEvent;
import org.jboss.cache.notifications.event.NodePassivatedEvent;
import org.jboss.cache.notifications.event.NodeRemovedEvent;
import org.jboss.cache.notifications.event.NodeVisitedEvent;
import org.jboss.logging.Logger;

/**
 * Single {@link CacheListener} that handles event Fqn parsing and then
 * delegates to a handler that has registered for the region.  Intent is
 * to do the region-matching work once here, rather than having multiple 
 * listeners registered with the cache.
 *  
 * @author Brian Stansberry
 */
@CacheListener
public class ClusteredCacheListener
{   
   private static final Logger log = Logger.getLogger(ClusteredCacheListener.class);

   public static interface RegionHandler
   {
      void nodeVisited(OwnedItem ownedItem, NodeVisitedEvent event);
      void nodeModified(OwnedItem ownedItem, NodeModifiedEvent event);
      void nodeRemoved(OwnedItem ownedItem, NodeRemovedEvent event);
      void nodeActivated(OwnedItem ownedItem, NodeActivatedEvent event);
      void nodePassivated(OwnedItem ownedItem, NodePassivatedEvent event);
   }
   
   private final Map<Fqn<Object>, RegionHandler> handlers = new ConcurrentHashMap<Fqn<Object>, RegionHandler>();
   private final boolean checkBuddy;
   
   public ClusteredCacheListener(boolean checkBuddy)
   {
      this.checkBuddy= checkBuddy;
   }
   
   public void addRegionHandler(Fqn<Object> region, RegionHandler handler)
   {
      handlers.put(region, handler);
   }
   
   public boolean removeRegionHandler(Fqn<Object> region)
   {
      handlers.remove(region);
      return handlers.size() > 0;
   }
   
   private RegionHandler getRegionHandler(OwnedItem oi)
   {
      return (oi == null ? null : handlers.get(oi.getRegion()));
   }
   
   @NodeVisited
   public void nodeVisited(NodeVisitedEvent event)
   {
      if (event.isPre())
         return;
      
      OwnedItem oi = OwnedItem.getOwnedItem(event.getFqn(), checkBuddy, event.isOriginLocal());
      RegionHandler handler = getRegionHandler(oi);
      if (handler != null)
      {
         handler.nodeVisited(oi, event);
      }
   }
   
   @NodeModified
   public void nodeModified(NodeModifiedEvent event)
   {
      if (event.isPre() || NodeModifiedEvent.ModificationType.REMOVE_DATA == event.getModificationType())
         return;
      
      OwnedItem oi = OwnedItem.getOwnedItem(event.getFqn(), checkBuddy, event.isOriginLocal());
      logEvent(oi, event);
      RegionHandler handler = getRegionHandler(oi);
      if (handler != null)
      {
         handler.nodeModified(oi, event);
      }
   }
   
   @NodeRemoved
   public void nodeRemoved(NodeRemovedEvent event)
   {
      // Here we only want pre as that's what lets us get the removed data
      if (!event.isPre())
         return; 
      
      OwnedItem oi = OwnedItem.getOwnedItem(event.getFqn(), checkBuddy, event.isOriginLocal());
      logEvent(oi, event);
      RegionHandler handler = getRegionHandler(oi);
      if (handler != null)
      {
         handler.nodeRemoved(oi, event);
      }
   }
   
   @NodeActivated
   public void nodeActivated(NodeActivatedEvent event)
   {
      if(event.isPre()) return; 
      
      OwnedItem oi = OwnedItem.getOwnedItem(event.getFqn(), checkBuddy, event.isOriginLocal());
      logEvent(oi, event);
      RegionHandler handler = getRegionHandler(oi);
      if (handler != null)
      {
         handler.nodeActivated(oi, event);
      }
   }

   @NodePassivated
   public void nodePassivated(NodePassivatedEvent event)
   {
      if(event.isPre()) return;
      
      OwnedItem oi = OwnedItem.getOwnedItem(event.getFqn(), checkBuddy, event.isOriginLocal());
      logEvent(oi, event);
      RegionHandler handler = getRegionHandler(oi);
      if (handler != null)
      {
         handler.nodePassivated(oi, event);
      }
   }
   
   private void logEvent(OwnedItem oi, NodeEvent event)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Event for " + oi + " -- " + event);
      }
   }
}
