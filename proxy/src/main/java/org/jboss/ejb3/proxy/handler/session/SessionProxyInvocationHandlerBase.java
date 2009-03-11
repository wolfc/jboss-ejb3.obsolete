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
package org.jboss.ejb3.proxy.handler.session;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.proxy.handler.NotEligibleForDirectInvocationException;
import org.jboss.ejb3.proxy.handler.ProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.intf.SessionProxy;
import org.jboss.logging.Logger;

/**
 * SessionProxyInvocationHandlerBase
 * 
 * Abstract base from which all JBoss Session Proxy InvocationHandlers
 * may extend
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionProxyInvocationHandlerBase extends ProxyInvocationHandlerBase
      implements
         SessionProxyInvocationHandler,
         Serializable
{
   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(SessionProxyInvocationHandlerBase.class);

   private static final String METHOD_NAME_GET_TARGET = "getTarget";

   private static final String METHOD_NAME_SET_TARGET = "setTarget";

   private static final SerializableMethod METHOD_GET_TARGET;

   private static final SerializableMethod METHOD_SET_TARGET;

   static
   {
      try
      {
         METHOD_GET_TARGET = new SerializableMethod(SessionProxy.class.getDeclaredMethod(METHOD_NAME_GET_TARGET));
         METHOD_SET_TARGET = new SerializableMethod(SessionProxy.class.getDeclaredMethod(METHOD_NAME_SET_TARGET,
               Object.class));
      }
      catch (NoSuchMethodException nsme)
      {
         throw new RuntimeException(
               "Methods for handling directly by the InvocationHandler were not initialized correctly", nsme);
      }

   }

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * The target for this Invocation (for instance, Session ID)
    */
   private Object target;

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor 
    * 
    * @param containerName The name of the target Container
    * @param containerGuid The globally-unique name of the container
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   protected SessionProxyInvocationHandlerBase(final String containerName, final String containerGuid,
         final Interceptor[] interceptors, final Object target)
   {
      super(containerName, containerGuid, interceptors);
      this.setTarget(target);
   }

   // ------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Handles the current invocation directly in this invocation handler.  Only 
    * a subset of method invocations are eligible for this treatment, else 
    * a NotEligibleForDirectInvocationException will be thrown
    * 
    * @param proxy
    * @param args Arguments of the current invocation
    * @param invokedMethod The method invoked
    * @return
    * @throws NotEligibleForDirectInvocationException
    */
   @Override
   protected Object handleInvocationDirectly(Object proxy, Object[] args, Method invokedMethod)
         throws NotEligibleForDirectInvocationException
   {
      // Obtain the invoked method
      assert invokedMethod != null : "Invoked Method was not set upon invocation of " + this.getClass().getName();

      // getTarget
      if (invokedMethod.equals(METHOD_GET_TARGET.toMethod()))
      {
         return this.getTarget();
      }
      // setTarget
      if (invokedMethod.equals(METHOD_SET_TARGET.toMethod()))
      {
         assert args.length == 1 : "Expecting exactly one argument for invocation of " + METHOD_SET_TARGET;
         Object arg = args[0];
         assert arg instanceof Serializable : "Argument must be instance of " + Serializable.class.getName();
         Serializable id = (Serializable) arg;
         this.setTarget(id);
         return null;
      }

      // Call to super
      return super.handleInvocationDirectly(proxy, args, invokedMethod);
   }

   // ------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public Object getTarget()
   {
      return target;
   }

   public void setTarget(Object target)
   {
      this.target = target;
   }

}
