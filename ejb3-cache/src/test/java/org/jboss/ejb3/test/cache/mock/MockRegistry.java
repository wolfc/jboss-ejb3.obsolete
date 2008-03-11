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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Brian Stansberry
 */
public class MockRegistry
{
   private static final Map<Object, Object> registry = new ConcurrentHashMap<Object, Object>();
   
   public static Object put(Object key, Object value)
   {
      return registry.put(new RegistryKey(key), value);
   }
   
   public static Object get(Object key)
   {
      return registry.get(new RegistryKey(key));
   }
   
   public static Object remove(Object key)
   {
      return registry.remove(new RegistryKey(key));
   }
   
   public static void clear()
   {
      registry.clear();
   }
   
   private static class RegistryKey
   {
      private ClassLoader cl;
      private Object key;
      
      RegistryKey(Object key)
      {
         this.key = key;
         this.cl = Thread.currentThread().getContextClassLoader();
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         
         if (obj instanceof RegistryKey)
         {
            RegistryKey other = (RegistryKey) obj;
            return (cl.equals(other.cl) && key.equals(other.key));
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         int result = 19;
         result = 51 * result + cl.hashCode();
         result = 51 * result + key.hashCode();
         return result;
      }
      
      
   }
}
