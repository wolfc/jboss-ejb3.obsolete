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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;

import org.jboss.aop.Advised;
import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.logging.Logger;

/**
 * Comment
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorsFactory extends AbstractInterceptorFactory
{
   private static final Logger log = Logger.getLogger(InterceptorsFactory.class);
   
   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      try
      {
         log.debug("createPerInstance");
         log.debug(" advisor " + advisor.getClass().getName());
         log.debug(" instanceAdvisor " + toString(instanceAdvisor));
         
         Interceptors interceptorsAnnotation = (Interceptors) advisor.resolveAnnotation(Interceptors.class);
         assert interceptorsAnnotation != null : "interceptors annotation not found"; // FIXME: not correct, bean can be without interceptors
         List<Interceptor> postConstructs = new ArrayList<Interceptor>();
         List<Interceptor> classInterceptors = new ArrayList<Interceptor>();
         for(Class<?> interceptorClass : interceptorsAnnotation.value())
         {
            Object interceptor = interceptorClass.newInstance();
            Advisor interceptorAdvisor = ((Advised) interceptor)._getAdvisor();
            log.debug("  interceptorAdvisor = " + interceptorAdvisor.getName());
            InstanceAdvisor interceptorInstanceAdvisor = ((Advised) interceptor)._getInstanceAdvisor();
            log.debug("  interceptorInstanceAdvisor = " + interceptorInstanceAdvisor.getClass().getName());
            // TODO: should be only non-overriden methods (EJB 3 12.4.1 last bullet)
            for(Method method : ClassHelper.getAllMethods(interceptorClass))
            {
               if(interceptorAdvisor.hasAnnotation(method, PostConstruct.class))
               {
                  postConstructs.add(new LifecycleCallbackInterceptorMethodInterceptor(interceptor, method));
               }
               if(interceptorAdvisor.hasAnnotation(method, AroundInvoke.class))
               {
                  classInterceptors.add(new BusinessMethodInterceptorMethodInterceptor(interceptor, method));
               }
            }
            //instanceAdvisor.appendInterceptorStack(stackName);
            //instanceAdvisor.appendInterceptor(new InvokeSpecInterceptorInterceptor());
         }
         
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
                  // FIXME: do not create perse, we might already have done that
                  Object interceptor = interceptorClass.newInstance();
                  Advisor interceptorAdvisor = ((Advised) interceptor)._getAdvisor();
                  for(Method method : ClassHelper.getAllMethods(interceptorClass))
                  {
                     /* EJB 3 12.7 footnote 57: no lifecycle callbacks on business method interceptors
                     if(interceptorAdvisor.hasAnnotation(method, PostConstruct.class))
                     {
                        postConstructs.add(new LifecycleCallbackInterceptorMethodInterceptor(interceptor, method));
                     }
                     */
                     if(interceptorAdvisor.hasAnnotation(method, AroundInvoke.class))
                     {
                        businessMethodInterceptors.add(new BusinessMethodInterceptorMethodInterceptor(interceptor, method));
                     }
                  }
               }
               assert businessMethodInterceptors.size() > 0 : "TODO: lucky guess";
               instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, beanMethod, businessMethodInterceptors);
            }
            
            if(advisor.hasAnnotation(beanMethod, AroundInvoke.class))
            {
               beanInterceptors.add(new BusinessMethodBeanMethodInterceptor(beanMethod));
            }
         }
         log.debug("Found bean interceptors " + beanInterceptors);
         instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, "beanInterceptors", beanInterceptors);
         
         log.debug("Found class interceptors " + classInterceptors);
         // Class Interceptors
         instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, "classInterceptors", classInterceptors);
         
         // Put the postConstructs interceptors here in the chain
         // TODO: why? We may need more control
         return new InterceptorSequencer(postConstructs.toArray(new Interceptor[0]));
         //return null;
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
   
   private String toString(Object obj)
   {
      return obj.getClass().getName() + "@" + System.identityHashCode(obj);
   }
}
