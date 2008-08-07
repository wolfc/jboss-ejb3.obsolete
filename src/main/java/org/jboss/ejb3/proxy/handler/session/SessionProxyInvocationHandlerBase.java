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

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.handler.ProxyInvocationHandlerBase;
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

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor 
    * 
    * @param containerName The name of the target Container
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   protected SessionProxyInvocationHandlerBase(final String containerName, final Interceptor[] interceptors)
   {
      super(containerName, interceptors);
   }

   /**
    * Returns the container housed locally
    * 
    * @return
    */
   protected InvokableContext getContainerLocally()
   {
      // Lookup
      Object obj = Ejb3RegistrarLocator.locateRegistrar().lookup(this.getContainerName());

      // Ensure of correct type
      assert obj instanceof InvokableContext : "Container retrieved from " + Ejb3Registrar.class.getSimpleName()
            + " was not of expected type " + InvokableContext.class.getName() + " but was instead " + obj;

      // Return
      return (InvokableContext) obj;
   }

   // ------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Obtains the Container upon which this Proxy should invoke
    * 
    * @return
    */
   protected abstract InvokableContext getContainer();
}
