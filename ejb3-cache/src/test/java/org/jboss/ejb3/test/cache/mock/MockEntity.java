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
 * A mock entity.  We subclass MockIdentifiable just to pick up its code.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class MockEntity implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private int id;
   
   public MockEntity(int id)
   {
      this.id = id;
   }
   
   public Object getId()
   {
      return id;
   }   

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj instanceof MockEntity)
      {
         MockEntity other = (MockEntity) obj;
         return this.id == other.id;
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      return id;
   }

   @Override
   public String toString()
   {
      return super.toString() + "{id=" + id + "}";
   }
}
