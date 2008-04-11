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
package org.jboss.ejb3.interceptors.metadata;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;

import org.jboss.ejb3.interceptors.annotation.impl.InterceptorsImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PostActivateImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PostConstructImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PreDestroyImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PrePassivateImpl;
import org.jboss.ejb3.interceptors.aop.annotation.DefaultInterceptors;
import org.jboss.ejb3.interceptors.aop.annotation.DefaultInterceptorsImpl;
import org.jboss.ejb3.interceptors.aop.annotation.InterceptorOrder;
import org.jboss.ejb3.interceptors.aop.annotation.InterceptorOrderImpl;
import org.jboss.ejb3.interceptors.util.InterceptorCollection;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.AroundInvokesMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingsMetaData;
import org.jboss.metadata.ejb.spec.InterceptorClassesMetaData;
import org.jboss.metadata.ejb.spec.MethodParametersMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class BeanInterceptorMetaDataBridge extends EnvironmentInterceptorMetaDataBridge<JBossEnterpriseBeanMetaData> implements MetaDataBridge<JBossEnterpriseBeanMetaData>
{
   private static final Logger log = Logger.getLogger(BeanInterceptorMetaDataBridge.class);

   private static boolean add(InterceptorCollection interceptors, ClassLoader classLoader, InterceptorBindingMetaData binding)
   {
      boolean result = false;
      InterceptorClassesMetaData interceptorClassesMetaData;
      if(binding.isTotalOrdering())
      {
         interceptorClassesMetaData = binding.getInterceptorOrder();
      }
      else
      {
         interceptorClassesMetaData = binding.getInterceptorClasses();
      }
      if(interceptorClassesMetaData != null)
      {
         for(String interceptorClassName : interceptorClassesMetaData)
         {
            result |= interceptors.addValue(loadClass(classLoader, interceptorClassName));
         }
      }
      return result;
   }
   
   protected static boolean add(List<Class<?>> interceptors, ClassLoader classLoader, InterceptorBindingMetaData binding)
   {
      InterceptorClassesMetaData interceptorClassesMetaData;
      if(binding.isTotalOrdering())
      {
         interceptorClassesMetaData = binding.getInterceptorOrder();
      }
      else
      {
         interceptorClassesMetaData = binding.getInterceptorClasses();
      }
      for(String interceptorClassName : interceptorClassesMetaData)
      {
         interceptors.add(loadClass(classLoader, interceptorClassName));
      }
      return true;
   }
   
   private static Class<?> loadClass(ClassLoader classLoader, String name)
   {
      try
      {
         return classLoader.loadClass(name);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader)
   {
      if(annotationClass == DefaultInterceptors.class)
      {
         InterceptorBindingsMetaData bindings = beanMetaData.getEjbJarMetaData().getAssemblyDescriptor().getInterceptorBindings();
         if(bindings != null)
         {
            List<Class<?>> interceptors = new ArrayList<Class<?>>();
            for(InterceptorBindingMetaData binding : bindings)
            {
               String bindingEjbName = binding.getEjbName();
               if(bindingEjbName.equals("*"))
               {
                  assert binding.getMethod() == null : "method binding not allowed on default interceptor";
                  
                  add(interceptors, classLoader, binding);
               }
            }
            if(!interceptors.isEmpty())
               return annotationClass.cast(new DefaultInterceptorsImpl(interceptors));
         }
      }
      else if(annotationClass == InterceptorOrder.class)
      {
         InterceptorBindingsMetaData bindings = beanMetaData.getEjbJarMetaData().getAssemblyDescriptor().getInterceptorBindings();
         if(bindings != null)
         {
            InterceptorOrderImpl interceptorOrder = new InterceptorOrderImpl();
            for(InterceptorBindingMetaData binding : bindings)
            {
               // Only for specifying order
               if(!binding.isTotalOrdering())
                  continue;
               
               // For the method component
               if(binding.getMethod() != null)
                  continue;
               
               String ejbName = beanMetaData.getEjbName();
               String bindingEjbName = binding.getEjbName();
               if(bindingEjbName.equals(ejbName))
                  add(interceptorOrder, classLoader, binding);
            }
            if(!interceptorOrder.isEmpty())
               return annotationClass.cast(interceptorOrder);
         }
      }
      else if(annotationClass == Interceptors.class)
      {
         InterceptorBindingsMetaData bindings = beanMetaData.getEjbJarMetaData().getAssemblyDescriptor().getInterceptorBindings();
         if(bindings != null)
         {
            InterceptorsImpl interceptors = new InterceptorsImpl();
            for(InterceptorBindingMetaData binding : bindings)
            {
               // Only for specifying order
               if(binding.isTotalOrdering())
                  continue;
               
               // For the method component
               if(binding.getMethod() != null)
                  continue;
               
               String ejbName = beanMetaData.getEjbName();
               String bindingEjbName = binding.getEjbName();
               if(bindingEjbName.equals(ejbName))
                  add(interceptors, classLoader, binding);
            }
            if(!interceptors.isEmpty())
               return annotationClass.cast(interceptors);
         }
      }
      return super.retrieveAnnotation(annotationClass, beanMetaData, classLoader);
   }

   @Override
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader, String methodName, String... parameterNames)
   {
      if(annotationClass == AroundInvoke.class)
      {
         AroundInvokesMetaData aroundInvokes = null;
//         if(beanMetaData instanceof JBossGenericBeanMetaData)
//            aroundInvokes = ((JBossGenericBeanMetaData) beanMetaData).getAroundInvokes();
         if(beanMetaData instanceof JBossMessageDrivenBeanMetaData)
            aroundInvokes = ((JBossMessageDrivenBeanMetaData) beanMetaData).getAroundInvokes();
         else if(beanMetaData instanceof JBossSessionBeanMetaData)
            aroundInvokes = ((JBossSessionBeanMetaData) beanMetaData).getAroundInvokes();
         if(aroundInvokes != null)
         {
            Annotation annotation = getAroundInvokeAnnotation(aroundInvokes, methodName);
            if(annotation != null)
               return annotationClass.cast(annotation);
         }
      }
      else if(annotationClass == InterceptorOrder.class)
      {
         InterceptorBindingsMetaData bindings = beanMetaData.getEjbJarMetaData().getAssemblyDescriptor().getInterceptorBindings();
         if(bindings != null)
         {
            InterceptorOrderImpl interceptorOrder = new InterceptorOrderImpl();
            for(InterceptorBindingMetaData binding : bindings)
            {
               // Only for specifying order
               if(!binding.isTotalOrdering())
                  continue;
               
               // For the bean
               if(binding.getMethod() == null)
                  continue;
               
               NamedMethodMetaData method = binding.getMethod();
               
               // TODO: this is weird, it should have been caught earlier (invalid xml)
               if(method.getMethodName() == null)
                  continue;
               
               if(method.getMethodName().equals(methodName))
               {
                  MethodParametersMetaData methodParams = method.getMethodParams();
                  if(methodParams == null)
                     add(interceptorOrder, classLoader, binding);
                  else
                  {
                     if(Arrays.equals(methodParams.toArray(), parameterNames))
                        add(interceptorOrder, classLoader, binding);
                  }
               }
            }
            if(!interceptorOrder.isEmpty())
               return annotationClass.cast(interceptorOrder);
         }
      }
      else if(annotationClass == Interceptors.class)
      {
         InterceptorBindingsMetaData bindings = beanMetaData.getEjbJarMetaData().getAssemblyDescriptor().getInterceptorBindings();
         if(bindings != null)
         {
            InterceptorsImpl interceptors = new InterceptorsImpl();
            for(InterceptorBindingMetaData binding : bindings)
            {
               // Only for specifying order
               if(binding.isTotalOrdering())
                  continue;
               
               // For the bean
               if(binding.getMethod() == null)
                  continue;
               
               NamedMethodMetaData method = binding.getMethod();
               
               // TODO: this is weird, it should have been caught earlier (invalid xml)
               if(method.getMethodName() == null)
                  continue;
               
               if(method.getMethodName().equals(methodName))
               {
                  MethodParametersMetaData methodParams = method.getMethodParams();
                  if(methodParams == null)
                     add(interceptors, classLoader, binding);
                  else
                  {
                     if(Arrays.equals(methodParams.toArray(), parameterNames))
                        add(interceptors, classLoader, binding);
                  }
               }
            }
            if(!interceptors.isEmpty())
               return annotationClass.cast(interceptors);
         }
      }
      else if(annotationClass == PostActivate.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData)
         {
            PostActivate lifeCycleAnnotation = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPostActivates(), PostActivateImpl.class, methodName);
            if(lifeCycleAnnotation != null)
               return annotationClass.cast(lifeCycleAnnotation);
         }
      }
      else if(annotationClass == PostConstruct.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData)
         {
            PostConstruct lifeCycleAnnotation = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPostConstructs(), PostConstructImpl.class, methodName);
            if(lifeCycleAnnotation != null)
               return annotationClass.cast(lifeCycleAnnotation);
         }
      }
      else if(annotationClass == PreDestroy.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData)
         {
            PreDestroy lifeCycleAnnotation = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPreDestroys(), PreDestroyImpl.class, methodName);
            if(lifeCycleAnnotation != null)
               return annotationClass.cast(lifeCycleAnnotation);
         }
      }
      else if(annotationClass == PrePassivate.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData)
         {
            PrePassivate lifeCycleAnnotation = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPrePassivates(), PrePassivateImpl.class, methodName);
            if(lifeCycleAnnotation != null)
               return annotationClass.cast(lifeCycleAnnotation);
         }
      }
      return super.retrieveAnnotation(annotationClass, beanMetaData, classLoader, methodName, parameterNames);
   }
}
