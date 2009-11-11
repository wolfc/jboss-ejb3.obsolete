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
package org.jboss.ejb3.sandbox.interceptorcontainer;

import java.lang.reflect.Method;

import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;
import org.jboss.ejb3.interceptors.direct.AbstractDirectContainer;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorContainerContainer extends AbstractDirectContainer<InterceptorContainer, InterceptorContainerContainer>
{
   /**
    * @param name
    * @param domainName
    * @param beanClass
    */
   public InterceptorContainerContainer(String name, String domainName, Class<? extends InterceptorContainer> beanClass)
   {
      super(name, domainName, beanClass);
   }

   protected final ClassAdvisor getAdvisor1()
   {
      return super.getAdvisor();
   }
   
   /**
    * Do not call, for use in indirect container implementations.
    * 
    * FIXME: bug in AbstractDirectContainer, should use ContainerMethodInvocation
    */
   public Object invokeIndirect(BeanContext<InterceptorContainer> target, Method method, Object arguments[]) throws Throwable
   {
      long methodHash = MethodHashing.calculateHash(method);
      MethodInfo info = getAdvisor().getMethodInfo(methodHash);
      if(info == null)
         throw new IllegalArgumentException("method " + method + " is not under advisement by " + this);
      ContainerMethodInvocation invocation = new ContainerMethodInvocation(info, info.getInterceptors())
      {
         @Override
         public Object invokeTarget() throws Throwable
         {
            // TODO: invoke the real target in special modus
            return null;
         }
      };
      invocation.setArguments(arguments);
      invocation.setBeanContext(target);
      return invocation.invokeNext();
   }
}
