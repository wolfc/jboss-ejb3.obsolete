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

import org.jboss.ejb3.cache.StatefulObjectFactory;


/**
 * @author Brian Stansberry
 */
public class MockStatefulObjectFactory 
   implements StatefulObjectFactory<MockBeanContext>
{
   private final MockBeanContainer container;
   
   private int creationCount;
   private int groupCreationCount;
   private int destroyCount;
   
   public MockStatefulObjectFactory(MockBeanContainer container)
   {
      this.container = container;
   }
   
   public MockBeanContext create(Class[] initTypes, Object[] initValues)
   {
      MockBeanContext ctx = new MockBeanContext(container.getName());
      ctx.setXPC(container.getXPC());
      
      // Here we mock creating nested beans
      for (MockBeanContainer childContainer : container.getChildren())
      {
         ctx.addChild(childContainer.getCache().create(null, null));
      }
      
      creationCount++;
      return ctx;
   }

   public void destroy(MockBeanContext ctx)
   {
      ctx.remove();
      destroyCount++;
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
