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
package org.jboss.ejb3.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.remoting.InvokerLocator;


/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class ServiceRemoteProxyFactory extends BaseServiceProxyFactory implements RemoteProxyFactory
{
   private RemoteBinding binding;
   private InvokerLocator locator;

   protected ServiceRemoteProxyFactory(ServiceContainer container, RemoteBinding binding)
   {
      super(container, binding.jndiBinding());
      
      this.binding = binding;
   }

//   public void setRemoteBinding(RemoteBinding binding)
//   {
//      this.binding = binding;
//   }

   protected Class<?>[] getInterfaces()
   {
      // Initialize
      Set<Class<?>> uniqueInterfaces = new HashSet<Class<?>>();

      // Obtain interfaces and add as unique
      uniqueInterfaces.addAll(Arrays.asList(ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(container)));

      // Add JBossProxy
      uniqueInterfaces.add(JBossProxy.class);
      
      // Return
      return uniqueInterfaces.toArray(new Class<?>[]{});
   }

   public void start() throws Exception
   {
      String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(binding);
      locator = new InvokerLocator(clientBindUrl);
      super.start();
   }

   public Object createProxyBusiness()
   {
      try
      {
         String stackName = "ServiceClientInterceptors";
         if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
         {
            stackName = binding.interceptorStack();
         }
         AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
         Object[] args = {new ServiceRemoteProxy(container, stack.createInterceptors(container.getAdvisor(), null), locator)};
         return proxyConstructor.newInstance(args);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getTargetException());
      }
   }

}
