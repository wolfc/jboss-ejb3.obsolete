/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptors.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.ConstructionInfo;
import org.jboss.aop.Domain;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructionInvocation;
import org.jboss.ejb3.interceptors.container.AbstractContainer;
import org.jboss.logging.Logger;

/**
 * An interceptor proxy container.
 * 
 * Decouple AOP from EJB3, so Advisor methods are hidden and the
 * Advisor life-cycle is controlled.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ProxyContainer<T> extends AbstractContainer<T, ProxyContainer<T>>
{
   private static final Logger log = Logger.getLogger(ProxyContainer.class);
   
   private class ProxyInvocationHandler implements InvocationHandler
   {
      private Object target;
      
      public ProxyInvocationHandler(Object target)
      {
         assert target != null : "target is null";
         
         this.target = target;
      }
      
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         return ProxyContainer.this.invoke(target, method, args);
      }
   }
   
   public ProxyContainer(String name, Domain domain, Class<? extends T> beanClass)
   {
      super(name, domain, beanClass);
   }
   
   public ProxyContainer(String name, String domainName, Class<? extends T> beanClass)
   {
      super(name, domainName, beanClass);
   }
   
   @SuppressWarnings("unchecked")
   public <I> I constructProxy(Class<?> interfaces[]) throws Throwable
   {
      // assert interfaces contains I
      Object args[] = null;
      int idx = 0; // TODO: find default constructor
      ClassAdvisor advisor = getAdvisor();
      // ClassAdvisor
      ConstructionInfo constructionInfo = advisor.getConstructionInfos()[idx];
      Interceptor[] cInterceptors = constructionInfo.getInterceptors();
      if (cInterceptors == null) cInterceptors = new Interceptor[0];
      log.debug("constructor interceptors " + Arrays.toString(cInterceptors));
      Constructor<?> constructor = advisor.getConstructors()[idx];
      ConstructionInvocation invocation = new ConstructionInvocation(cInterceptors, constructor);
      
      invocation.setAdvisor(advisor);
      invocation.setArguments(args);
      // First we create the instance
      Object instance = constructor.newInstance();
      invocation.setTargetObject(instance);
      // then we do (construction) interception
      invocation.invokeNext();
      
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      //Class<?> interfaces[] = { intf };
      Object proxy = Proxy.newProxyInstance(loader, interfaces, new ProxyInvocationHandler(instance));
      return (I) proxy;
   }
}
