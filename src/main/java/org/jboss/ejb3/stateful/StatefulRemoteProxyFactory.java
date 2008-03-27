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

import java.lang.reflect.Proxy;

import javax.ejb.RemoteHome;
import javax.naming.NamingException;

import org.jboss.aop.AspectManager;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.util.naming.Util;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 *
 * @version $Revision$
 */
public class StatefulRemoteProxyFactory extends BaseStatefulProxyFactory implements RemoteProxyFactory
{
   private static final Logger log = Logger.getLogger(StatefulRemoteProxyFactory.class);
   
//   public static final String FACTORY_ATTRIBUTE = ",element=ProxyFactory";
   
   private static final String STACK_NAME_STATEFUL_SESSION_CLIENT_INTERCEPTORS = "StatefulSessionClientInterceptors";
   
   private RemoteBinding binding;
   private InvokerLocator locator;

   public StatefulRemoteProxyFactory(SessionContainer container, RemoteBinding binding)
   {
      super(container, binding.jndiBinding());
      
      this.binding = binding;
   }
   
   /**
    * Defines the access type for this Proxies created by this Factory
    * 
    * @return
    */
   @Override
   protected ProxyAccessType getProxyAccessType(){
      return ProxyAccessType.REMOTE;
   }
   
   protected void validateEjb21Views()
   { 
      // Obtain Container
      SessionContainer container = this.getContainer();
      
      // Obtain @RemoteHome
      RemoteHome remoteHome = container.getAnnotation(RemoteHome.class);

      // Ensure that if EJB 2.1 Components are defined, they're complete
      this.validateEjb21Views(remoteHome == null ? null : remoteHome.value(), ProxyFactoryHelper
            .getRemoteInterfaces(container));

   }
   
   protected boolean bindHomeAndBusinessTogether(SessionContainer container)
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
      init();

      super.start();
      Class<?>[] interfaces = {ProxyFactory.class};
      String targetId = getTargetId();
      String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(binding);
      Object factoryProxy = createPojiProxy(targetId, interfaces, clientBindUrl);
      log.debug("Binding proxy factory for " + getContainer().getEjbName() + " in JNDI at " + jndiName + PROXY_FACTORY_NAME + " with client bind url " + clientBindUrl);
      try
      {
         Util.rebind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME, factoryProxy);
      }
      catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful remote proxy with ejb name " + getContainer().getEjbName() + " into JNDI under jndiName: " + getContainer().getInitialContext().getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }
      assert !Dispatcher.singleton.isRegistered(targetId) : targetId + " is already registered";
      Dispatcher.singleton.registerTarget(targetId, this);

      SessionContainer statefulContainer = (SessionContainer) getContainer();
      RemoteHome remoteHome = (RemoteHome) statefulContainer.resolveAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Object homeProxy = createHomeProxy(remoteHome.value());
         String homeJndiName = ProxyFactoryHelper.getHomeJndiName(getContainer());
         log.debug("Binding home proxy at " + homeJndiName);
         Util.rebind(getContainer().getInitialContext(), homeJndiName, homeProxy);
      }
   }

   public void stop() throws Exception
   {
      Util.unbind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      Dispatcher.singleton.unregisterTarget(getTargetId());
      
      SessionContainer statefulContainer = this.getContainer();
      RemoteHome remoteHome = statefulContainer.getAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Util.unbind(this.getContainer().getInitialContext(), ProxyFactoryHelper.getHomeJndiName(getContainer()));
      }
      super.stop();
   }


   public Object createHomeProxy(Class<?> homeInterface)
   {
      try
      {
         Object containerId = getContainer().getObjectName().getCanonicalName();
         String stackName = StatefulRemoteProxyFactory.STACK_NAME_STATEFUL_SESSION_CLIENT_INTERCEPTORS;
         if (binding.interceptorStack() != null && !binding.interceptorStack().trim().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
         StatefulHomeRemoteProxy proxy = new StatefulHomeRemoteProxy(getContainer(), stack.createInterceptors(getContainer().getAdvisor(), null), locator);

         setEjb21Objects(proxy);
         Class<?>[] intfs = {homeInterface};
         return java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(), intfs, proxy);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public Object createProxy()
   {
      Object id = getContainer().createSession();
      return this.createProxy(id);
   }
   public Object createEjb21Proxy()
   {
      Object id = getContainer().createSession();
      return this.createEjb21Proxy(id);
   }

   protected StatefulHandleImpl getHandle()
   {
      StatefulHandleImpl handle = new StatefulHandleImpl();
      handle.jndiName = jndiName;

      return handle;
   }

   public Object createProxy(Object id)
   {
      return this.createProxy(id,SpecificationInterfaceType.EJB30_BUSINESS);
   }
   
   public Object createEjb21Proxy(Object id)
   {
      return this.createProxy(id,SpecificationInterfaceType.EJB21);
   }
   
   private Object createProxy(Object id,SpecificationInterfaceType type)
   {
      String stackName = StatefulRemoteProxyFactory.STACK_NAME_STATEFUL_SESSION_CLIENT_INTERCEPTORS;
      if (binding.interceptorStack() != null && !binding.interceptorStack().trim().equals(""))
      {
         stackName = binding.interceptorStack();
      }
      AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
      if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
      StatefulRemoteProxy proxy = new StatefulRemoteProxy(getContainer(), stack.createInterceptors(getContainer().getAdvisor(), null), locator, id);
      this.setEjb21Objects(proxy);
      return type.equals(SpecificationInterfaceType.EJB21) ? this.constructEjb21Proxy(proxy) : this
            .constructBusinessProxy(proxy);
   }
   
   /**
    * @return unique name for this proxy factory
    */
   protected String getTargetId()
   {  
      assert jndiName != null : "jndiName is null"; 
      return jndiName + PROXY_FACTORY_NAME;
   }
   
   protected Object createPojiProxy(Object oid, Class<?>[] interfaces, String uri) throws Exception
   {
      InvokerLocator locator = new InvokerLocator(uri);
      Interceptor[] interceptors = {IsLocalProxyFactoryInterceptor.singleton, InvokeRemoteInterceptor.singleton};
      PojiProxy proxy = new PojiProxy(oid, locator, interceptors);
      return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, proxy);

   }


}
