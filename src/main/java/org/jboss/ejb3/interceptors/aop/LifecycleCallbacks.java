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
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.advice.PerVmAdvice;
import org.jboss.aspects.currentinvocation.CurrentInvocationInterceptor;
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
      
      // TODO: these should come from aop.xml
      interceptors.add(new CurrentInvocationInterceptor());
      interceptors.add(PerVmAdvice.generateInterceptor(null, new InvocationContextInterceptor(), "setup"));
      
      // 12.7 footnote 57: ignore method level interceptors
      // The lifecycle callbacks on the interceptors must be invoked in order
      for(Class<?> interceptorClass : lifecycleInterceptorClasses)
      {
         Object interceptor = component.getInterceptor(interceptorClass);
         ExtendedAdvisor interceptorAdvisor = ExtendedAdvisorHelper.getExtendedAdvisor(advisor, interceptor);
         HashSet<Class<?>> classes = null;
         for(Method interceptorMethod : ClassHelper.getAllMethods(interceptorClass))
         {
            if (!ClassHelper.isOverridden(interceptorClass, interceptorMethod))
            {
               //Only a candidate for a lifecycle method if not overridden 
               if(interceptorAdvisor.isAnnotationPresent(interceptorClass, interceptorMethod, lifecycleAnnotationType)) //For Xml this returns true sometimes
               {
                  classes = checkClass(classes, interceptorMethod, advisor, lifecycleAnnotationType);  
                  interceptors.add(new LifecycleCallbackInterceptorMethodInterceptor(interceptor, interceptorMethod));
               }
            }
         }
      }
      
      // Bean lifecycle callbacks
      Class<?> beanClass = advisor.getClazz();
      HashSet<Class<?>> classes = null;
      for(Method beanMethod : ClassHelper.getAllMethods(beanClass))
      {
         if (!ClassHelper.isOverridden(beanClass, beanMethod))
         {
            if(advisor.hasAnnotation(beanMethod, lifecycleAnnotationType))
            {
               classes = checkClass(classes, beanMethod, advisor, lifecycleAnnotationType);  
               interceptors.add(new LifecycleCallbackBeanMethodInterceptor(beanMethod));
            }
         }
      }
      
      return interceptors.toArray(new Interceptor[0]);
   }
   
   private static HashSet<Class<?>> checkClass(HashSet<Class<?>> classes, Method m, Advisor advisor, Class<? extends Annotation> lifecycleAnnotationType)
   {
      if (classes != null)
      {
         if (classes.contains(m.getDeclaringClass()))
         {
            String type = null;
            if (lifecycleAnnotationType == PostConstruct.class)
            {
               type = "post-construct";
            }
            else if (lifecycleAnnotationType == PreDestroy.class)
            {
               type = "pre-destroy";
            } 
            else if (lifecycleAnnotationType == PostActivate.class)
            {
               type = "post-activate";
            }
            else if (lifecycleAnnotationType == PrePassivate.class)
            {
               type = "pre-passivate";
            }         
            throw new RuntimeException("More than one '" + type + "' method in " + advisor.getName());
         }
      }
      else
      {
         classes = new HashSet<Class<?>>();
      }
      classes.add(m.getDeclaringClass());
      return classes;
   }
}
