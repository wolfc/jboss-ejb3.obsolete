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
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.interceptors.container.AbstractContainer;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.ejb3.interceptors.registry.InterceptorRegistry;
import org.jboss.logging.Logger;

/**
 * The direct container invokes interceptors directly on an instance.
 * 
 * It's useful in an environment where we don't want to fiddle with the
 * classloader and still have control on how instances are called.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public abstract class AbstractDirectContainer<T, C extends AbstractDirectContainer<T, C>> extends AbstractContainer<T, C>
{
   private static final Logger log = Logger.getLogger(AbstractDirectContainer.class);
   
   protected AbstractDirectContainer()
   {
      
   }
   
   protected AbstractDirectContainer(String name, Domain domain, Class<? extends T> beanClass)
   {
      super(name, domain, beanClass);
   }
   
   protected AbstractDirectContainer(String name, String domainName, Class<? extends T> beanClass)
   {
      super(name, domainName, beanClass);
   }
   
   public BeanContext<T> construct() throws SecurityException, NoSuchMethodException
   {
      return construct((Object[]) null, null);
   }
   
   @SuppressWarnings("unchecked")
   public BeanContext<T> construct(Object initargs[], Class<?> parameterTypes[]) throws SecurityException, NoSuchMethodException
   {
      ClassAdvisor advisor = getAdvisor();
      Constructor<T> constructor = advisor.getClazz().getConstructor(parameterTypes);
      BeanContext<T> targetObject = construct(constructor, initargs);
      
      // If we're advising an indirect container make it aware of this
      if(targetObject.getInstance() instanceof IndirectContainer)
         ((IndirectContainer<T, C>) targetObject.getInstance()).setDirectContainer((C) this);
      
      return targetObject;
   }
   
   @Override
   public void destroy(BeanContext<T> bean)
   {
      super.destroy(bean);
   }
   
   /**
    * Do not call, for use in indirect container implementations.
    * @return
    */
   public Class<? extends T> getBeanClass()
   {
      return super.getBeanClass();
   }
   
   // expose the interceptor registry
   @Override
   public InterceptorRegistry getInterceptorRegistry()
   {
      return super.getInterceptorRegistry();
   }
   
   // expose the invoke method
   @Override
   public Object invoke(BeanContext<T> target, Method method, Object[] arguments) throws Throwable
   {
      return super.invoke(target, method, arguments);
   }
   
   // the compiler won't allow me to expose the super method
   /**
    * A convenient, but unchecked and slow method to call a method upon a target.
    * 
    * (Slow method)
    * 
    * @param <R>        the return type
    * @param target     the target to invoke upon
    * @param methodName the method name to invoke
    * @param args       the arguments to the method
    * @return           the return value
    * @throws Throwable if anything goes wrong
    */
   @SuppressWarnings("unchecked")
   public <R> R invoke(BeanContext<T> target, String methodName, Object ... args) throws Throwable
   {
      Method method;
      if(args != null)
      {
         Class<?> params[] = new Class<?>[args.length];
         for(int i = 0; i < params.length; i++)
            params[i] = args[i].getClass();
         method = ClassHelper.getMethod(target.getInstance().getClass(), methodName, params);
      }
      else
         method = ClassHelper.getMethod(target.getInstance().getClass(), methodName);
      return (R) invoke(target, method, args);
   }
   
   /**
    * Do not call, for use in indirect container implementations.
    */
   public Object invokeIndirect(BeanContext<T> target, Method method, Object arguments[]) throws Throwable
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
