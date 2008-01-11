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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundInvoke;

import org.jboss.ejb3.annotation.impl.AroundInvokeImpl;
import org.jboss.ejb3.annotation.impl.PostConstructImpl;
import org.jboss.ejb3.annotation.impl.PreDestroyImpl;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.AroundInvokesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.plugins.loader.BasicMetaDataLoader;
import org.jboss.metadata.spi.retrieval.AnnotationItem;
import org.jboss.metadata.spi.retrieval.AnnotationsItem;
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
public abstract class ClassMetaDataLoader<M extends Environment> extends BasicMetaDataLoader
{
   private static final Logger log = Logger.getLogger(ClassMetaDataLoader.class);

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
      
      protected AroundInvoke getAroundInvokeAnnotation(AroundInvokesMetaData callbacks)
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
         if(annotationType == PostConstruct.class)
         {
            Annotation lifeCycleAnnotation = getLifeCycleAnnotation(metaData.getPostConstructs(), PostConstructImpl.class);
            if(lifeCycleAnnotation != null)
               return new SimpleAnnotationItem<T>(annotationType.cast(lifeCycleAnnotation));
         }
         else if(annotationType == PreDestroy.class)
         {
            Annotation lifeCycleAnnotation = getLifeCycleAnnotation(metaData.getPreDestroys(), PreDestroyImpl.class);
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
   
   /** The meta data */
   private M metaData;
   
   /** Component cache */
   private Map<Signature, MetaDataRetrieval> cache = new ConcurrentHashMap<Signature, MetaDataRetrieval>();
   
   protected abstract MetaDataRetrieval createComponentMetaDataRetrieval(Signature signature);
   
   protected ClassMetaDataLoader(ScopeKey key, M metaData)
   {
      super(key);
      this.metaData = metaData;
   }
   
   @Override
   public MetaDataRetrieval getComponentMetaDataRetrieval(Signature signature)
   {
      MetaDataRetrieval retrieval = cache.get(signature);
      if (retrieval != null)
         return retrieval;
      
      retrieval = createComponentMetaDataRetrieval(signature);
      
      if(retrieval != null)
         cache.put(signature, retrieval);
      
      return retrieval;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.retrieval.MetaDataRetrieval#isEmpty()
    */
   public boolean isEmpty()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.retrieval.MetaDataRetrieval#retrieveAnnotations()
    */
   public AnnotationsItem retrieveAnnotations()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   @Override
   public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
   {
      // Resources, EJBs etc
      return null;
   }
}
