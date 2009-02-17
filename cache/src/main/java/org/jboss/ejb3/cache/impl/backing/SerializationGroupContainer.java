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

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.logging.Logger;

/**
 * Functions as both a StatefulObjectFactory and PassivationManager for
 * {@link SerializationGroup}s.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * @version $Revision: $
 */
public class SerializationGroupContainer<T extends CacheItem> 
   implements StatefulObjectFactory<SerializationGroup<T>>, PassivationManager<SerializationGroup<T>>
{
   private static final Logger log = Logger.getLogger(SerializationGroupContainer.class);
   
   private PassivatingBackingCache<T, SerializationGroup<T>> groupCache;
   
   private boolean clustered;
   
   public boolean isClustered()
   {
      return clustered;
   }
   
   public void setClustered(boolean clustered)
   {
      this.clustered = clustered;
   }

   public SerializationGroup<T> create(Class<?>[] initTypes, Object[] initValues, Map<Object, Object> sharedState)
   {
      SerializationGroupImpl<T> group = new SerializationGroupImpl<T>();
      group.setClustered(clustered);
      group.setGroupCache(groupCache);
      return group;
   }

   public void destroy(SerializationGroup<T> group)
   {
      // TODO: nothing?
   }

   public void postActivate(SerializationGroup<T> group)
   {
      log.trace("post activate " + group);
      // Restore ref to the groupCache in case it was lost during serialization
      group.setGroupCache(groupCache);
      group.postActivate();
   }

   public void prePassivate(SerializationGroup<T> group)
   {
      log.trace("pre passivate " + group);
      group.prePassivate();
   }

   public void postReplicate(SerializationGroup<T> group)
   {
      log.trace("post replicate " + group);
      // Restore ref to the groupCache in case it was lost during serialization
      group.setGroupCache(groupCache);
      group.postReplicate();
   }

   public void preReplicate(SerializationGroup<T> group)
   {
      log.trace("pre replicate " + group);
      group.preReplicate();
   }

   public PassivatingBackingCache<T, SerializationGroup<T>> getGroupCache()
   {
      return groupCache;
   }

   public void setGroupCache(PassivatingBackingCache<T, SerializationGroup<T>> groupCache)
   {
      this.groupCache = groupCache;
   }
   
}
