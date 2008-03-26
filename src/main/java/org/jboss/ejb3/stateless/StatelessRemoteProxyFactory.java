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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.RemoteHome;
import javax.naming.NamingException;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.jboss.remoting.InvokerLocator;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessRemoteProxyFactory extends BaseStatelessProxyFactory implements RemoteProxyFactory
{
   private static final Logger log = Logger.getLogger(StatelessRemoteProxyFactory.class);

   protected RemoteBinding binding;
   protected InvokerLocator locator;

   public StatelessRemoteProxyFactory(SessionContainer container, RemoteBinding binding)
   {
      super(container, binding.jndiBinding());
      
      this.binding = binding;
   }

   protected Class<?>[] getInterfaces()
   {
      SessionContainer container = this.getContainer();
      RemoteHome remoteHome = container.getAnnotation(RemoteHome.class);

      boolean bindTogether = false;

      if (remoteHome != null && bindHomeAndBusinessTogether(container))
         bindTogether = true;

      // Obtain all remote interfaces
      Set<Class<?>> remoteInterfaces = new HashSet<Class<?>>();
      remoteInterfaces.addAll(Arrays.asList(ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(getContainer())));

      // Ensure remote interfaces defined
      if (remoteInterfaces.size() > 0)
      {
         // Add JBossProxy
         remoteInterfaces.add(JBossProxy.class);

         // If binding along w/ home, add home
         if (bindTogether)
         {
            remoteInterfaces.add(remoteHome.value());
         }
      }
      else
      {
         // No remote interfaces defined, log warning
         log.warn("[EJBTHREE-933] NPE when deploying web service beans");
      }

      // Return
      return remoteInterfaces.toArray(new Class<?>[]
      {});
   }
   
   protected void ensureEjb21ViewComplete()
   {
      // Obtain Container
      EJBContainer container = this.getContainer();

      // Obtaine @RemoteHome
      RemoteHome remoteHome = container.getAnnotation(RemoteHome.class);

      // Ensure that if EJB 2.1 Components are defined, they're complete
      this.ensureEjb21ViewComplete(remoteHome == null ? null : remoteHome.value(), ProxyFactoryHelper
            .getRemoteInterfaces(container));
   }
   
   protected boolean bindHomeAndBusinessTogether(EJBContainer container)
   {
      return ProxyFactoryHelper.getHomeJndiName(container).equals(ProxyFactoryHelper.getRemoteBusinessJndiName(container));
   }

   public void init() throws Exception
   {
      super.init();
      String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(binding);
      locator = new InvokerLocator(clientBindUrl);
   }

   public void start() throws Exception
   {
      super.start();
      EJBContainer statelessContainer = (EJBContainer) getContainer();
      RemoteHome remoteHome = (RemoteHome) statelessContainer.resolveAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statelessContainer))
      {
         Object homeProxy = createHomeProxy(remoteHome.value());
         String jndiName = ProxyFactoryHelper.getHomeJndiName(getContainer());
         try
         {
            log.debug("Binding proxy for " + getContainer().getEjbName() + " in JNDI at " + jndiName);
            Util.rebind(getContainer().getInitialContext(), jndiName, homeProxy);
         }
         catch (NamingException e)
         {
            NamingException namingException = new NamingException("Could not bind stateless home proxy with ejb name " + getContainer().getEjbName() + " into JNDI under jndiName: " + getContainer().getInitialContext().getNameInNamespace() + "/" + jndiName);
            namingException.setRootCause(e);
            throw namingException;
         }

      }
   }

   public void stop() throws Exception
   {
      super.stop();
      EJBContainer statelessContainer = (EJBContainer) getContainer();
      RemoteHome remoteHome = (RemoteHome) statelessContainer.resolveAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statelessContainer))
      {
         Util.unbind(getContainer().getInitialContext(), ProxyFactoryHelper.getHomeJndiName(getContainer()));
      }
   }

   protected StatelessHandleImpl getHandle()
   {
      StatelessHandleImpl handle = new StatelessHandleImpl();
      RemoteBinding remoteBinding = (RemoteBinding) getContainer().resolveAnnotation(RemoteBinding.class);
      if (remoteBinding != null)
         handle.jndiName = remoteBinding.jndiBinding() ;

      return handle;
   }

   public Object createHomeProxy(Class homeInterface)
   {
      try
      {
         String stackName = "StatelessSessionClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         StatelessRemoteProxy proxy = new StatelessRemoteProxy(getContainer(),
               stack.createInterceptors(getContainer().getAdvisor(), null), locator);
         setEjb21Objects(proxy);
         Class[] interfaces = {homeInterface};
         return java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(), interfaces, proxy);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
   }

   public Object createProxy()
   {
      String stackName = "StatelessSessionClientInterceptors";
      if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
      {
         stackName = binding.interceptorStack();
      }
      AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
      StatelessRemoteProxy proxy = new StatelessRemoteProxy(getContainer(),
            stack.createInterceptors(getContainer().getAdvisor(), null), locator);
      setEjb21Objects(proxy);
      return constructProxy(proxy);
   }

}
