/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.remoting.endpoint.client;

import java.util.Map;

/**
 * Allows for setting and resetting of the remote context data.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RemoteContextData
{
   private static ThreadLocal<Map<String, Object>> contextData = new ThreadLocal<Map<String, Object>>();
   
   public static void cleanContextData()
   {
      Map<String, Object> current = contextData.get();
      if(current == null)
         throw new IllegalStateException("no context data found");
      contextData.remove();
   }
   
   public static Map<String, Object> getContextData()
   {
      Map<String, Object> current = contextData.get();
      if(current == null)
         throw new IllegalStateException("no context data found");
      return current;
   }
   
   public static void setContextData(Map<String, Object> value)
   {
      Map<String, Object> previous = contextData.get();
      if(previous != null)
         throw new IllegalStateException("found previous context data " + previous);
      contextData.set(value);
   }
}
