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
package org.jboss.ejb3.proxy.factory.stateful;

import java.lang.reflect.Proxy;

import javax.ejb.RemoteHome;
import javax.naming.NamingException;

import org.jboss.aop.AspectManager;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.proxy.ProxyFactory;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.factory.RemoteProxyFactory;
import org.jboss.ejb3.proxy.handler.BaseSessionRemoteProxyInvocationHandler;
import org.jboss.ejb3.proxy.handler.stateful.StatefulHomeRemoteProxyInvocationHandler;
import org.jboss.ejb3.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.session.SessionSpecContainer;
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
public class StatefulRemoteProxyFactory extends BaseStatefulRemoteProxyFactory implements RemoteProxyFactory
{
   private static final Logger log = Logger.getLogger(StatefulRemoteProxyFactory.class);
   
   private static final String STACK_NAME_STATEFUL_SESSION_CLIENT_INTERCEPTORS = "StatefulSessionClientInterceptors";

   public StatefulRemoteProxyFactory(SessionSpecContainer container, RemoteBinding binding)
   {
      super(container, binding);
   }
   
   /**
    * Returns whether this Proxy Factory is local.  A Hack until EJB3 Proxy 
    * is in place, but this keeps us moving forward easily.
    * 
    * @deprecated Hack
    * @return
    */
   @Deprecated
   protected boolean isLocal()
   {
      return false;
   }

   @Override
   public void start() throws Exception
   {
      super.start();
      Class<?>[] interfaces = {ProxyFactory.class};
      String targetId = getTargetId();
      String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(this.getBinding());
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

      SessionSpecContainer statefulContainer = this.getContainer();
      RemoteHome remoteHome = statefulContainer.getAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether())
      {
         Object homeProxy = createHomeProxy(remoteHome.value());
         String homeJndiName = ProxyFactoryHelper.getHomeJndiName(statefulContainer);
         log.debug("Binding home proxy at " + homeJndiName);
         Util.rebind(this.getContainer().getInitialContext(), homeJndiName, homeProxy);
      }
   }

   @Override
   public void stop() throws Exception
   {
      Util.unbind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      Dispatcher.singleton.unregisterTarget(getTargetId());
      
      SessionContainer statefulContainer = this.getContainer();
      RemoteHome remoteHome = statefulContainer.getAnnotation(RemoteHome.class);
      if (remoteHome != null && !bindHomeAndBusinessTogether())
      {
         Util.unbind(this.getContainer().getInitialContext(), ProxyFactoryHelper.getHomeJndiName(getContainer()));
      }
      super.stop();
   }

   public Object createHomeProxy(Class<?> homeInterface)
   {
      try
      {
         String stackName = StatefulRemoteProxyFactory.STACK_NAME_STATEFUL_SESSION_CLIENT_INTERCEPTORS;
         RemoteBinding binding = this.getBinding();
         if (binding.interceptorStack() != null && !binding.interceptorStack().trim().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
         StatefulHomeRemoteProxyInvocationHandler proxy = new StatefulHomeRemoteProxyInvocationHandler(getContainer(), stack.createInterceptors(
               getContainer().getAdvisor(), null), this.getLocator());

         setEjb21Objects(proxy);
         Class<?>[] intfs = {homeInterface};
         return java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(), intfs, proxy);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   protected void setEjb21Objects(BaseSessionRemoteProxyInvocationHandler proxy)
   {
      proxy.setHandle(this.createHandle());
      proxy.setHomeHandle(getHomeHandle());
      proxy.setEjbMetaData(getEjbMetaData());
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
      return StatefulRemoteProxyFactory.STACK_NAME_STATEFUL_SESSION_CLIENT_INTERCEPTORS;
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
