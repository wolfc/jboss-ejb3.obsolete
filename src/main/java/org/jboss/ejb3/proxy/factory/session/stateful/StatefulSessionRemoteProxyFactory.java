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
package org.jboss.ejb3.proxy.factory.session.stateful;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.Set;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.proxy.container.StatefulSessionInvokableContext;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulRemoteProxyInvocationHandler;
import org.jboss.ejb3.proxy.remoting.Ejb3PojiProxy;
import org.jboss.ejb3.proxy.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.remoting.InvokerLocator;

/**
 * StatefulSessionRemoteProxyFactory
 * 
 * A SFSB Proxy Factory for Remote Views
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatefulSessionRemoteProxyFactory extends StatefulSessionProxyFactoryBase implements SessionProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(StatefulSessionRemoteProxyFactory.class);

   private static final String STACK_NAME_STATEFUL_SESSION_CLIENT_INTERCEPTORS = "StatefulSessionClientInterceptors";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * URL to be used in Remoting
    */
   private String url;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param name The unique name for this ProxyFactory
    * @param containerName The name of the InvokableContext (container)
    *   upon which Proxies will invoke
    * @param metadata The metadata representing this SFSB
    * @param classloader The ClassLoader associated with the StatelessContainer
    *       for which this ProxyFactory is to generate Proxies
    * @param url The URL to use for remoting
    */
   public StatefulSessionRemoteProxyFactory(final String name, final String containerName,
         final JBossSessionBeanMetaData metadata, final ClassLoader classloader, final String url)
   {
      // Call Super
      super(name, containerName, metadata, classloader);
      this.setUrl(url);
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns the a Set of String representations of the Business Interface Types
    * 
    *  @return
    */
   @Override
   protected final Set<String> getBusinessInterfaceTypes()
   {
      return this.getMetadata().getBusinessRemotes();
   }

   /**
    * Returns the String representation of the Home Interface Type
    * @return
    */
   @Override
   protected final String getHomeType()
   {
      return this.getMetadata().getHome();
   }

   /**
    * Returns the String representation of the EJB2.x Interface Type
    * 
    *  @return
    */
   @Override
   protected final String getEjb2xInterfaceType()
   {
      return this.getMetadata().getRemote();
   }

   /**
    * Return the name of the interceptor stack to apply to 
    * proxies created by this proxy factory
    * 
    * @return
    */
   @Override
   protected String getInterceptorStackName()
   {
      return StatefulSessionRemoteProxyFactory.STACK_NAME_STATEFUL_SESSION_CLIENT_INTERCEPTORS;
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   protected SessionProxyInvocationHandler createInvocationHandler(String businessInterfaceName)
   {
      // Create
      SessionProxyInvocationHandler handler = new StatefulRemoteProxyInvocationHandler(businessInterfaceName, this
            .getUrl());

      // Return
      return handler;
   }

   /**
    * Obtains the Container used by this Proxy Factory
    * 
    * @return The Container for this Proxy Factory
    */
   @Override
   protected StatefulSessionInvokableContext<?> obtainContainer()
   {
      /*
       * Obtain the Container
       */
      StatefulSessionInvokableContext<?> container = null;
      String containerName = this.getContainerName();

      // Create an InvokerLocator
      String url = this.getUrl();
      assert url != null && !url.trim().equals("") : InvokerLocator.class.getSimpleName()
            + " URL is required, but is not specified for " + this;
      InvokerLocator locator = null;
      try
      {
         locator = new InvokerLocator(url);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(
               "URL for " + InvokerLocator.class.getSimpleName() + " in " + this + " was improper", e);
      }

      // Create a POJI Proxy to the Container
      Interceptor[] interceptors =
      {IsLocalProxyFactoryInterceptor.singleton, InvokeRemoteInterceptor.singleton};
      PojiProxy handler = new Ejb3PojiProxy(containerName, locator, interceptors);
      Class<?>[] interfaces = new Class<?>[]
      {StatefulSessionInvokableContext.class};
      container = (StatefulSessionInvokableContext<?>) Proxy.newProxyInstance(interfaces[0].getClassLoader(),
            interfaces, handler);
      log.debug("Created Remoting Proxy to " + StatefulSessionInvokableContext.class.getSimpleName() + " with name "
            + containerName + " using URL " + url);

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
