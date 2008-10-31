/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptor;

import java.lang.reflect.Method;

import javax.jms.MessageListener;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.logging.Logger;
import org.jboss.util.MethodHashing;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class EJB3InterceptorsFactory implements org.jboss.aop.advice.AspectFactory
{

   static Logger log = Logger.getLogger(EJB3InterceptorsFactory.class);
   final static long MESSAGE_LISTENER_ON_MESSAGE;

   static
   {
      try
      {
         Class clazz = MessageListener.class;
         Method m = clazz.getDeclaredMethod("onMessage", new Class[]{javax.jms.Message.class});
         MESSAGE_LISTENER_ON_MESSAGE = MethodHashing.calculateHash(m);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error initialising hash for MessageListener.onMessage()", e);
      }
   }

   public String getName()
   {
      return getClass().getName();
   }

   public Object createPerVM()
   {
      throw new RuntimeException("NOT ALLOWED");
   }

   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      throw new RuntimeException("NOT ALLOWED");
   }

   public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint jp)
   {
      throw new RuntimeException("NOT ALLOWED");
   }

   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   {
      /*
      if (jp instanceof MethodJoinpoint)
      {
         EJBContainer container = EJBContainer.getEJBContainer(advisor);
         Class beanClass = container.getBeanClass();

         try
         {
            Method method = ((MethodJoinpoint) jp).getMethod();
            if (container.isBusinessMethod(method))
            {
               InterceptorInfo[] infos = container.getInterceptorRepository().getBusinessInterceptors(container, method);
               Method[] beanAroundInvoke = container.getInterceptorRepository().getBeanClassAroundInvokes(container);
               Object infoString = "[]";
               if (infoString != null)
                  infoString = Arrays.asList(infos);
               log.debug("Bound interceptors for joinpoint: " + method + " - " + infoString);
               return new EJB3InterceptorsInterceptor(infos, beanAroundInvoke);
            }
         }
         catch (RuntimeException e)
         {
            throw new RuntimeException("An exception occurred initialising interceptors for " + beanClass + "." + ((MethodJoinpoint) jp).getMethod().getName(), e);
         }
      }
      return new EJB3InterceptorsInterceptor(new InterceptorInfo[0], null);
      */
      throw new RuntimeException("no longer supported (EJBTHREE-1174)");
   }

   public Object createPerClass(Advisor advisor)
   {
      throw new RuntimeException("NOT ALLOWED");
   }
}
