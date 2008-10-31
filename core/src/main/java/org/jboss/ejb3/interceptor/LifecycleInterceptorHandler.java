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
import java.lang.reflect.Modifier;

import javax.interceptor.InvocationContext;
import javax.ejb.PostActivate;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PrePassivate;
import javax.ejb.Timeout;
import javax.ejb.Timer;

import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.util.MethodHashing;
import org.jboss.logging.Logger;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 * @deprecated EJBTHREE-1174
 */
@Deprecated
public class LifecycleInterceptorHandler
{
   private static final Logger log = Logger.getLogger(LifecycleInterceptorHandler.class);

   private EJBContainer container;
   private InterceptorInfo[] postConstructs;
   private InterceptorInfo[] postActivates;
   private InterceptorInfo[] prePassivates;
   private InterceptorInfo[] preDestroys;
   private Method[] beanPostConstructs;
   private Method[] beanPostActivates;
   private Method[] beanPrePassivates;
   private Method[] beanPreDestroys;
   private Method timeoutCallbackMethod;
   private long timeoutCalllbackHash;

   public LifecycleInterceptorHandler(EJBContainer container, Class[] handledCallbacks)
   {
      /*
      this.container = container;
      InterceptorInfoRepository repostitory = container.getInterceptorRepository();
      for (Class clazz : handledCallbacks)
      {
         if (clazz == PostConstruct.class)
         {
            postConstructs = repostitory.getPostConstructInterceptors(container);
            beanPostConstructs = repostitory.getBeanClassPostConstructs(container);
         }
         else if (clazz == PostActivate.class)
         {
            postActivates = repostitory.getPostActivateInterceptors(container);
            beanPostActivates = repostitory.getBeanClassPostActivates(container);
         }
         else if (clazz == PrePassivate.class)
         {
            prePassivates = repostitory.getPrePassivateInterceptors(container);
            beanPrePassivates = repostitory.getBeanClassPrePassivates(container);
         }
         else if (clazz == PreDestroy.class)
         {
            preDestroys = repostitory.getPreDestroyInterceptors(container);
            beanPreDestroys = repostitory.getBeanClassPreDestroys(container);
         }
         else if (clazz == Timeout.class)
         {
            resolveTimeoutCallback();
         }
      }
      */
      throw new RuntimeException("no longer supported (EJBTHREE-1174)");
   }

   public long getTimeoutCalllbackHash()
   {
      return timeoutCalllbackHash;
   }

   public void postConstruct(BeanContext ctx, Object[] params)
   {
      try
      {
         InvocationContext ic = LifecycleInvocationContextImpl.getLifecycleInvocationContext(PostConstruct.class, ctx, postConstructs, beanPostConstructs);
         ic.setParameters(params);
         ic.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void preDestroy(BeanContext ctx)
   {
      Object id = null;
      if (ctx instanceof StatefulBeanContext)
      {
         id = ((StatefulBeanContext)ctx).getId();
      }
      try
      {
         InvocationContext ic = LifecycleInvocationContextImpl.getLifecycleInvocationContext(
               PreDestroy.class,
               ctx,
               preDestroys,
               beanPreDestroys);
         ic.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void postActivate(BeanContext ctx)
   {
      try
      {
         InvocationContext ic = LifecycleInvocationContextImpl.getLifecycleInvocationContext(
               PostActivate.class,
               ctx,
               postActivates,
               beanPostActivates);
         ic.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void prePassivate(BeanContext ctx)
   {
      try
      {
         InvocationContext ic = LifecycleInvocationContextImpl.getLifecycleInvocationContext(
               PrePassivate.class,
               ctx,
               prePassivates,
               beanPrePassivates);
         ic.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public Method getTimeoutCallback()
   {
      return timeoutCallbackMethod;
   }


   private void resolveTimeoutCallback()
   {
      for (Method method : container.getBeanClass().getMethods())
      {
         if (container.resolveAnnotation(method, Timeout.class) != null)
         {
            if (Modifier.isPublic(method.getModifiers()) &&
                  method.getReturnType().equals(Void.TYPE) &&
                  method.getParameterTypes().length == 1 &&
                  method.getParameterTypes()[0].equals(Timer.class))
            {
               timeoutCallbackMethod = method;
            }
            else
            {
               throw new RuntimeException("@Timeout methods must have the signature: public void <METHOD>(javax.ejb.Timer timer) - " + method);
            }
         }
      }

      try
      {
         if (timeoutCallbackMethod == null && javax.ejb.TimedObject.class.isAssignableFrom(container.getBeanClass()))
         {
            Class[] params = new Class[]{Timer.class};
            timeoutCallbackMethod = container.getBeanClass().getMethod("ejbTimeout", params);
         }
      }
      catch (Exception e)
      {
         log.error("Exception encoutered in " + LifecycleInterceptorHandler.class.getName()
               + ".resolveTimeoutCallback()", e);
      }

      if (timeoutCallbackMethod != null)
      {
         timeoutCalllbackHash = MethodHashing.calculateHash(timeoutCallbackMethod);
      }
   }

}
