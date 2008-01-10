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
package org.jboss.ejb3.stateless;

import javax.ejb.EJBException;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.jboss.aspects.asynch.AsynchMixin;
import org.jboss.aspects.asynch.AsynchProvider;
import org.jboss.aspects.asynch.FutureHolder;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.LocalProxy;
import org.jboss.ejb3.ProxyUtils;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessLocalProxy extends LocalProxy
{
   private static final long serialVersionUID = -3241008127518089831L;
   
   private static final Logger log = Logger.getLogger(StatelessLocalProxy.class);
   
   AsynchProvider provider;

   public StatelessLocalProxy()
   {
   }

   public StatelessLocalProxy(Container container)
   {
      super(container);
   }

   public StatelessLocalProxy(AsynchProvider provider, Container container)
   {
      super(container);
      this.provider = provider;
   }

   public Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable
   {
      if (method.getDeclaringClass() == AsynchProvider.class)
      {
         return provider.getFuture();
      }

      Object ret = ProxyUtils.handleCallLocally(proxy, this, method, args);
      if (ret != null)
      {
         return ret;
      }
      
      StatelessContainer container = (StatelessContainer) getContainer();
       
      if (container == null)
      {
         throw new EJBException("Invalid invocation of local interface (null container)");
      }
      
      return container.localInvoke(method, args, (FutureHolder) provider);
   }

   public Object getAsynchronousProxy(Object proxy)
   {
      Class[] infs = proxy.getClass().getInterfaces();
      if (!ProxyUtils.isAsynchronous(infs))
      {
         Class[] interfaces = ProxyUtils.addAsynchProviderInterface(infs);
         AsynchMixin mixin = new AsynchMixin();
         StatelessLocalProxy handler = new StatelessLocalProxy(mixin, getContainer());
         return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, handler);
      }

      //I was already asynchronous
      return proxy;
   }
/*
   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      provider = (AsynchProvider)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeObject(provider);
   }
*/

   public String toString()
   {
      if (getContainer() == null)
         return proxyName;
      else
         return getContainer().getEjbName();
   }
}
