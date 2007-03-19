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

import javax.ejb.RemoteHome;
import javax.naming.NamingException;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.naming.Util;


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

   public void setRemoteBinding(RemoteBinding binding)
   {
      this.binding = binding;
   }

   protected Class[] getInterfaces()
   {
      Class[] interfaces;
      
      StatelessContainer statelessContainer = (StatelessContainer) container;
      RemoteHome remoteHome = (RemoteHome) statelessContainer.resolveAnnotation(RemoteHome.class);
      
      boolean bindTogether = false;
      
      if (remoteHome != null && bindHomeAndBusinessTogether(statelessContainer))
         bindTogether = true;
      
      Class[] remoteInterfaces = ProxyFactoryHelper.getRemoteInterfaces(container);
      
      if (bindTogether)
         interfaces = new Class[remoteInterfaces.length + 3];
      else
         interfaces = new Class[remoteInterfaces.length + 2];
      
      System.arraycopy(remoteInterfaces, 0, interfaces, 0,
            remoteInterfaces.length);
      interfaces[remoteInterfaces.length] = JBossProxy.class;
      interfaces[remoteInterfaces.length + 1] = javax.ejb.EJBObject.class;
      
      if (bindTogether)
         interfaces[remoteInterfaces.length + 2] = remoteHome.value();

      return interfaces;

   }
   
   protected boolean bindHomeAndBusinessTogether(EJBContainer container)
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
      super.start();
      EJBContainer statelessContainer = (EJBContainer) container;
      RemoteHome remoteHome = (RemoteHome) statelessContainer.resolveAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statelessContainer))
      {
         Object homeProxy = createHomeProxy(remoteHome.value());
         try
         {
            Util.rebind(container.getInitialContext(), ProxyFactoryHelper.getHomeJndiName(container), homeProxy);
         }
         catch (NamingException e)
         {
            NamingException namingException = new NamingException("Could not bind stateless home proxy with ejb name " + container.getEjbName() + " into JNDI under jndiName: " + container.getInitialContext().getNameInNamespace() + "/" + ProxyFactoryHelper.getHomeJndiName(container));
            namingException.setRootCause(e);
            throw namingException;
         }

      }
   }

   public void stop() throws Exception
   {
      super.stop();
      EJBContainer statelessContainer = (EJBContainer) container;
      RemoteHome remoteHome = (RemoteHome) statelessContainer.resolveAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statelessContainer))
      {
         Util.unbind(container.getInitialContext(), ProxyFactoryHelper.getHomeJndiName(container));
      }
   }

   protected StatelessHandleImpl getHandle()
   {
      StatelessHandleImpl handle = new StatelessHandleImpl();
      RemoteBinding remoteBinding = (RemoteBinding) advisor.resolveAnnotation(RemoteBinding.class);
      if (remoteBinding != null)
         handle.jndiName = remoteBinding.jndiBinding() ;

      return handle;
   }

   public Object createHomeProxy(Class homeInterface)
   {
      try
      {
         Object containerId = container.getObjectName().getCanonicalName();
         ;
         String stackName = "StatelessSessionClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         StatelessRemoteProxy proxy = new StatelessRemoteProxy(containerId, stack.createInterceptors((Advisor) container, null), locator);
         setEjb21Objects(proxy);
         Class[] interfaces = {homeInterface};
         return java.lang.reflect.Proxy.newProxyInstance(container.getBeanClass().getClassLoader(), interfaces, proxy);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
   }

   public Object createProxy()
   {
//      try
      {
         Object containerId = container.getObjectName().getCanonicalName();
         ;
         String stackName = "StatelessSessionClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         StatelessRemoteProxy proxy = new StatelessRemoteProxy(containerId, stack.createInterceptors((Advisor) container, null), locator);
         setEjb21Objects(proxy);
         /*
         Object[] args = {proxy};
         return proxyConstructor.newInstance(args);
         */
         return constructProxy(proxy);
      }
      /*
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
      */
   }

}
