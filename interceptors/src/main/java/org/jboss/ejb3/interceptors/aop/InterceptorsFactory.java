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
package org.jboss.ejb3.interceptors.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.interceptors.InterceptorFactory;
import org.jboss.ejb3.interceptors.InterceptorFactoryRef;
import org.jboss.ejb3.interceptors.aop.annotation.DefaultInterceptors;
import org.jboss.ejb3.interceptors.container.ManagedObjectAdvisor;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.logging.Logger;

/**
 * The interceptors factory analyzes the annotations and creates
 * interceptor instances out of those. These are then attached
 * to the advisor as meta data.
 * 
 * Do not access this meta data directly, use the provided static
 * methods herein.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class InterceptorsFactory extends AbstractInterceptorFactory
{
   private static final Logger log = Logger.getLogger(InterceptorsFactory.class);
   
   // TODO: allow for extensions (/ new life-cycle annotations) (and PostActive, PrePassivate are part of stateful) 
   /**
    * This defined the life-cycle annotations for which we build up
    * an interceptor chain.
    */
   @SuppressWarnings("unchecked")
   private static final Class<? extends Annotation> lifeCycleAnnotationClasses[] = (Class<? extends Annotation>[]) new Class<?>[] { PostActivate.class, PostConstruct.class, PreDestroy.class, PrePassivate.class };
   
   private List<? extends Interceptor> createInterceptors(Advisor advisor, InterceptorFactory interceptorFactory, Class<?>[] interceptorClasses, List<BusinessMethodInterceptorMethodInterceptor> interceptors, Map<Class<?>, Object> existingInterceptors, Map<Class<? extends Annotation>, List<Interceptor>> lifeCycleInterceptors) throws InstantiationException, IllegalAccessException
   {
      if(interceptorClasses != null)
      {
         for(Class<?> interceptorClass : interceptorClasses)
         {
            // TODO: what if I've specified the same interceptor twice? (throw an Exception?)
            Object interceptor = existingInterceptors.get(interceptorClass);
            if(interceptor == null)
            {
               interceptor = interceptorFactory.create(advisor, interceptorClass);
               existingInterceptors.put(interceptorClass, interceptor);
            }
            //Advisor interceptorAdvisor = ((Advised) interceptor)._getAdvisor();
            //Advisor interceptorAdvisor = advisor.getManager().getAdvisor(interceptorClass);
            //AnnotationAdvisor interceptorAdvisor = AnnotationAdvisorHelper.getAnnotationAdvisor(advisor, interceptor);
            ExtendedAdvisor interceptorAdvisor = ExtendedAdvisorHelper.getExtendedAdvisor(advisor, interceptor);
            log.debug("  interceptorAdvisor = " + interceptorAdvisor);
            // TODO: should be only non-overriden methods (EJB 3 12.4.1 last bullet)
            for(Method method : ClassHelper.getAllMethods(interceptorClass))
            {
               if(interceptorAdvisor.isAnnotationPresent(interceptorClass, method, AroundInvoke.class))
               {
                  interceptors.add(new BusinessMethodInterceptorMethodInterceptor(interceptor, method));
               }
               for(Class<? extends Annotation> lifeCycleAnnotationClass : lifeCycleAnnotationClasses)
               {
                  if(interceptorAdvisor.isAnnotationPresent(interceptorClass, method, lifeCycleAnnotationClass))
                  {
                     lifeCycleInterceptors.get(lifeCycleAnnotationClass).add(new LifecycleCallbackInterceptorMethodInterceptor(interceptor, method));
                  }
               }
            }
            //instanceAdvisor.appendInterceptorStack(stackName);
            //instanceAdvisor.appendInterceptor(new InvokeSpecInterceptorInterceptor());
         }
      }
      return interceptors;
   }
   
   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      if(advisor instanceof ManagedObjectAdvisor)
      {
         log.warn("EJBTHREE-1246: Do not use InterceptorsFactory with a ManagedObjectAdvisor, InterceptorRegistry should be used via the container");
         return new NopInterceptor();
      }
      
      try
      {
         log.debug("createPerInstance");
         log.debug(" advisor " + advisor.getClass().getName());
         log.debug(" instanceAdvisor " + toString(instanceAdvisor));
         
         // TODO: the whole interceptor advisor & annotation stuff is butt ugly
         
         InterceptorFactoryRef interceptorFactoryRef = (InterceptorFactoryRef) advisor.resolveAnnotation(InterceptorFactoryRef.class);
         if(interceptorFactoryRef == null)
            throw new IllegalStateException("No InterceptorFactory specified on " + advisor.getName());
         log.debug("interceptor factory class = " + interceptorFactoryRef.value());
         InterceptorFactory interceptorFactory = interceptorFactoryRef.value().newInstance();
         
         Map<Class<?>, Object> interceptors = new HashMap<Class<?>, Object>();
         
         Map<Class<? extends Annotation>, List<Interceptor>> lifeCycleInterceptors = new HashMap<Class<? extends Annotation>, List<Interceptor>>();
         for(Class<? extends Annotation> lifeCycleAnnotationClass : lifeCycleAnnotationClasses)
         {
            List<Interceptor> list = new ArrayList<Interceptor>();
            lifeCycleInterceptors.put(lifeCycleAnnotationClass, list);
         }
         
         DefaultInterceptors defaultInterceptorsAnnotation = (DefaultInterceptors) advisor.resolveAnnotation(DefaultInterceptors.class);
         List<BusinessMethodInterceptorMethodInterceptor> defaultInterceptors = new ArrayList<BusinessMethodInterceptorMethodInterceptor>();
         if(defaultInterceptorsAnnotation != null)
            createInterceptors(advisor, interceptorFactory, defaultInterceptorsAnnotation.value(), defaultInterceptors, interceptors, lifeCycleInterceptors);
         
         log.debug("Found class interceptors " + defaultInterceptors);
         // Default Interceptors
         instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, "defaultInterceptors", defaultInterceptors);
         
         Interceptors interceptorsAnnotation = (Interceptors) advisor.resolveAnnotation(Interceptors.class);
         List<BusinessMethodInterceptorMethodInterceptor> classInterceptors = new ArrayList<BusinessMethodInterceptorMethodInterceptor>();
         if(interceptorsAnnotation != null)
            createInterceptors(advisor, interceptorFactory, interceptorsAnnotation.value(), classInterceptors, interceptors, lifeCycleInterceptors);
         
         log.debug("Found class interceptors " + classInterceptors);
         // Class Interceptors
         instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, "classInterceptors", classInterceptors);
         
         Class<?> beanClass = advisor.getClazz();
         List<Interceptor> beanInterceptors = new ArrayList<Interceptor>();
         for(Method beanMethod : ClassHelper.getAllMethods(beanClass))
         {
            interceptorsAnnotation = (Interceptors) advisor.resolveAnnotation(beanMethod, Interceptors.class);
            if(interceptorsAnnotation != null)
            {
               List<Interceptor> businessMethodInterceptors = new ArrayList<Interceptor>();
               // TODO: use visitors?
               for(Class<?> interceptorClass : interceptorsAnnotation.value())
               {
                  Object interceptor = interceptors.get(interceptorClass);
                  if(interceptor == null)
                  {
                     interceptor = interceptorFactory.create(advisor, interceptorClass);
                     interceptors.put(interceptorClass, interceptor);
                  }
                  //Advisor interceptorAdvisor = ((Advised) interceptor)._getAdvisor();
                  //Advisor interceptorAdvisor = advisor.getManager().getAdvisor(interceptorClass);
                  ExtendedAdvisor interceptorAdvisor = ExtendedAdvisorHelper.getExtendedAdvisor(advisor, interceptor);
                  for(Method method : ClassHelper.getAllMethods(interceptorClass))
                  {
                     /* EJB 3 12.7 footnote 57: no lifecycle callbacks on business method interceptors
                     if(interceptorAdvisor.isAnnotationPresent(interceptorClass, method, PostConstruct.class))
                     {
                        postConstructs.add(new LifecycleCallbackInterceptorMethodInterceptor(interceptor, method));
                     }
                     */
                     if(interceptorAdvisor.isAnnotationPresent(interceptorClass, method, AroundInvoke.class))
                     {
                        businessMethodInterceptors.add(new BusinessMethodInterceptorMethodInterceptor(interceptor, method));
                     }
                  }
               }
               instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, beanMethod, businessMethodInterceptors);
            }
            
            if(advisor.hasAnnotation(beanMethod, AroundInvoke.class))
            {
               beanInterceptors.add(new BusinessMethodBeanMethodInterceptor(beanMethod));
            }
            for(Class<? extends Annotation> lifeCycleAnnotationClass : lifeCycleAnnotationClasses)
            {
               if(advisor.hasAnnotation(beanMethod, lifeCycleAnnotationClass))
               {
                  lifeCycleInterceptors.get(lifeCycleAnnotationClass).add(new LifecycleCallbackBeanMethodInterceptor(beanMethod));
               }
            }
         }
         log.debug("Found bean interceptors " + beanInterceptors);
         instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, "beanInterceptors", beanInterceptors);
         
         log.debug("Found life cycle interceptors " + lifeCycleInterceptors);
         instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, "lifeCycleInterceptors", Collections.unmodifiableMap(lifeCycleInterceptors));
         
         return new NopInterceptor();
      }
      catch(InstantiationException e)
      {
         Throwable cause = e.getCause();
         if(cause instanceof Error)
            throw (Error) cause;
         if(cause instanceof RuntimeException)
            throw (RuntimeException) cause;
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }

   @SuppressWarnings("unchecked")
   public static List<Interceptor> getBeanInterceptors(InstanceAdvisor instanceAdvisor)
   {
      return (List<Interceptor>) instanceAdvisor.getMetaData().getMetaData(InterceptorsFactory.class, "beanInterceptors");
   }
   
   @SuppressWarnings("unchecked")
   @Deprecated
   public static Interceptor[] getBusinessMethodInterceptors(MethodInvocation invocation)
   {
      List<Interceptor> list = (List<Interceptor>) invocation.getMetaData(InterceptorsFactory.class, invocation.getActualMethod());
      if(list == null) return null;
      return list.toArray(new Interceptor[0]);
   }
   
   @SuppressWarnings("unchecked")
   public static List<Interceptor> getBusinessMethodInterceptors(InstanceAdvisor instanceAdvisor, Method businessMethod)
   {
      return (List<Interceptor>) instanceAdvisor.getMetaData().getMetaData(InterceptorsFactory.class, businessMethod);
   }
   
   @SuppressWarnings("unchecked")
   @Deprecated
   public static Interceptor[] getClassInterceptors(Invocation invocation)
   {
      return ((List<Interceptor>) invocation.getMetaData(InterceptorsFactory.class, "classInterceptors")).toArray(new Interceptor[0]);
   }
   
   @SuppressWarnings("unchecked")
   public static List<Interceptor> getClassInterceptors(InstanceAdvisor instanceAdvisor)
   {
      return (List<Interceptor>) instanceAdvisor.getMetaData().getMetaData(InterceptorsFactory.class, "classInterceptors");
   }
   
   @SuppressWarnings("unchecked")
   public static List<Interceptor> getDefaultInterceptors(InstanceAdvisor instanceAdvisor)
   {
      return (List<Interceptor>) instanceAdvisor.getMetaData().getMetaData(InterceptorsFactory.class, "defaultInterceptors");
   }
   
   @SuppressWarnings("unchecked")
   private static Map<Class<? extends Annotation>, List<Interceptor>> getLifeCycleInterceptors(InstanceAdvisor instanceAdvisor)
   {
      return (Map<Class<? extends Annotation>, List<Interceptor>>) instanceAdvisor.getMetaData().getMetaData(InterceptorsFactory.class, "lifeCycleInterceptors");
   }
   
   public static List<Interceptor> getLifeCycleInterceptors(InstanceAdvisor instanceAdvisor,
         Class<? extends Annotation> lifeCycleAnnotationClass)
   {
      // Obtain lifecycle interceptors
      Map<Class<? extends Annotation>, List<Interceptor>> lifecycleInterceptors = getLifeCycleInterceptors(instanceAdvisor);

      // If there are no lifecycle interceptors
      if (lifecycleInterceptors == null)
      {
         // Return an empty list
         return new ArrayList<Interceptor>();
      }
      
      // Return the interceptors for this lifecycle annotation class
      return lifecycleInterceptors.get(lifeCycleAnnotationClass);
   }
   
   /**
    * @deprecated use getLifeCycleInterceptors
    * @param instanceAdvisor
    * @return
    */
   @Deprecated
   public static List<Interceptor> getPreDestroys(InstanceAdvisor instanceAdvisor)
   {
      return getLifeCycleInterceptors(instanceAdvisor, PreDestroy.class);
   }
   
   private String toString(Object obj)
   {
      return obj.getClass().getName() + "@" + System.identityHashCode(obj);
   }
}
