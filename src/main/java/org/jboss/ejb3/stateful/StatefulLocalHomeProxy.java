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
package org.jboss.ejb3.stateful;

import java.lang.reflect.Method;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.LocalProxy;
import org.jboss.ejb3.session.SessionContainer;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulLocalHomeProxy extends LocalProxy
{
   private static final long serialVersionUID = -9026021347498876589L;

   public StatefulLocalHomeProxy()
   {
      super();
   }

   public StatefulLocalHomeProxy(Container container)
   {
      super(container);
   }

   public Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable
   {
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
