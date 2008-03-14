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

package org.jboss.ejb3.test.cache.mock;

import java.io.Serializable;

/**
 * A mock extended PersistenceContext.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class MockXPC implements Serializable, XPC
{
   public static final String DEFAULT_XPC_NAME = "xpc";

   private static final long serialVersionUID = 1L;
   
   private boolean closed = false;
   private MockEntity entity;
   private final String name;

   public MockXPC()
   {
      this(DEFAULT_XPC_NAME);
   }
   
   public MockXPC(String name)
   {
      this.name = name;
      MockRegistry.put(name, this);
   }
   
   public MockEntity createEntity()
   {
      if (entity != null)
         throw new IllegalStateException("entity already created");
      entity = new MockEntity();
      return entity;
   }
   
   public MockEntity getEntity()
   {
      return entity;
   }
   
   public void removeEntity()
   {
      if (entity == null)
         throw new IllegalStateException("no entity to remove");
      entity = null;
   }
   
   public boolean isClosed()
   {
      return closed;
   }
   
   public void close()
   {
      closed = true;
      entity = null;
   }

   public String getName()
   {
      return name;
   }
   
   
}
