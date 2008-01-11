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
package org.jboss.ejb3.metadata.plugins.loader;

import java.lang.annotation.Annotation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundInvoke;

import org.jboss.ejb3.annotation.impl.AroundInvokeImpl;
import org.jboss.ejb3.annotation.impl.PostConstructImpl;
import org.jboss.ejb3.annotation.impl.PreDestroyImpl;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.AroundInvokesMetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.spi.retrieval.AnnotationItem;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.retrieval.simple.SimpleAnnotationItem;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.metadata.spi.signature.MethodSignature;
import org.jboss.metadata.spi.signature.Signature;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorClassMetaDataLoader extends ClassMetaDataLoader
{
   private static final Logger log = Logger.getLogger(InterceptorClassMetaDataLoader.class);
   
   /**
    * MethodMetaDataRetrieval.
    */
   protected class MethodMetaDataRetrieval extends AbstractMethodMetaDataLoader
   {
      /** The signature */
      private MethodSignature signature;
      
      /**
       * Create a new MethodMetaDataRetrieval.
       * 
       * @param methodSignature the signature
       */
      public MethodMetaDataRetrieval(MethodSignature methodSignature)
      {
         this.signature = methodSignature;
      }

      private <T extends Annotation> T createAnnotationImpl(Class<T> annotationImplType)
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
      
      private AroundInvoke getAroundInvokeAnnotation(AroundInvokesMetaData callbacks)
      {
         if(callbacks == null || callbacks.isEmpty())
            return null;
         
         assert callbacks.size() == 1;
         String methodName = callbacks.get(0).getMethodName();
         if(methodName.equals(signature.getName()))
            return new AroundInvokeImpl();
         return null;
      }
      
      private <T extends Annotation> T getLifeCycleAnnotation(LifecycleCallbacksMetaData callbacks, Class<T> annotationImplType)
      {
         if(callbacks == null || callbacks.isEmpty())
            return null;
         
         assert callbacks.size() == 1;
         String methodName = callbacks.get(0).getMethodName();
         if(methodName.equals(signature.getName()))
            return createAnnotationImpl(annotationImplType);
         return null;
      }
      
      public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
      {
         if(annotationType == AroundInvoke.class)
         {
            Annotation annotation = getAroundInvokeAnnotation(interceptorMetaData.getAroundInvokes());
            if(annotation != null)
               return new SimpleAnnotationItem<T>(annotationType.cast(annotation));
         }
         if(annotationType == PostConstruct.class)
         {
            Annotation lifeCycleAnnotation = getLifeCycleAnnotation(interceptorMetaData.getPostConstructs(), PostConstructImpl.class);
            if(lifeCycleAnnotation != null)
               return new SimpleAnnotationItem<T>(annotationType.cast(lifeCycleAnnotation));
         }
         else if(annotationType == PreDestroy.class)
         {
            Annotation lifeCycleAnnotation = getLifeCycleAnnotation(interceptorMetaData.getPreDestroys(), PreDestroyImpl.class);
            if(lifeCycleAnnotation != null)
               return new SimpleAnnotationItem<T>(annotationType.cast(lifeCycleAnnotation));
         }
         /* Example 
         JBossEnterpriseBeanMetaData beanMetaData = getBeanMetaData();
         if (beanMetaData == null)
            return null;
         
         if (annotationType == TransactionTimeout.class)
         {
            MethodAttributesMetaData methodAttributes = beanMetaData.getMethodAttributes();
            int timeout = methodAttributes.getMethodTransactionTimeout(signature.getName());
            return new SimpleAnnotationItem(new TransactionTimeoutImpl(timeout));
         }
         */
         return null;
      }
   }
   
   private InterceptorMetaData interceptorMetaData;
   
   public InterceptorClassMetaDataLoader(ScopeKey key, InterceptorMetaData interceptorMetaData)
   {
      super(key);
      assert interceptorMetaData != null;
      this.interceptorMetaData = interceptorMetaData;
   }
   
   @Override
   protected MetaDataRetrieval createComponentMetaDataRetrieval(Signature signature)
   {
      if(signature instanceof MethodSignature)
         return new MethodMetaDataRetrieval((MethodSignature) signature);
      return null;
   }
}
