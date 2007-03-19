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
package org.jboss.ejb3.stateful;

import java.lang.reflect.InvocationTargetException;
import javax.naming.NamingException;
import javax.ejb.RemoteHome;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.aspects.remoting.Remoting;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.naming.Util;
import org.jboss.remoting.InvokerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulRemoteProxyFactory extends BaseStatefulProxyFactory implements RemoteProxyFactory
{
   private static final Logger log = Logger.getLogger(StatefulRemoteProxyFactory.class);

   private RemoteBinding binding;
   private InvokerLocator locator;

   public void setRemoteBinding(RemoteBinding binding)
   {
      this.binding = binding;
   }

   protected Class[] getInterfaces()
   {
      Class[] interfaces;
      
      StatefulContainer statefulContainer = (StatefulContainer) container;
      RemoteHome remoteHome = (RemoteHome) statefulContainer.resolveAnnotation(RemoteHome.class);
      
      boolean bindTogether = false;
      
      if (remoteHome != null && bindHomeAndBusinessTogether(statefulContainer))
         bindTogether = true;
      
      Class[] remoteInterfaces = ProxyFactoryHelper.getRemoteInterfaces(container);
      if (bindTogether)
         interfaces = new Class[remoteInterfaces.length + 3];
      else
         interfaces = new Class[remoteInterfaces.length + 2];

      System.arraycopy(remoteInterfaces, 0, interfaces, 0, remoteInterfaces.length);
      interfaces[remoteInterfaces.length] = JBossProxy.class;
      interfaces[remoteInterfaces.length + 1] = javax.ejb.EJBObject.class;
      if (bindTogether)
         interfaces[remoteInterfaces.length + 2] = remoteHome.value();
         
      return interfaces;

   }
   
   protected boolean bindHomeAndBusinessTogether(StatefulContainer container)
   {
      return ProxyFactoryHelper.getHomeJndiName(container).equals(ProxyFactoryHelper.getRemoteJndiName(container));
   }

   protected void initializeJndiName()
   {
      jndiName = ProxyFactoryHelper.getRemoteJndiName(container, binding);
   }

   public void init() throws Exception
   {
      super.init();
      String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(binding);
      locator = new InvokerLocator(clientBindUrl);
   }

   public void start() throws Exception
   {
      init();

      super.start();
      Class[] interfaces = {ProxyFactory.class};
      Object factoryProxy = Remoting.createPojiProxy(jndiName + PROXY_FACTORY_NAME, interfaces, ProxyFactoryHelper.getClientBindUrl(binding));
      try
      {
         Util.rebind(container.getInitialContext(), jndiName + PROXY_FACTORY_NAME, factoryProxy);
      }
      catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful remote proxy with ejb name " + container.getEjbName() + " into JNDI under jndiName: " + container.getInitialContext().getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }
      Dispatcher.singleton.registerTarget(jndiName + PROXY_FACTORY_NAME, this);

      StatefulContainer statefulContainer = (StatefulContainer) container;
      RemoteHome remoteHome = (RemoteHome) statefulContainer.resolveAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Object homeProxy = createHomeProxy(remoteHome.value());
         Util.rebind(container.getInitialContext(), ProxyFactoryHelper.getHomeJndiName(container), homeProxy);
      }
   }

   public void stop() throws Exception
   {
      Util.unbind(container.getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      Dispatcher.singleton.unregisterTarget(jndiName + PROXY_FACTORY_NAME);
      StatefulContainer statefulContainer = (StatefulContainer) container;
      RemoteHome remoteHome = (RemoteHome) statefulContainer.resolveAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Util.unbind(container.getInitialContext(), ProxyFactoryHelper.getHomeJndiName(container));
      }
      super.stop();
   }


   public Object createHomeProxy(Class homeInterface)
   {
      try
      {
         Object containerId = container.getObjectName().getCanonicalName();
         String stackName = "StatefulSessionClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
         StatefulHomeRemoteProxy proxy = new StatefulHomeRemoteProxy(containerId, stack.createInterceptors((Advisor) container, null), locator);


         setEjb21Objects(proxy);
         Class[] intfs = {homeInterface};
         return java.lang.reflect.Proxy.newProxyInstance(container.getBeanClass().getClassLoader(), intfs, proxy);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
   }
   public Object createProxy()
   {
      try
      {
         Object containerId = container.getObjectName().getCanonicalName();
         String stackName = "StatefulSessionClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
         StatefulRemoteProxy proxy = new StatefulRemoteProxy(containerId, stack.createInterceptors((Advisor) container, null), locator);


         setEjb21Objects(proxy);
         Object[] args = {proxy};
         return proxyConstructor.newInstance(args);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getTargetException());  //To change body of catch statement use Options | File Templates.
      }
   }

   protected StatefulHandleImpl getHandle()
   {
      StatefulHandleImpl handle = new StatefulHandleImpl();
      RemoteBinding remoteBinding = (RemoteBinding) advisor.resolveAnnotation(RemoteBinding.class);
      if (remoteBinding != null)
         handle.jndiName = remoteBinding.jndiBinding();

      return handle;
   }

   public Object createProxy(Object id)
   {
      try
      {
         Object containerId = container.getObjectName().getCanonicalName();
         String stackName = "StatefulSessionClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         StatefulRemoteProxy proxy = new StatefulRemoteProxy(containerId, stack.createInterceptors((Advisor) container, null), locator, id);
         setEjb21Objects(proxy);
         Object[] args = {proxy};
         return proxyConstructor.newInstance(args);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getTargetException());  //To change body of catch statement use Options | File Templates.
      }
   }


}
