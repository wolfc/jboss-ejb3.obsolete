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
package org.jboss.ejb3.interceptors.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.advice.PerVmAdvice;
import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.aop.joinpoint.ConstructionInvocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.interceptors.InterceptorFactoryRef;
import org.jboss.ejb3.interceptors.annotation.AnnotationAdvisor;
import org.jboss.ejb3.interceptors.annotation.AnnotationAdvisorSupport;
import org.jboss.ejb3.interceptors.aop.InterceptorsFactory;
import org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.logging.Logger;

/**
 * The base of all containers. Provides functions to allow for object
 * construction and invocation with interception.
 * 
 * Note that it's up to the actual implementation to expose any methods.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public abstract class AbstractContainer<T, C extends AbstractContainer<T, C>> extends AnnotationAdvisorSupport implements AnnotationAdvisor
{
   private static final Logger log = Logger.getLogger(AbstractContainer.class);
   
   private ManagedObjectAdvisor<T, C> advisor;
   
   /**
    * For a completely customized startup.
    * 
    * Note that before construction ends advisor must be set!
    */
   protected AbstractContainer()
   {
      
   }
   
   protected AbstractContainer(String name, Domain domain, Class<? extends T> beanClass)
   {
      initializeAdvisor(name, domain, beanClass);
   }
   
   protected AbstractContainer(String name, String domainName, Class<? extends T> beanClass)
   {
      this(name, getDomain(domainName), beanClass);
   }
   
   protected T construct(Constructor<? extends T> constructor, Object ... initargs)
   {
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
   
   protected Object createInterceptor(Class<?> interceptorClass) throws InstantiationException, IllegalAccessException
   {
      return interceptorClass.newInstance();
   }
   
   protected void destroy(T bean)
   {
      try
      {
         // TODO: speed up
         List<Interceptor> interceptors = new ArrayList<Interceptor>(InterceptorsFactory.getPreDestroys(advisor));
         interceptors.add(0, PerVmAdvice.generateInterceptor(null, new InvocationContextInterceptor(), "setup"));
         
         DestructionInvocation invocation = new DestructionInvocation(interceptors.toArray(new Interceptor[0]));
         invocation.setAdvisor(advisor);
         invocation.setTargetObject(bean);
         invocation.invokeNext();
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
    * Finalize construction of the abstract container by setting the advisor.
    * 
    * @param name       the name of the advisor
    * @param domain     the domain for the advisor
    * @param beanClass  the class being advised
    */
   protected void initializeAdvisor(String name, Domain domain, Class<? extends T> beanClass)
   {
      initializeAdvisor(name, domain, beanClass, null);
   }
   
   protected void initializeAdvisor(String name, Domain domain, Class<? extends T> beanClass, AnnotationRepository annotations)
   {
      if(this.advisor != null) throw new IllegalStateException("advisor already set to " + advisor);
      
      assert name != null : "name is null";
      assert domain != null : "domain is null";
      assert beanClass != null : "beanClass is null";
      
      // Decouple setting the advisor and initializing it, so interceptors
      // can get it.
      this.advisor = new ManagedObjectAdvisor<T, C>((C) this, name, domain, annotations);
      advisor.getAnnotations().addClassAnnotation(InterceptorFactoryRef.class, new InterceptorFactoryRefImpl(ContainerInterceptorFactory.class));
      advisor.initialize(beanClass);
   }
   
   protected final ManagedObjectAdvisor<T, C> getAdvisor()
   {
      if(advisor == null) throw new IllegalStateException("advisor has not been initialized");
      return advisor;
   }
   
   @SuppressWarnings("unchecked")
   protected Class<? extends T> getBeanClass()
   {
      return getAdvisor().getClazz();
   }
   
   @SuppressWarnings("unchecked")
   public static <C extends AbstractContainer<?, ?>> C getContainer(Advisor advisor)
   {
      return (C) ((ManagedObjectAdvisor) advisor).getContainer();
   }
   
   /*
    * TODO: this should not be here, it's an AspectManager helper function.
    */
   protected static final Domain getDomain(String domainName)
   {
      DomainDefinition domainDefinition = AspectManager.instance().getContainer(domainName);
      if(domainDefinition == null)
         throw new IllegalArgumentException("Domain definition '" + domainName + "' can not be found");
      
      final Domain domain = (Domain) domainDefinition.getManager();
      return domain;
   }
   
   /**
    * Call a method upon a target object with all interceptors in place.
    * 
    * @param target     the target to invoke upon
    * @param method     the method to invoke
    * @param arguments  arguments to the method
    * @return           return value of the method
    * @throws Throwable if anything goes wrong
    */
   protected Object invoke(T target, Method method, Object arguments[]) throws Throwable
   {
      long methodHash = MethodHashing.calculateHash(method);
      MethodInfo info = getAdvisor().getMethodInfo(methodHash);
      if(info == null)
         throw new IllegalArgumentException("method " + method + " is not under advisement by " + this);
      MethodInvocation invocation = new MethodInvocation(info, info.getInterceptors());
      invocation.setArguments(arguments);
      invocation.setTargetObject(target);
      return invocation.invokeNext();
   }
   
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
   protected <R> R invoke(T target, String methodName, Object ... args) throws Throwable
   {
      Method method = ClassHelper.getMethod(target.getClass(), methodName);
      return (R) invoke(target, method, args);
   }
}
