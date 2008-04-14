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
package org.jboss.ejb3.stateful;

import javax.ejb.EJBObject;
import javax.ejb.RemoteHome;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.ejb3.session.ProxyAccessType;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;

/**
 * BaseStatefulRemoteProxyFactory
 * 
 * Common base for factories generating remoting-enabled
 * proxies (ie. remote and clustering)
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class BaseStatefulRemoteProxyFactory extends BaseStatefulProxyFactory implements RemoteProxyFactory
{
   // Class Members
   
   private static final Logger log = Logger.getLogger(BaseStatefulRemoteProxyFactory.class);
   
   // Instance Members
   
   private RemoteBinding binding;
   
   private InvokerLocator locator;
   
   // Constructor
   public BaseStatefulRemoteProxyFactory(SessionSpecContainer container, RemoteBinding binding)
   {
      super(container, binding.jndiBinding());
      
      this.binding = binding;
      
      try
      {
         String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(this.getBinding());
         this.locator = new InvokerLocator(clientBindUrl);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   // Required Implementations

   @Override
   protected ProxyAccessType getProxyAccessType()
   {
      return ProxyAccessType.REMOTE;
   }

   @Override
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
   
   public Object createProxyBusiness()
   {
      Object id = getContainer().createSession();
      return this.createProxyBusiness(id);
   }
   
   public Object createProxyBusiness(Object id)
   {
      return this.createProxy(id,SpecificationInterfaceType.EJB30_BUSINESS);
   }
   
   // Specifications
   
   abstract String getStackNameInterceptors();
   
   // Functional Methods 
   
   @Override
   protected boolean bindHomeAndBusinessTogether()
   {
      SessionSpecContainer container = this.getContainer();
      String homeJndiName = ProxyFactoryHelper.getHomeJndiName(container);
      String remoteBusinessJndiName = ProxyFactoryHelper.getRemoteBusinessJndiName(container);
      return homeJndiName.equals(remoteBusinessJndiName);
   }
   
   Object createProxy(Object id,SpecificationInterfaceType type)
   {
      String stackName = this.getStackNameInterceptors();
      RemoteBinding binding = this.getBinding();
      if (binding.interceptorStack() != null && !binding.interceptorStack().trim().equals(""))
      {
         stackName = binding.interceptorStack();
      }
      AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
      if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
      StatefulRemoteProxy proxy = new StatefulRemoteProxy(getContainer(), stack.createInterceptors(getContainer()
            .getAdvisor(), null), this.getLocator(), id);
      
      if(type.equals(SpecificationInterfaceType.EJB21))
      {
         this.getContainer();
         return this.constructEjb21Proxy(proxy);
      }
      else
      {
         return this.constructProxyBusiness(proxy);
      }
   }
   
   @Override
   protected StatefulHandleRemoteImpl createHandle()
   {
      EJBObject proxy = this.createProxyEjb21();
      return this.createHandle(proxy);
   }
   
   protected StatefulHandleRemoteImpl createHandle(EJBObject proxy)
   {
      StatefulHandleRemoteImpl handle = new StatefulHandleRemoteImpl(proxy);
      return handle;
   } 
   
   public EJBObject createProxyEjb21()
   {
      Object id = getContainer().createSession();
      return this.createProxyEjb21(id);
   }
   
   @SuppressWarnings("unchecked")
   public <T extends EJBObject> T createProxyEjb21(Object id)
   {      
      // Cast explicitly to catch improper proxies
      return (T)this.createProxy(id,SpecificationInterfaceType.EJB21);
   }
   
   @Override
   public void start() throws Exception
   {
      super.start();
   }

   // Accessors / Mutators
   
   RemoteBinding getBinding()
   {
      assert this.binding!=null : "RemoteBinding has not been initialized";
      return this.binding;
   }
   
   InvokerLocator getLocator()
   {
      assert this.locator!=null : "InvokerLocator has not been initialized"; 
      return this.locator;
   }

}
