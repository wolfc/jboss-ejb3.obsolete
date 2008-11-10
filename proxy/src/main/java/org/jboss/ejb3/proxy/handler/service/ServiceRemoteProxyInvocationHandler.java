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
package org.jboss.ejb3.proxy.handler.service;

import java.io.Serializable;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.remoting.ProxyRemotingUtils;

/**
 * ServiceRemoteProxyInvocationHandler
 * 
 * Invocation Handler for Remote view of
 * a @Service proxy
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ServiceRemoteProxyInvocationHandler extends ServiceProxyInvocationHandlerBase
      implements
         Serializable,
         ServiceProxyInvocationHandler
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private String url;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param containerName The name of the target container
    * @param url The URL to the Remote Host
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   public ServiceRemoteProxyInvocationHandler(final String containerName, final String containerGuid,
         final Interceptor[] interceptors, final String url)
   {
      super(containerName, containerGuid, interceptors);

      // Adjust URL if not specified to a default
      String remotingUrl = url;
      if (remotingUrl == null || remotingUrl.trim().length() == 0)
      {
         remotingUrl = ProxyRemotingUtils.getDefaultClientBinding();
      }
      this.setUrl(remotingUrl);
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
      return this.createRemoteProxyToContainer(this.getUrl());
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates and returns a Remoting Proxy to invoke upon the container
    * 
    * This implementation is marked as FIXME as remoting should be an add-on
    * capability atop ejb3-proxy
    * 
    * @param url The location of the remote host holding the Container
    * @return
    */
   //FIXME
   protected InvokableContext createRemoteProxyToContainer(String url)
   {
      InvokableContext container = ProxyRemotingUtils.createRemoteProxyToContainer(this.getContainerName(), this
            .getContainerGuid(), url, this.getInterceptors(), null);
      return container;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public String getUrl()
   {
      return url;
   }

   public void setUrl(String url)
   {
      this.url = url;
   }

}
