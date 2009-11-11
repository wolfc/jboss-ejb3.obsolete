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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;

import org.jboss.aop.Advisor;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructionInvocation;
import org.jboss.ejb3.interceptors.InterceptorFactory;
import org.jboss.ejb3.interceptors.InterceptorFactoryRef;
import org.jboss.ejb3.interceptors.aop.LifecycleCallbacks;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.container.DummyBeanContext;
import org.jboss.ejb3.interceptors.container.SimpleBeanContextFactory;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InjectingBeanContextFactory extends SimpleBeanContextFactory<InterceptorContainer, InterceptorContainerContainer>
{
   private static final Logger log = Logger.getLogger(InjectingBeanContextFactory.class);
   
   private InterceptorContainerContainer container;

   public BeanContext<InterceptorContainer> createBean() throws Exception
   {
      try
      {
         ClassAdvisor advisor = container.getAdvisor1();
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
         
         Constructor<InterceptorContainer> constructor = advisor.getClazz().getConstructor();
         int idx = advisor.getConstructorIndex(constructor);
         Object initargs[] = null;
         InterceptorContainer targetObject = container.getBeanClass().cast(advisor.invokeNew(initargs, idx));
         
         BeanContext<InterceptorContainer> component = new DummyBeanContext<InterceptorContainer>(targetObject, ejb3Interceptors);
         
         // FIXME: here be true injection
         InitialContext ctx = new InitialContext();
         Class<?> beanClass = (Class<?>) ctx.lookup("java:comp/env/beanClass");
         component.getInstance().setBeanClass(beanClass);
         
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

   /*
   public void destroyBean(BeanContext<InterceptorContainer> component)
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
   */
   
   private Interceptor[] createLifecycleInterceptors(BeanContext<InterceptorContainer> component, Class<? extends Annotation> lifecycleAnnotationType) throws Exception
   {
      List<Class<?>> lifecycleInterceptorClasses = container.getInterceptorRegistry().getLifecycleInterceptorClasses();
      Advisor advisor = container.getAdvisor1();
      return LifecycleCallbacks.createLifecycleCallbackInterceptors(advisor, lifecycleInterceptorClasses, component, lifecycleAnnotationType);
   }
   
   public void setContainer(InterceptorContainerContainer container)
   {
      this.container = container;
      super.setContainer(container);
   }
}
