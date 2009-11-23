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
package org.jboss.ejb3.core.proxy.spi;

/**
 * Allows you to specify a ProxyFactory to use for creating the remote proxies.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class CurrentRemoteProxyFactory
{
   private static ThreadLocal<RemoteProxyFactory> current = new ThreadLocal<RemoteProxyFactory>();
   
   public static RemoteProxyFactory get()
   {
      return current.get();
   }
   
   public static <T extends RemoteProxyFactory> T get(Class<T> type)
   {
      return type.cast(get());
   }
   
   public static boolean isSet()
   {
      return current.get() != null;
   }
   
   public static void remove()
   {
      if(!isSet())
         throw new IllegalStateException("There is no current remote proxy set");
      current.remove();
   }
   
   public static void set(RemoteProxyFactory factory)
   {
      // Normally we would only communicate via one channel, so any recursive call
      // would still mean the same channel.
      RemoteProxyFactory previous = current.get();
      if(previous != null)
         throw new IllegalStateException("Already have a current remote proxy factory " + previous);
      
      current.set(factory);
   }
}
