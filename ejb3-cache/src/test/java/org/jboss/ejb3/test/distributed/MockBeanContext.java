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
package org.jboss.ejb3.test.distributed;

import java.io.Serializable;

import org.jboss.ejb3.cache.Cacheable;

/**
 * Comment
 *
 * @author Brian Stansberry
 * @version $Revision: 65339 $
 */
public class MockBeanContext implements Cacheable, Serializable
{
   private static final long serialVersionUID = 1L;

   private static volatile long currentId = 100000;
   
   private long id;
   
   private boolean inUse;
   private long lastUsed;
   
   public Object shared;
   
   public MockBeanContext()
   {
      this.id = ++currentId;
   }
   
   public Object getId()
   {
      return id;
   }

   public boolean isInUse()
   {
      return inUse;
   }

   public void setInUse(boolean inUse)
   {
      this.inUse = inUse;
      lastUsed = System.currentTimeMillis();
   }

   public long getLastUsed()
   {
      return lastUsed;
   }

   @Override
   public String toString()
   {
      return super.toString() + "{id=" + id + "}";
   }

}
