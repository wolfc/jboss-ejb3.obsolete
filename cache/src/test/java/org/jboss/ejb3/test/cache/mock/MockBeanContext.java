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

package org.jboss.ejb3.test.cache.mock;

import java.util.HashMap;
import java.util.Map;

import org.jboss.ejb3.cache.CacheItem;

/**
 * @author Brian Stansberry
 *
 */
public class MockBeanContext 
   extends MockIdentifiable
   implements CacheItem
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 3209950231614290498L;

   private final String containerName;
   
   private MockXPC xpc;
   private MockEntity entity;
   
   private boolean modified;
   private int preReplicateCount;
   private int prePassivateCount;
   private int postReplicateCount;
   private int postActivateCount;
   private Map<String, Object> children;
   private final Map<Object, Object> sharedState;
   private int count;
   
   public MockBeanContext(String containerName, Map<Object, Object> sharedState)
   {
      super(createId());
      this.containerName = containerName;
      this.children = new HashMap<String, Object>();
      this.sharedState = sharedState;
   }
   
   public boolean isModified()
   {
      boolean result = modified;
      modified = false;
      return result;
   }

   public void setModified(boolean modified)
   {
      this.modified = modified;
   }   
   
   public MockBeanContainer getContainer()
   {
      return (MockBeanContainer) MockRegistry.get(containerName);
   }
   
   public void addChild(String childContainerName, Object childId)
   {
      // Just store the id; tests get the id from getChild() and
      // then call get(id) on the child container's cache.
      // This simulates how an internal call via a nested bean's 
      // proxy would work
      children.put(childContainerName, childId);
   }
   
   public Object getChild(String containerName)
   {
      return children.get(containerName);
   }
   
   public void remove()
   {
      if (xpc != null)
      {
         SharedXPC shared = (SharedXPC) sharedState.get(xpc.getName());
         if (shared.removeSharedUser() == 0 && !xpc.isClosed())
            xpc.close();
      }
   }
   
   public int getPreReplicateCount()
   {
      return preReplicateCount;
   }
   public int getPrePassivateCount()
   {
      return prePassivateCount;
   }
   public int getPostReplicateCount()
   {
      return postReplicateCount;
   }
   public int getPostActivateCount()
   {
      return postActivateCount;
   }
   
   public MockXPC getExtendedPersistenceContext(String id)
   {      
      SharedXPC shared = (SharedXPC) sharedState.get(id);
      return (shared == null ? null : shared.getXPC());
   }

   public void addExtendedPersistenceContext(String id, MockXPC pc)
   {
      if (xpc != null)
         throw new IllegalStateException("XPC already configured");
      
      SharedXPC shared = (SharedXPC) sharedState.get(id);
      if (shared == null)
      {
         shared = new SharedXPC(pc);
         sharedState.put(id, shared);
      }
      
      shared.addSharedUser();
      xpc = pc;
   }
   
   public XPC getXPC()
   {
      return xpc;
   }
   
   public void preReplicate()
   {
      preReplicateCount++;
      synchronized(this)
      {
         notifyAll();
      }
   }
   
   public void postReplicate()
   {
      postReplicateCount++;
      synchronized(this)
      {
         notifyAll();
      }
   }
   
   public void prePassivate()
   {
      prePassivateCount++;
      synchronized(this)
      {
         notifyAll();
      }
   }
   
   public void postActivate()
   {
      postActivateCount++;
      synchronized(this)
      {
         notifyAll();
      }
   }
   
   // -- Underlying bean operations
   
   public MockEntity createEntity()
   {
      entity = xpc.createEntity();
      setModified(true);
      log.trace(getId() + ": createEntity()");
      return entity;
   }
   
   public MockEntity getEntity()
   {
      log.trace(getId() + ": getEntity()");
      MockEntity was = entity;
      entity = xpc.getEntity();
      if (was != entity)
         setModified(true);
      return entity;
   }
   
   public void removeEntity()
   {
      log.trace(getId() + ": removeEntity()");
      xpc.removeEntity();
      if (entity != null)
      {
         entity = null;
         setModified(true);
      }
   }
   
   public void invokeNonModifying()
   {
      log.trace(getId() + ": invokeNonModifying()");
   }
   
   public void invokeModifying()
   {
      log.trace(getId() + ": invokeModifying()");
      setModified(true);
   }
   
   public int getCount()
   {
      return count;
   }
   
   public int increment()
   {
      count++;
      setModified(true);
      return count;
   }
}
