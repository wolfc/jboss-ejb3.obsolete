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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jboss.aop.Advisor;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.advice.PerVmAdvice;
import org.jboss.aop.joinpoint.ConstructionInvocation;
import org.jboss.ejb3.interceptors.InterceptorFactory;
import org.jboss.ejb3.interceptors.InterceptorFactoryRef;
import org.jboss.ejb3.interceptors.aop.ExtendedAdvisor;
import org.jboss.ejb3.interceptors.aop.ExtendedAdvisorHelper;
import org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor;
import org.jboss.ejb3.interceptors.aop.LifecycleCallbackBeanMethodInterceptor;
import org.jboss.ejb3.interceptors.aop.LifecycleCallbackInterceptorMethodInterceptor;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleBeanContextFactory<T, C extends AbstractContainer<T, C>> implements BeanContextFactory<T, C>
{
   private static final Logger log = Logger.getLogger(SimpleBeanContextFactory.class);
   
   private AbstractContainer<T, C> container;
   
   public BeanContext<T> createBean() throws Exception
   {
      try
      {
         ClassAdvisor advisor = container.getAdvisor();
         InterceptorFactoryRef interceptorFactoryRef = (InterceptorFactoryRef) advisor.resolveAnnotation(InterceptorFactoryRef.class);
         if(interceptorFactoryRef == null)
            throw new IllegalStateException("No InterceptorFactory specified on " + advisor.getName());
         log.debug("interceptor factory class = " + interceptorFactoryRef.value());
         InterceptorFactory interceptorFactory = interceptorFactoryRef.value().newInstance();
         
         List<Object> ejb3Interceptors = new ArrayList<Object>();
         for(Class<?> interceptorClass : container.getInterceptorRegistry().getInterceptorClasses())
         {
            Object interceptor = interceptorFactory.create(advisor, interceptorClass);
            ejb3Interceptors.add(interceptor);
         }
         
         Constructor<T> constructor = advisor.getClazz().getConstructor();
         int idx = advisor.getConstructorIndex(constructor);
         Object initargs[] = null;
         T targetObject = container.getBeanClass().cast(advisor.invokeNew(initargs, idx));
         
         BeanContext<T> component = new DummyBeanContext<T>(targetObject, ejb3Interceptors);
         
         // Do lifecycle callbacks
         Interceptor interceptors[] = createLifecycleInterceptors(component, PostConstruct.class);
         
         ConstructionInvocation invocation = new ConstructionInvocation(interceptors, constructor, initargs);
         invocation.setAdvisor(advisor);
         invocation.setTargetObject(targetObject);
         invocation.invokeNext();
         
         return component;
      }
      catch(Error e)
      {
         throw e;
      }
      catch(Throwable t)
      {
         // TODO: decompose
         throw new RuntimeException(t);
      }
   }

   public void destroyBean(BeanContext<T> component)
   {
      try
      {
         Advisor advisor = container.getAdvisor();
         Interceptor interceptors[] = createLifecycleInterceptors(component, PreDestroy.class);
         
         DestructionInvocation invocation = new DestructionInvocation(interceptors);
         invocation.setAdvisor(advisor);
         invocation.setTargetObject(component.getInstance());
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
   
   private Interceptor[] createLifecycleInterceptors(BeanContext<T> component, Class<? extends Annotation> lifecycleAnnotationType) throws Exception
   {
      List<Class<?>> lifecycleInterceptorClasses = container.getInterceptorRegistry().getLifecycleInterceptorClasses();
      Advisor advisor = container.getAdvisor();
      List<Interceptor> interceptors = new ArrayList<Interceptor>();
      Object ejb3Interceptors[] = component.getInterceptors();
      for(Object interceptor : ejb3Interceptors)
      {
         // 12.7 footnote 57: ignore method level interceptors
         Class<?> interceptorClass = interceptor.getClass();
         if(!lifecycleInterceptorClasses.contains(interceptorClass))
            continue;
         ExtendedAdvisor interceptorAdvisor = ExtendedAdvisorHelper.getExtendedAdvisor(advisor, interceptor);
         for(Method interceptorMethod : ClassHelper.getAllMethods(interceptorClass))
         {
            if(interceptorAdvisor.isAnnotationPresent(interceptorClass, interceptorMethod, lifecycleAnnotationType))
            {
               interceptors.add(new LifecycleCallbackInterceptorMethodInterceptor(interceptor, interceptorMethod));
            }
         }
      }
      Class<?> beanClass = container.getBeanClass();
      for(Method beanMethod : ClassHelper.getAllMethods(beanClass))
      {
         if(advisor.hasAnnotation(beanMethod, lifecycleAnnotationType))
         {
            interceptors.add(new LifecycleCallbackBeanMethodInterceptor(beanMethod));
         }
      }
      interceptors.add(0, PerVmAdvice.generateInterceptor(null, new InvocationContextInterceptor(), "setup"));
      
      return interceptors.toArray(new Interceptor[0]);
   }
   
   public void setContainer(AbstractContainer<T, C> container)
   {
      this.container = container;
   }
}
