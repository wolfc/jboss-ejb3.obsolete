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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundInvoke;

import org.jboss.ejb3.interceptors.annotation.impl.AroundInvokeImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PostConstructImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PreDestroyImpl;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.AroundInvokeMetaData;
import org.jboss.metadata.ejb.spec.AroundInvokesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.spi.signature.DeclaredMethodSignature;

/**
 * Does only interceptor stuff.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class EnvironmentInterceptorMetaDataBridge<M extends Environment> implements MetaDataBridge<M>
{
   private static final Logger log = Logger.getLogger(EnvironmentInterceptorMetaDataBridge.class);

   protected <T extends Annotation> T createAnnotationImpl(Class<T> annotationImplType)
   {
      try
      {
         return annotationImplType.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   protected AroundInvoke getAroundInvokeAnnotation(AroundInvokesMetaData callbacks, DeclaredMethodSignature method)
   {
      if(callbacks == null || callbacks.isEmpty())
         return null;
      
      for(AroundInvokeMetaData callback : callbacks)
      {
         String callbackClassName = callback.getClassName();
         if(isEmpty(callbackClassName) || callbackClassName.equals(method.getDeclaringClass()))
         {
            String callbackMethodName = callback.getMethodName();
            if(method.getName().equals(callbackMethodName))
               return new AroundInvokeImpl();
         }
      }
      return null;
   }
   
   protected <T extends Annotation> T getLifeCycleAnnotation(LifecycleCallbacksMetaData callbacks, Class<T> annotationImplType, DeclaredMethodSignature method)
   {
      if(callbacks == null || callbacks.isEmpty())
         return null;
      
      for(LifecycleCallbackMetaData callback : callbacks)
      {
         String callbackClassName = callback.getClassName();
         if(isEmpty(callbackClassName) || callbackClassName.equals(method.getDeclaringClass()))
         {
            String callbackMethodName = callback.getMethodName();
            if(method.getName().equals(callbackMethodName))
               return createAnnotationImpl(annotationImplType);
         }
      }
      return null;
   }
   
   private static boolean isEmpty(String s)
   {
      return s == null || s.length() == 0;
   }
   
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, M metaData, ClassLoader classLoader)
   {
      return null;
   }

   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, M metaData, ClassLoader classLoader, DeclaredMethodSignature method)
   {
      if(log.isTraceEnabled()) log.trace("retrieve annotation " + annotationClass + " on " + metaData + " for " + method);
      if(annotationClass == PostConstruct.class)
      {
         PostConstruct lifeCycleAnnotation = getLifeCycleAnnotation(metaData.getPostConstructs(), PostConstructImpl.class, method);
         if(lifeCycleAnnotation != null)
            return annotationClass.cast(lifeCycleAnnotation);
      }
      else if(annotationClass == PreDestroy.class)
      {
         PreDestroy lifeCycleAnnotation = getLifeCycleAnnotation(metaData.getPreDestroys(), PreDestroyImpl.class, method);
         if(lifeCycleAnnotation != null)
            return annotationClass.cast(lifeCycleAnnotation);
      }
      return null;
   }
}
