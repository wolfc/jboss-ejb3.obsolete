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
package org.jboss.ejb3.interceptors.registry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.interceptors.aop.annotation.DefaultInterceptors;
import org.jboss.ejb3.interceptors.aop.annotation.InterceptorOrder;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.logging.Logger;

/**
 * The interceptor registry for a given EJB.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorRegistry
{
   private static final Logger log = Logger.getLogger(InterceptorRegistry.class);
   
   private Advisor advisor;
   
   private List<Class<?>> interceptorClasses = new ArrayList<Class<?>>();
   private List<Class<?>> readOnlyInterceptorClasses = Collections.unmodifiableList(interceptorClasses);
   
   /**
    * Interceptors who are interested in lifecycle callbacks.
    */
   private List<Class<?>> lifecycleInterceptorClasses = new ArrayList<Class<?>>();
   private List<Class<?>> readOnlyLifecycleInterceptorClasses = Collections.unmodifiableList(lifecycleInterceptorClasses);
   
   private Map<Method, List<Class<?>>> applicableInterceptorClasses = new HashMap<Method, List<Class<?>>>();
   
   public InterceptorRegistry(Advisor advisor)
   {
      assert advisor != null : "advisor is null";
      
      this.advisor = advisor;
      
      initialize();
   }
   
   public List<Class<?>> getApplicableInterceptorClasses(Method method)
   {
      List<Class<?>> methodApplicableInterceptorClasses = applicableInterceptorClasses.get(method);
      assert methodApplicableInterceptorClasses != null : "applicable interceptors is non-existent for " + method;
      return methodApplicableInterceptorClasses;
   }
   
   public List<Class<?>> getInterceptorClasses()
   {
      return readOnlyInterceptorClasses;
   }
   
   /**
    * All default and class interceptors (not method interceptors (12.7 footnote 57)
    * @return
    */
   public List<Class<?>> getLifecycleInterceptorClasses()
   {
      return readOnlyLifecycleInterceptorClasses;
   }
   
   private void initialize()
   {
      DefaultInterceptors defaultInterceptorsAnnotation = (DefaultInterceptors) advisor.resolveAnnotation(DefaultInterceptors.class);
      List<Class<?>> defaultInterceptorClasses = new ArrayList<Class<?>>();
      if(defaultInterceptorsAnnotation != null)
      {
         for(Class<?> defaultInterceptorClass : defaultInterceptorsAnnotation.value())
            defaultInterceptorClasses.add(defaultInterceptorClass);
      }
      log.debug("Found default interceptors " + defaultInterceptorClasses);
      interceptorClasses.addAll(defaultInterceptorClasses);
      lifecycleInterceptorClasses.addAll(defaultInterceptorClasses);
      
      Interceptors interceptorsAnnotation = (Interceptors) advisor.resolveAnnotation(Interceptors.class);
      List<Class<?>> classInterceptorClasses = new ArrayList<Class<?>>();
      if(interceptorsAnnotation != null)
      {
         for(Class<?> classInterceptorClass : interceptorsAnnotation.value())
         {
            classInterceptorClasses.add(classInterceptorClass);
            if(!interceptorClasses.contains(classInterceptorClass))
               interceptorClasses.add(classInterceptorClass);
            if(!lifecycleInterceptorClasses.contains(classInterceptorClass))
               lifecycleInterceptorClasses.add(classInterceptorClass);
         }
      }
      log.debug("Found class interceptors " + classInterceptorClasses);
      
      Class<?> beanClass = advisor.getClazz();
      for(Method beanMethod : ClassHelper.getAllMethods(beanClass))
      {
         interceptorsAnnotation = (Interceptors) advisor.resolveAnnotation(beanMethod, Interceptors.class);
         List<Class<?>> methodInterceptorClasses = new ArrayList<Class<?>>();
         if(interceptorsAnnotation != null)
         {
            for(Class<?> interceptorClass : interceptorsAnnotation.value())
            {
               methodInterceptorClasses.add(interceptorClass);
               if(!interceptorClasses.contains(interceptorClass))
                  interceptorClasses.add(interceptorClass);
            }
         }
         
         // Interceptors applicable for this bean method
         List<Class<?>> methodApplicableInterceptorClasses = new ArrayList<Class<?>>();
         if(!isExcludeDefaultInterceptors(advisor, beanMethod))
            methodApplicableInterceptorClasses.addAll(defaultInterceptorClasses);
         if(!isExcludeClassInterceptors(advisor, beanMethod))
            methodApplicableInterceptorClasses.addAll(classInterceptorClasses);
         methodApplicableInterceptorClasses.addAll(methodInterceptorClasses);
         
         // TODO: remove duplicates?
         
         // Total ordering (EJB 3 12.8.2.1)
         // TODO: @Interceptors with all?
         InterceptorOrder order = (InterceptorOrder) advisor.resolveAnnotation(beanMethod, InterceptorOrder.class);
         if(order == null)
            order = (InterceptorOrder) advisor.resolveAnnotation(InterceptorOrder.class);
         // TODO: validate the order to see if all interceptors are listed
         if(order != null)
            applicableInterceptorClasses.put(beanMethod, Arrays.asList(order.value()));
         else
            applicableInterceptorClasses.put(beanMethod, methodApplicableInterceptorClasses);
      }
   }
   
   private static final boolean isExcludeClassInterceptors(Advisor advisor, Method method)
   {
      return advisor.hasAnnotation(method, ExcludeClassInterceptors.class) || advisor.resolveAnnotation(ExcludeClassInterceptors.class) != null;
   }
   
   private static final boolean isExcludeDefaultInterceptors(Advisor advisor, Method method)
   {
      return advisor.hasAnnotation(method, ExcludeDefaultInterceptors.class) || advisor.resolveAnnotation(ExcludeDefaultInterceptors.class) != null;
   } 
}
