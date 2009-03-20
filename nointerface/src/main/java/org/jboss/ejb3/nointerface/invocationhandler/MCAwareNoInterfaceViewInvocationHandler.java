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

import javax.ejb.EJBContainer;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;

/**
 * MCAwareNoInterfaceViewInvocationHandler
 *
 * An {@link InvocationHandler} which corresponds to the
 * no-interface view of a {@link EJBContainer}. All calls on the no-interface
 * view are routed through this {@link InvocationHandler} to the container.
 *
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MCAwareNoInterfaceViewInvocationHandler implements InvocationHandler
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(MCAwareNoInterfaceViewInvocationHandler.class);

   /**
    * The KernelControllerContext corresponding to the container of a bean for which
    * the no-interface view is to be created by this factory. This context
    * may <i>not</i> be in INSTALLED state. This factory is responsible
    * for pushing it to INSTALLED state whenever necessary. 
    * 
    * All calls to this invocation handler will be forwarded to the container represented
    * by this context
    * 
    *
    */
   private KernelControllerContext containerContext;

   /**
    * The target (=sessionId) used to interact with the {@link InvokableContext}
    */
   private Object target;

   /**
    * Constructor
    * @param container
    */
   public MCAwareNoInterfaceViewInvocationHandler(KernelControllerContext containerContext, Object target)
   {
      assert containerContext != null : "Container context is null for no-interface view invocation handler";
      this.containerContext = containerContext;
      this.target = target;
   }

   /**
    * The entry point when a client calls any methods on the no-interface view of a bean,
    * returned through JNDI.
    *
    *
    * @param proxy
    * @param method The invoked method
    * @param args The arguments to the method
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // TODO: Some methods like toString() can be handled locally.
      // But as of now let's just pass it on to the container.

      if (logger.isTraceEnabled())
      {
         logger.trace("Pushing the container context to INSTALLED state from its current state = "
               + this.containerContext.getState().getStateString());
      }
      // first push the context corresponding to the container to INSTALLED
      this.containerContext.getController().change(this.containerContext, ControllerState.INSTALLED);
      // get hold of the container from its context
      InvokableContext container = (InvokableContext) this.containerContext.getTarget();
      // finally pass on the control to the container
      SerializableMethod serializableMethod = new SerializableMethod(method);
      return container.invoke(this.target, serializableMethod, args);
   }

   /**
    * Returns the context corresponding to the container, associated with this invocation handler
    *
    * @return
    */
   public KernelControllerContext getContainerContext()
   {
      return this.containerContext;
   }

}
