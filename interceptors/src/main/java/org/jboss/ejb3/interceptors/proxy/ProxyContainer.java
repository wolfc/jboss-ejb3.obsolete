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

import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.ConstructionInfo;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructionInvocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.interceptors.proxy.aop.ManagedObjectContainer;
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
public class ProxyContainer
{
   private static final Logger log = Logger.getLogger(ProxyContainer.class);
   
   private Advisor advisor;
   
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
   
   public ProxyContainer(String name, Domain domain, Class<?> beanClass)
   {
      assert domain != null : "domain is null";
      assert beanClass != null : "beanClass is null";
      
      ManagedObjectContainer delegate = new ManagedObjectContainer(name, domain, beanClass);
      this.advisor = delegate;
   }
   
   public ProxyContainer(String name, String domainName, Class<?> beanClass)
   {
      this(name, getDomain(domainName), beanClass);
   }
   
   @SuppressWarnings("unchecked")
   public <I> I constructProxy(Class<?> interfaces[]) throws Throwable
   {
      // assert interfaces contains I
      Object args[] = null;
      int idx = 0; // TODO: find default constructor
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
   
   /*
    * TODO: this should not be here, it's an AspectManager helper function.
    */
   private static final Domain getDomain(String domainName)
   {
      DomainDefinition domainDefinition = AspectManager.instance().getContainer(domainName);
      if(domainDefinition == null)
         throw new IllegalArgumentException("Domain definition '" + domainName + "' can not be found");
      
      final Domain domain = (Domain) domainDefinition.getManager();
      return domain;
   }
   
   protected Object invoke(Object target, Method method, Object arguments[]) throws Throwable
   {
      long methodHash = MethodHashing.calculateHash(method);
      MethodInfo info = advisor.getMethodInfo(methodHash);
      MethodInvocation invocation = new MethodInvocation(info, info.getInterceptors());
      invocation.setArguments(arguments);
      invocation.setTargetObject(target);
      return invocation.invokeNext();
   }
}
