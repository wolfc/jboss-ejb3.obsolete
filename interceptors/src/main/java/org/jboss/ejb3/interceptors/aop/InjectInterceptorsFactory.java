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

import javax.interceptor.ExcludeClassInterceptors;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructorJoinpoint;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aop.joinpoint.MethodJoinpoint;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
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
         
         List<Interceptor> interceptors = new ArrayList<Interceptor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean addAll(Collection<? extends Interceptor> c)
            {
               if(c == null) return false;
               return super.addAll(c);
            }
         };
         // TODO: implement default interceptors
//         if(!isIgnoreDefaultInterceptors(advisor, jp))
//            interceptors.addAll(defaultInterceptors);
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
         
         // FIXME: currently still handled by InterceptorsFactory
         
         return new InterceptorSequencer(new Interceptor[0]);
      }
   }
   
   private static final boolean isExcludeClassInterceptors(Advisor advisor, Method method)
   {
      return advisor.hasAnnotation(method, ExcludeClassInterceptors.class);
   }
   
   public String getName()
   {
      return "InjectInterceptorsFactory";
   }
}
