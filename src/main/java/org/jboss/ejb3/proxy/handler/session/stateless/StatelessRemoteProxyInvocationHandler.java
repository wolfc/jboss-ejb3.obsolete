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
package org.jboss.ejb3.proxy.handler.session.stateless;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.invocation.InvokableContextStatefulRemoteProxyInvocationHack;
import org.jboss.ejb3.proxy.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.remoting.InvokerLocator;

/**
 * StatelessRemoteProxyInvocationHandler
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatelessRemoteProxyInvocationHandler extends StatelessProxyInvocationHandlerBase implements Serializable
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
    * @param businessInterfaceType The possibly null businessInterfaceType
    *   marking this invocation hander as specific to a given
    *   EJB3 Business Interface
    * @param url The URL to the Remote Host
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   public StatelessRemoteProxyInvocationHandler(final String containerName, final String businessInterfaceType,
         final String url, final Interceptor[] interceptors)
   {
      super(containerName, businessInterfaceType, interceptors);
      this.setUrl(url);
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
    * @param url The location of the remote host holding the Container
    * @return
    */
   //FIXME Mostly a copy of the SFSB Remote Handler, but passing null 
   // as a SessionID.  Should be using more intelligent design, SLSB's have 
   // no Sessions.  To be reworked in InvokableContextStatefulRemoteProxyInvocationHack, 
   // as this implementation is @deprecated
   protected InvokableContext createRemoteProxyToContainer(String url)
   {
      // Create an InvokerLocator
      InvokerLocator locator = null;
      try
      {
         locator = new InvokerLocator(url);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException("Could not create " + InvokerLocator.class.getSimpleName() + " to url \"" + url
               + "\"", e);
      }

      /*
       * Define interceptors
       */

      // Manually define a few
      Interceptor[] interceptorsManuallyDefined =
      {IsLocalProxyFactoryInterceptor.singleton, InvokeRemoteInterceptor.singleton};

      // Get interceptors from the stack
      Interceptor[] interceptorsFromStack = this.getInterceptors();

      // Merge
      List<Interceptor> interceptorsManuallyDefinedList = Arrays.asList(interceptorsManuallyDefined);
      List<Interceptor> interceptorsFromStackList = Arrays.asList(interceptorsFromStack);
      Set<Interceptor> mergedInterceptors = new HashSet<Interceptor>();
      mergedInterceptors.addAll(interceptorsManuallyDefinedList);
      mergedInterceptors.addAll(interceptorsFromStackList);
      Interceptor[] interceptors = mergedInterceptors.toArray(new Interceptor[]
      {});

      // Create a POJI Proxy to the Container
      PojiProxy handler = new InvokableContextStatefulRemoteProxyInvocationHack(this.getContainerName(), locator,
            interceptors, null);
      Class<?>[] interfaces = new Class<?>[]
      {InvokableContext.class};
      InvokableContext container = (InvokableContext) Proxy.newProxyInstance(InvokableContext.class.getClassLoader(),
            interfaces, handler);

      // Return
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
