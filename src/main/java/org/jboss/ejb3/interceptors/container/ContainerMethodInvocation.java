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
package org.jboss.ejb3.interceptors.container;

import java.lang.reflect.Method;

import org.jboss.aop.Advisor;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * A method invocation on a intercepting container.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ContainerMethodInvocation extends MethodInvocation
{
   private static final long serialVersionUID = 1L;
   
   private BeanContext<?> beanContext;
   
   /**
    * Meant for use in the EJB container, which does a late beanContext
    * association using an instance interceptor.
    */
   protected ContainerMethodInvocation(MethodInfo info)
   {
      super(info, info.getInterceptors());
   }
   
   protected ContainerMethodInvocation(Interceptor[] interceptors, long methodHash, Method advisedMethod, Method unadvisedMethod, Advisor advisor)
   {
      super(interceptors, methodHash, advisedMethod, unadvisedMethod, advisor);
   }
   
   protected ContainerMethodInvocation()
   {
      super();
   }

   protected ContainerMethodInvocation(MethodInfo info, Interceptor[] interceptors)
   {
      super(info, interceptors);
   }

   ContainerMethodInvocation(MethodInfo info, BeanContext<?> beanContext, Object arguments[])
   {
      super(info, info.getInterceptors());
      
      assert beanContext != null : "beanContext is null";
      
      setArguments(arguments);
      setBeanContext(beanContext);
   }
   
   /**
    * @param newchain
    */
   protected ContainerMethodInvocation(Interceptor[] newchain)
   {
      super(newchain);
   }

   public BeanContext<?> getBeanContext()
   {
      return beanContext;
   }
   
   public static ContainerMethodInvocation getContainerMethodInvocation(Invocation invocation)
   {
      if(invocation instanceof ContainerMethodInvocation)
         return (ContainerMethodInvocation) invocation;
      throw new IllegalArgumentException("invocation " + invocation + " is not done through AbstractContainer");
   }
   
   @Override
   public Invocation getWrapper(Interceptor[] newchain)
   {
      return new ContainerMethodInvocationWrapper(this, newchain);
   }
   
   /**
    * @param beanContext the beanContext to set or null for disassociation
    */
   public void setBeanContext(BeanContext<?> beanContext)
   {
      this.beanContext = beanContext;
      if(beanContext != null)
         super.setTargetObject(beanContext.getInstance());
      else
         super.setTargetObject(null);
   }
   
   @Override
   public void setTargetObject(Object targetObject)
   {
      throw new RuntimeException("Call setBeanContext, not setTargetObject");
   }
}
