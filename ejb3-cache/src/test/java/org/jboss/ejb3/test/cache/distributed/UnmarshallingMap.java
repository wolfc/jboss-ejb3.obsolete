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

package org.jboss.ejb3.test.cache.distributed;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Brian Stansberry
 */
public class UnmarshallingMap extends HashMap<Object, Object>
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5305256519983822837L;

   /**
    * Create a new UnmarshallingMap.
    * 
    */
   public UnmarshallingMap()
   {
      super();
   }

   /**
    * Create a new UnmarshallingMap.
    * 
    * @param initialCapacity
    */
   public UnmarshallingMap(int initialCapacity)
   {
      super(initialCapacity);
   }

   /**
    * Create a new UnmarshallingMap.
    * 
    * @param m
    */
   public UnmarshallingMap(Map<Object, Object> m)
   {
      super(m);
   }

   /**
    * Create a new UnmarshallingMap.
    * 
    * @param initialCapacity
    * @param loadFactor
    */
   public UnmarshallingMap(int initialCapacity, float loadFactor)
   {
      super(initialCapacity, loadFactor);
   }
   
   public Object unmarshall(Object key)
   {
      Object value = get(key);
      if (value instanceof byte[])
      {      
         ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) value);
         try
         {
            ObjectInputStream ois = new ObjectInputStream(bais);
            value = ois.readObject();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      
      return value;
   }

}
