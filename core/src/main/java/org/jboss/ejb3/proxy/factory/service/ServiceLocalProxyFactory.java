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
package org.jboss.ejb3.proxy.factory.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.proxy.JBossProxy;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.handler.service.ServiceLocalProxyInvocationHandler;
import org.jboss.ejb3.service.ServiceContainer;


/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class ServiceLocalProxyFactory extends BaseServiceProxyFactory
{
   public ServiceLocalProxyFactory(ServiceContainer container, LocalBinding binding)
   {
      super(container, binding.jndiBinding());
   }

   protected Class<?>[] getInterfaces()
   {
      // Initialize
      Set<Class<?>> uniqueInterfaces = new HashSet<Class<?>>(); 
      
      // Obtain interfaces and add as unique
      uniqueInterfaces.addAll(Arrays.asList(ProxyFactoryHelper.getLocalAndBusinessLocalInterfaces(container))) ;
      
      // Add JBossProxy
      uniqueInterfaces.add(JBossProxy.class);
      
      // Return
      return uniqueInterfaces.toArray(new Class<?>[]{});
   }

   public Object createProxyBusiness()
   {
      try
      {
         Object[] args = {new ServiceLocalProxyInvocationHandler(container)};
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
