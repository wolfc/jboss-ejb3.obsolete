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
package org.jboss.ejb3.proxy.handler.session.stateful;

import java.io.Serializable;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.proxy.container.InvokableContext;

/**
 * StatefulLocalProxyInvocationHandler
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatefulLocalProxyInvocationHandler extends StatefulProxyInvocationHandlerBase implements Serializable
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param containerName The name of the target container
    * @param containerGuid The globally-unique name of the container
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   public StatefulLocalProxyInvocationHandler(final String containerName, final String containerGuid,
         final Interceptor[] interceptors)
   {
      super(containerName, containerGuid, interceptors, null, null);
   }

   /**
    * Constructor
    * 
    * @param containerName The name of the target container
    * @param containerGuid The globally-unique name of the container
    * @param businessInterfaceType The possibly null businessInterfaceType
    *   marking this invocation hander as specific to a given
    *   EJB3 Business Interface
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   public StatefulLocalProxyInvocationHandler(final String containerName, final String containerGuid,
         final Interceptor[] interceptors, final String businessInterfaceType)
   {
      super(containerName, containerGuid, interceptors, businessInterfaceType, null);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.handler.session.SessionProxyInvocationHandlerBase#getContainer()
    */
   @Override
   protected InvokableContext getContainer()
   {
      return this.getContainerLocally();
   }

}
