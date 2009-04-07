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
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.InvocationContext;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructorJoinpoint;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aop.joinpoint.MethodJoinpoint;
import org.jboss.ejb3.interceptors.container.AbstractContainer;
import org.jboss.ejb3.interceptors.container.ManagedObjectAdvisor;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class InjectInterceptorsFactory extends AbstractInterceptorFactory
{
   private static final Logger log = Logger.getLogger(InjectInterceptorsFactory.class);

   public InjectInterceptorsFactory()
   {
      log.debug("new InjectInterceptorsFactory");
   }

   /**
    * Generate the proper interceptor chain based on the spec interceptors.
    */
   @Override
   public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint jp)
   {
      assert advisor != null;
      assert instanceAdvisor != null;
      assert jp instanceof MethodJoinpoint || jp instanceof ConstructorJoinpoint;

      log.debug("Create interceptor chain for " + instanceAdvisor.getClass().getName() + "@" + System.identityHashCode(instanceAdvisor) + " on " + jp);

      /*
      defaultInterceptors = ...;
      classInterceptors = ...;
      businessMethodInterceptors = ...;
      beanInterceptor = ...;
      */
      if(jp instanceof MethodJoinpoint)
      {
         // aroundInvoke

         Method method = ((MethodJoinpoint) jp).getMethod();

         if(advisor instanceof ManagedObjectAdvisor)
         {
            AbstractContainer<?, ?> container = AbstractContainer.getContainer(advisor);
            List<Class<?>> interceptorClasses = container.getInterceptorRegistry().getApplicableInterceptorClasses(method);
            List<Interceptor> interceptors = new ArrayList<Interceptor>();
            if (interceptorClasses != null)
            {
               for (Class<?> interceptorClass : interceptorClasses)
               {
                  ExtendedAdvisor interceptorAdvisor = ExtendedAdvisorHelper.getExtendedAdvisor(advisor);
                  // Get all public/private/protected/package access methods of signature:
                  // Object <MethodName> (InvocationContext)
                  Method[] possibleInterceptorMethods = ClassHelper.getMethods(interceptorClass, Object.class,new Class<?>[] {InvocationContext.class});
                  for (Method interceptorMethod : possibleInterceptorMethods)
                  {
                     if (interceptorAdvisor.isAnnotationPresent(interceptorClass, interceptorMethod, AroundInvoke.class))
                     {
                        if (!ClassHelper.isOverridden(interceptorMethod, possibleInterceptorMethods))
                        {
                           interceptors.add(new EJB3InterceptorInterceptor(interceptorClass, interceptorMethod));
                        }
                     }
                  }
               }
            }
            Class<?> beanClass = advisor.getClazz();
            // Get all public/private/protected/package access methods of signature:
            // Object <MethodName> (InvocationContext)
            Method[] possibleAroundInvokeMethods = ClassHelper.getMethods(beanClass, Object.class, new Class<?>[] {InvocationContext.class});
            for(Method beanMethod : possibleAroundInvokeMethods)
            {
               if(advisor.hasAnnotation(beanMethod, AroundInvoke.class))
               {
                  if (!ClassHelper.isOverridden(beanMethod, possibleAroundInvokeMethods))
                  {
                     interceptors.add(new BusinessMethodBeanMethodInterceptor(beanMethod));
                  }
               }
            }
            return new InterceptorSequencer(interceptors);
         }

         List<Interceptor> interceptors = new ArrayList<Interceptor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean addAll(Collection<? extends Interceptor> c)
            {
               if(c == null) return false;
               return super.addAll(c);
            }
         };
         if(!isExcludeDefaultInterceptors(advisor, method))
            interceptors.addAll(InterceptorsFactory.getDefaultInterceptors(instanceAdvisor));
         if(!isExcludeClassInterceptors(advisor, method))
            interceptors.addAll(InterceptorsFactory.getClassInterceptors(instanceAdvisor));
         interceptors.addAll(InterceptorsFactory.getBusinessMethodInterceptors(instanceAdvisor, method));
         interceptors.addAll(InterceptorsFactory.getBeanInterceptors(instanceAdvisor));

         log.debug("interceptors " + interceptors);

         // TODO: total ordering (EJB 3 12.8.2.1 and @Interceptors with all)
         // FIXME
         return new InterceptorSequencer(interceptors);
      }
      else
      {
         // postConstruct

         if(advisor instanceof ManagedObjectAdvisor)
         {
            log.warn("EJBTHREE-1246: Do not use InjectInterceptorsFactory with a ManagedObjectAdvisor for lifecycle callbacks, should be done by the container");
            // Note that the container delegates it to ejb3-callbacks or the MC bean factory
            return new NopInterceptor();
         }

         List<Interceptor> interceptors = InterceptorsFactory.getLifeCycleInterceptors(instanceAdvisor, PostConstruct.class);

         log.debug("PostConstruct interceptors " + interceptors);

         return new InterceptorSequencer(interceptors);
      }
   }

   @Override
   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   {
      log.warn("WEIRDNESS IN AOP: advisor " + advisor);
      return new InterceptorSequencer(new Interceptor[0]);
      // If we're not running instrumented classes there is no instance advisor during
      // construction. (I've no clue why.)
      // Luckily our advisor is on the case.
//      InstanceAdvisor instanceAdvisor = (InstanceAdvisor) advisor;
//      return createPerJoinpoint(advisor, instanceAdvisor, jp);
      // Can't do that, because the instance interceptors are not there yet (InterceptorsFactory)
      // so the hack is in ManagedObjectAdvisor.createInterceptorChain.
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
