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

import java.util.Map;

import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.logging.Logger;


/**
 * @author Brian Stansberry
 */
public class MockStatefulObjectFactory 
   implements StatefulObjectFactory<MockBeanContext>
{
   private static final Logger log = Logger.getLogger(MockStatefulObjectFactory.class);   
   
   private final MockBeanContainer container;
   
   private int creationCount;
   private int groupCreationCount;
   private int destroyCount;
   
   public MockStatefulObjectFactory(MockBeanContainer container)
   {
      this.container = container;
   }
   
   public MockBeanContext create(Class<?>[] initTypes, Object[] initValues, Map<Object, Object> sharedState)
   {
      MockBeanContext ctx = new MockBeanContext(container.getName(), sharedState);
      if (container.getXPCName() != null)
      {
         MockXPC xpc = ctx.getExtendedPersistenceContext(container.getXPCName());
         if (xpc == null)
         {
            xpc = (MockXPC) MockRegistry.get(container.getXPCName());
         }
         if (xpc == null)
         {
            xpc = new MockXPC(container.getXPCName());
         }
         ctx.addExtendedPersistenceContext(container.getXPCName(), xpc);
      }      
      
      // Here we mock creating nested beans
      for (MockBeanContainer childContainer : container.getChildren())
      {
         ctx.addChild(childContainer.getName(), childContainer.getCache().create(null, null));
      }
      
      creationCount++;
      
      log.trace("Created context " + ctx.getId() + " for " + container.getName());
      
      return ctx;
   }

   public void destroy(MockBeanContext ctx)
   {
      Object id = ctx.getId();
      ctx.remove();      
      destroyCount++;
      log.trace("Destroyed context " + id + " for " + container.getName());
   }

   public MockBeanContainer getContainer()
   {
      return container;
   }

   public int getCreationCount()
   {
      return creationCount;
   }

   public int getGroupCreationCount()
   {
      return groupCreationCount;
   }

   public int getDestroyCount()
   {
      return destroyCount;
   }
   
}
