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
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.logging.Logger;

/**
 * Comment
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorsFactory implements AspectFactory
{
   private static final Logger log = Logger.getLogger(InterceptorsFactory.class);
   
   public Object createPerClass(Advisor advisor)
   {
      throw new IllegalStateException("Only per instance scope is supported");
   }

   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      try
      {
         log.debug("createPerInstance");
         log.debug(" advisor " + advisor.getName());
         log.debug(" instanceAdvisor " + instanceAdvisor);
         
         Interceptors interceptorsAnnotation = (Interceptors) advisor.resolveAnnotation(Interceptors.class);
         assert interceptorsAnnotation != null : "interceptors annotation not found"; // FIXME: not correct, bean can be without interceptors
         List<Interceptor> postConstructs = new ArrayList<Interceptor>();
         List<Interceptor> classInterceptors = new ArrayList<Interceptor>();
         for(Class<?> interceptorClass : interceptorsAnnotation.value())
         {
            Object interceptor = interceptorClass.newInstance();
            Advisor interceptorAdvisor = ((Advised) interceptor)._getAdvisor();
            log.info("interceptorAdvisor = " + interceptorAdvisor.getName());
            InstanceAdvisor interceptorInstanceAdvisor = ((Advised) interceptor)._getInstanceAdvisor();
            log.info("interceptorInstanceAdvisor = " + interceptorInstanceAdvisor);
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
         for(Method beanMethod : ClassHelper.getAllMethods(beanClass))
         {
            interceptorsAnnotation = (Interceptors) advisor.resolveAnnotation(beanMethod, Interceptors.class);
            if(interceptorsAnnotation != null)
            {
               List<Interceptor> businessMethodInterceptors = new ArrayList<Interceptor>();
               // TODO: use visitors
               for(Class<?> interceptorClass : interceptorsAnnotation.value())
               {
                  // TODO: do not create perse, we might already have done that
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
               instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, beanMethod, businessMethodInterceptors.toArray(new Interceptor[0]));
            }
         }
         
         log.debug("classInterceptors " + classInterceptors);
         // Class Interceptors
         instanceAdvisor.getMetaData().addMetaData(InterceptorsFactory.class, "classInterceptors", classInterceptors.toArray(new Interceptor[0]));
         
         // Put the postConstructs interceptors here in the chain
         // TODO: why? We may need more control
         return new InterceptorSequencer(postConstructs.toArray(new Interceptor[0]));
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

   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   {
      throw new IllegalStateException("Only per instance scope is supported");
   }

   public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint jp)
   {
      log.warn("Only per instance scope is supported");
      return createPerInstance(advisor, instanceAdvisor);
   }

   public Object createPerVM()
   {
      throw new IllegalStateException("Only per instance scope is supported");
   }

   public String getName()
   {
      return "InterceptorsFactory";
   }

}
