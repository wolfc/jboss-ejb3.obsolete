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
package org.jboss.ejb3.proxy.factory.stateless;

import javax.ejb.RemoteHome;
import javax.naming.NamingException;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.factory.RemoteProxyFactory;
import org.jboss.ejb3.proxy.handler.stateless.StatelessRemoteProxyInvocationHandler;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.util.naming.Util;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessRemoteProxyFactory extends BaseStatelessRemoteProxyFactory implements RemoteProxyFactory
{
   private static final Logger log = Logger.getLogger(StatelessRemoteProxyFactory.class);
   
   private static final String STACK_NAME_STATELESS_SESSION_CLIENT_INTERCEPTORS = "StatelessSessionClientInterceptors";

   public StatelessRemoteProxyFactory(SessionSpecContainer container, RemoteBinding binding)
   {
      super(container, binding);
   }
   
   /**
    * Whether or not to bind the home and business interfaces together
    * 
    * @return
    */
   @Override
   protected boolean bindHomeAndBusinessTogether()
   {
      SessionSpecContainer container = this.getContainer();
      return ProxyFactoryHelper.getHomeJndiName(container).equals(ProxyFactoryHelper.getRemoteBusinessJndiName(container));
   }

   public void init() throws Exception
   {
      super.init();
   }

   public void start() throws Exception
   {
      super.start();
      RemoteHome remoteHome = this.getContainer().getAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndEjb21ViewTogether(this.getContainer()))
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
      SessionSpecContainer statelessContainer = this.getContainer();
      RemoteHome remoteHome = this.getContainer().getAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndEjb21ViewTogether(statelessContainer))
      {
         Util.unbind(getContainer().getInitialContext(), ProxyFactoryHelper.getHomeJndiName(getContainer()));
      }
   }

   public Object createHomeProxy(Class<?> homeInterface)
   {
      try
      {
         String stackName = this.getStackNameInterceptors();
         RemoteBinding binding = this.getBinding();
         InvokerLocator locator = this.getLocator();
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         StatelessRemoteProxyInvocationHandler proxy = new StatelessRemoteProxyInvocationHandler(getContainer(), stack
               .createInterceptors(getContainer().getAdvisor(), null), locator, null);
         setEjb21Objects(proxy);
         Class<?>[] interfaces = {homeInterface};
         return java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(), interfaces, proxy);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Returns the interface type for Home
    * 
    * @return
    */
   @Override
   protected Class<?> getHomeType()
   {
      return ProxyFactoryHelper.getRemoteHomeInterface(this.getContainer());
   }

   @Override
   String getStackNameInterceptors()
   {
      return StatelessRemoteProxyFactory.STACK_NAME_STATELESS_SESSION_CLIENT_INTERCEPTORS;
   }

}
