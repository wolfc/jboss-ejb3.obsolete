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
package org.jboss.ejb3.proxy.handler.stateful;

import java.lang.reflect.Method;

import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.LocalProxyInvocationHandler;
import org.jboss.ejb3.proxy.ProxyUtils;
import org.jboss.ejb3.session.SessionContainer;

/**
 * StatefulLocalHomeProxyInvocationHandler
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 68194 $
 */
public class StatefulLocalHomeProxyInvocationHandler extends LocalProxyInvocationHandler
{
   private static final long serialVersionUID = -9026021347498876589L;

   public StatefulLocalHomeProxyInvocationHandler()
   {
      super();
   }

   public StatefulLocalHomeProxyInvocationHandler(Container container)
   {
      super(container, null);
   }

   public Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable
   {
      // Attempt to handle locally
      long hash = MethodHashing.calculateHash(method);
      Object ret = ProxyUtils.handleCallLocally(hash, proxy, this, method, args);
      if (ret != null)
      {
         // Was handled locally, return
         return ret;
      }
      
      // Invoke upon container
      SessionContainer sfsb = (SessionContainer) getContainer();
      return sfsb.localHomeInvoke(method, args);
   }

   public Object getAsynchronousProxy(Object proxy)
   {
      throw new RuntimeException("NOT AVAILABLE FOR HOME PROXIES");
   }

   public String toString()
   {
      return proxyName + ": Home Proxy";
   }

}
