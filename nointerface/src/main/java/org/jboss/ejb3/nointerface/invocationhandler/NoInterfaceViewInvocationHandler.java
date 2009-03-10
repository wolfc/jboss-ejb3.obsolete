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
package org.jboss.ejb3.nointerface.invocationhandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.proxy.container.InvokableContext;

/**
 * NoInterfaceViewInvocationHandler
 *
 * An {@link InvocationHandler} which corresponds to the
 * no-interface view of a {@link EJBContainer}. All calls on the no-interface
 * view are routed through this {@link InvocationHandler} to the container.
 *
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceViewInvocationHandler implements InvocationHandler
{

   /**
    * The container to which this invocation handler corresponds to.
    * All calls to this invocation handler will be forwarded to this
    * container.
    *
    */
   private InvokableContext container;

   private Object proxy;

   /**
    * Constructor
    * @param container
    */
   public NoInterfaceViewInvocationHandler(InvokableContext container)
   {
      assert container != null : "Container is null for no-interface view invocation handler";
      this.container = container;
   }

   /**
    * The entry point when a client calls any methods on the no-interface view of a bean,
    * returned through JNDI.
    *
    * This method will do the common steps (common for SLSB and SFSB) before passing on the
    * call to {@link #doInvoke(Object, Method, Object[])}
    *
    * @param obj
    * @param method The invoked method
    * @param args The arguments to the method
    */
   public Object invoke(Object obj, Method method, Object[] args) throws Throwable
   {
      // TODO: Some methods like toString() can be handled locally.
      // But as of now let's just pass it on to the container.
      SerializableMethod serializableMethod = new SerializableMethod(method);
      return getContainer().invoke(this.proxy, serializableMethod, args);
   }

   /**
    * Returns the container associated with this invocation handler
    *
    * @return
    */
   public InvokableContext getContainer()
   {
      return this.container;
   }

   public void setProxy(Object proxy)
   {
      this.proxy = proxy;
   }

   public Object getProxy()
   {
      return this.proxy;
   }

}
