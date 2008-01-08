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
package org.jboss.ejb3.interceptors.direct;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructionInvocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.interceptors.container.AbstractContainer;
import org.jboss.logging.Logger;

/**
 * The direct container invokes interceptors directly on an instance.
 * 
 * It's useful in an environment where we don't want to fiddle with the
 * classloader and still have control on how instances are called.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DirectContainer<T> extends AbstractContainer<T>
{
   private static final Logger log = Logger.getLogger(DirectContainer.class);
   
   public DirectContainer(String name, Domain domain, Class<? extends T> beanClass)
   {
      super(name, domain, beanClass);
   }
   
   public DirectContainer(String name, String domainName, Class<? extends T> beanClass)
   {
      super(name, domainName, beanClass);
   }
   
   public T construct() throws SecurityException, NoSuchMethodException
   {
      return construct(null, null);
   }
   
   @SuppressWarnings("unchecked")
   public T construct(Object initargs[], Class<?> parameterTypes[]) throws SecurityException, NoSuchMethodException
   {
      ClassAdvisor advisor = getAdvisor();
      Constructor<T> constructor = advisor.getClazz().getConstructor(parameterTypes);
      int idx = advisor.getConstructorIndex(constructor);
      assert idx != -1 : "can't find constructor in the advisor";
      try
      {
         T targetObject = (T) advisor.invokeNew(initargs, idx);
         
         Interceptor interceptors[] = advisor.getConstructionInfos()[idx].getInterceptors();
         ConstructionInvocation invocation = new ConstructionInvocation(interceptors, constructor, initargs);
         invocation.setAdvisor(advisor);
         invocation.setTargetObject(targetObject);
         invocation.invokeNext();
         
         if(targetObject instanceof IndirectContainer)
            ((IndirectContainer<T>) targetObject).setDirectContainer(this);
         
         return targetObject;
      }
      catch(Throwable t)
      {
         // TODO: disect
         if(t instanceof RuntimeException)
            throw (RuntimeException) t;
         throw new RuntimeException(t);
      }
   }
   
   /**
    * Do not call, for use in indirect container implementations.
    * @return
    */
   public Class<?> getBeanClass()
   {
      return getAdvisor().getClazz();
   }
   
   // FIXME: copy of ProxyContainer.invoke
   public Object invokeIndirect(Object target, Method method, Object arguments[]) throws Throwable
   {
      long methodHash = MethodHashing.calculateHash(method);
      MethodInfo info = getAdvisor().getMethodInfo(methodHash);
      if(info == null)
         throw new IllegalArgumentException("method " + method + " is not under advisement by " + this);
      MethodInvocation invocation = new MethodInvocation(info, info.getInterceptors())
      {
         @Override
         public Object invokeTarget() throws Throwable
         {
            // TODO: invoke the real target in special modus
            return null;
         }
      };
      invocation.setArguments(arguments);
      invocation.setTargetObject(target);
      return invocation.invokeNext();
   }
}
