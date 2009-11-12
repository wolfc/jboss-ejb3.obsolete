/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptors.test.indirectcontainer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.interceptor.Interceptors;

import org.jboss.ejb3.interceptors.ManagedObject;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.IndirectContainer;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Interceptors(DummyInterceptor.class)
@ManagedObject
public class DummyIndirectContainer implements IndirectContainer<DummyIndirectContainer, DummyContainerContainer>, InvocationHandler
{
   private DummyContainerContainer directContainer;
   private BeanContext<DummyIndirectContainer> beanContext;
   
   private static final Method INVOKE_METHOD;
   
   static
   {
      try
      {
         INVOKE_METHOD = InvocationHandler.class.getDeclaredMethod("invoke", new Class<?>[] { Object.class, Method.class, new Object[0].getClass() });
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void setBeanContext(BeanContext<DummyIndirectContainer> interceptorContainer)
   {
      this.beanContext = interceptorContainer;
   }
   
   public void setDirectContainer(DummyContainerContainer container)
   {
      this.directContainer = container;
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // I'm indirectly advised, let's delegate to the direct container
      Object arguments[] = { method, args };
      return directContainer.invokeIndirect(beanContext, INVOKE_METHOD, arguments);
   }
}
