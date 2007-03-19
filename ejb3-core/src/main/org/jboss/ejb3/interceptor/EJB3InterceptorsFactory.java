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

import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Producer;
import org.jboss.annotation.ejb.Producers;
import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aop.joinpoint.MethodJoinpoint;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.mdb.ConsumerContainer;
import org.jboss.ejb3.mdb.MDB;
import org.jboss.ejb3.service.ServiceContainer;
import org.jboss.logging.Logger;
import org.jboss.util.MethodHashing;

import javax.jms.MessageListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

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
      if (jp instanceof MethodJoinpoint)
      {
         EJBContainer container = (EJBContainer) advisor;
         Class beanClass = container.getBeanClass();

         try
         {
            Method method = ((MethodJoinpoint) jp).getMethod();
            if (isBusinessMethod(container, method))
            {
               InterceptorInfo[] infos = container.getInterceptorRepository().getBusinessInterceptors(container, method);
               Method[] beanAroundInvoke = container.getInterceptorRepository().getBeanClassAroundInvokes(container);
               log.debug("Bound interceptors for joinpoint: " + method + " - " + infos);
               return new EJB3InterceptorsInterceptor(infos, beanAroundInvoke);
            }
         }
         catch (RuntimeException e)
         {
            throw new RuntimeException("An exception occurred initialising interceptors for " + beanClass + "." + ((MethodJoinpoint) jp).getMethod().getName(), e);
         }
      }
      return new EJB3InterceptorsInterceptor(new InterceptorInfo[0], null);
   }

   public Object createPerClass(Advisor advisor)
   {
      throw new RuntimeException("NOT ALLOWED");
   }

   private boolean isBusinessMethod(EJBContainer container, Method method)
   {
      long hash = MethodHashing.calculateHash(method);
      ArrayList<Class> businessInterfaces = getBusinessInterfaces(container);
      for (Class businessInterface : businessInterfaces)
      {
         for (Method interfaceMethod : businessInterface.getMethods())
         {
            if (MethodHashing.calculateHash(interfaceMethod) == hash)
            {
               return true;
            }
         }
      }

      return false;
   }

   private ArrayList<Class> getBusinessInterfaces(EJBContainer container)
   {
      ArrayList<Class> interfaces = new ArrayList<Class>();
      if (container instanceof ConsumerContainer)
      {
         Producers producers = (Producers) container.resolveAnnotation(Producers.class);
         if (producers != null)
         {
            for (Producer producer : producers.value())
            {
               interfaces.add(producer.producer());
            }
         }

         Producer producer = (Producer) container.resolveAnnotation(Producer.class);
         if (producer != null)
         {
            interfaces.add(producer.producer());
         }

         for (Class implIf : container.getBeanClass().getInterfaces())
         {
            if (implIf.getAnnotation(Producer.class) != null)
            {
               interfaces.add(implIf);
            }
         }
      }
      else if (container instanceof MDB)
      {
         interfaces.add(((MDB)container).getMessagingType());
      }
      else
      {
         Class[] remotes = ProxyFactoryHelper.getRemoteInterfaces(container);
         Class[] locals = ProxyFactoryHelper.getLocalInterfaces(container);
         if (remotes != null)
         {
            interfaces.addAll(Arrays.asList(remotes));
         }
         if (locals != null)
         {
            interfaces.addAll(Arrays.asList(locals));
         }

         if (container instanceof ServiceContainer)
         {
            Management man = (Management) container.resolveAnnotation(Management.class);
            if (man != null)
            {
               Class iface = man.value();
               if (iface != null)
               {
                  interfaces.add(iface);
               }
            }

            Class[] implIfaces = container.getBeanClass().getInterfaces();
            for (Class iface : implIfaces)
            {
               if (iface.getAnnotation(Management.class) != null)
               {
                  interfaces.add(iface);
               }
            }
         }
      }

      return interfaces;
   }

}
