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
package org.jboss.ejb3.interceptor;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.aop.util.PayloadKey;

/**
 * Utility class for placing the client interceptor data under a given metadata tag
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
public class ClientInterceptorUtil
{
   public static final String CLIENT_METADATA = "jboss.ejb3.client.invocation.metadata";
   
   public static void addMetadata(Invocation invocation, Object key, Object value, PayloadKey payload)
   {
      invocation.getMetaData().addMetaData(CLIENT_METADATA, key, value, payload);
   }
   
   public static void addMetadata(Invocation invocation, Object key, Object value)
   {
      invocation.getMetaData().addMetaData(CLIENT_METADATA, key, value);
   }
   
   public static Object getMetadata(Invocation invocation, Object key)
   {
      return invocation.getMetaData().getMetaData(CLIENT_METADATA, key);
   }
   
   static Map getClientMetadataMap(Invocation invocation)
   {
      Map map =  invocation.getMetaData().tag(CLIENT_METADATA);
      if (map != null)
      {
         return new ClientValueMap(map);
      }
      return null;
   }

   /**
    * Wraps map containing data set by client interceptorss and lazily unmarshals it 
    */
         
   private static class ClientValueMap implements Map
   {
      Map marshalledMap;
      boolean haveUnmarshalledAllEntries;

      private ClientValueMap(Map marshalledMap)
      {
         this.marshalledMap = marshalledMap;
      }
      
      public void clear()
      {
         marshalledMap.clear();
      }

      public boolean containsKey(Object key)
      {
         return marshalledMap.containsKey(key);
      }

      public boolean containsValue(Object value)
      {
         unmarshallAllEntries();
         return marshalledMap.containsValue(value);
      }

      public Set entrySet()
      {
         unmarshallAllEntries();
         return marshalledMap.entrySet();
      }

      public boolean equals(Object o)
      {
         return marshalledMap.equals(o);
      }

      public Object get(Object key)
      {
         return unmarshallEntry(key);
      }

      public int hashCode()
      {
         return marshalledMap.hashCode();
      }

      public boolean isEmpty()
      {
         return marshalledMap.isEmpty();
      }

      public Set keySet()
      {
         return marshalledMap.keySet();
      }

      public Object put(Object key, Object value)
      {
         return marshalledMap.put(key, value);
      }

      public void putAll(Map t)
      {
         marshalledMap.putAll(t);
      }

      public Object remove(Object key)
      {
         return marshalledMap.remove(key);
      }

      public int size()
      {
         return marshalledMap.size();
      }

      public Collection values()
      {
         unmarshallAllEntries();
         return marshalledMap.values();
      }
      
      private void unmarshallAllEntries()
      {
         if (haveUnmarshalledAllEntries)
         {
            return;
         }
         
         Iterator keys = marshalledMap.keySet().iterator();
         while (keys.hasNext())
         {
            //Make sure that each entry gets unmarshalled
            unmarshallEntry(keys.next());
         }
         haveUnmarshalledAllEntries = true;
      }
      
      private Object unmarshallEntry(Object key)
      {
         try
         {
            Object obj = marshalledMap.get(key);
            if (obj instanceof SimpleMetaData.MetaDataValue)
            {
               Object realObj = ((SimpleMetaData.MetaDataValue)obj).get(); 
               marshalledMap.put(key, realObj);
               return realObj;
            }
            return obj;
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException(e);
         }
      }
   }
}
