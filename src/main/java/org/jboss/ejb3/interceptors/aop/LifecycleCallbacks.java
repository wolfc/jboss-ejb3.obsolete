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
package org.jboss.ejb3.interceptors.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.advice.PerVmAdvice;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.lang.ClassHelper;

/**
 * The common logic for lifecycle callbacks.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class LifecycleCallbacks
{
   public static Interceptor[] createLifecycleCallbackInterceptors(Advisor advisor, List<Class<?>> lifecycleInterceptorClasses, BeanContext<?> component, Class<? extends Annotation> lifecycleAnnotationType) throws Exception
   {
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
      Class<?> beanClass = advisor.getClazz();
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
}
