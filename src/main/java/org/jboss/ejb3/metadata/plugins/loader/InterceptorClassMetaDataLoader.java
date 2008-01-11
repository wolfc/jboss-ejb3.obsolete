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

import javax.interceptor.AroundInvoke;

import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
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
public class InterceptorClassMetaDataLoader extends ClassMetaDataLoader<InterceptorMetaData>
{
   private static final Logger log = Logger.getLogger(InterceptorClassMetaDataLoader.class);
   
   /**
    * MethodMetaDataRetrieval.
    */
   protected class MethodMetaDataRetrieval extends ClassMetaDataLoader<InterceptorMetaData>.MethodMetaDataRetrieval
   {
      /**
       * Create a new MethodMetaDataRetrieval.
       * 
       * @param methodSignature the signature
       */
      public MethodMetaDataRetrieval(MethodSignature methodSignature)
      {
         super(methodSignature);
      }

      @Override
      public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
      {
         if(annotationType == AroundInvoke.class)
         {
            Annotation annotation = getAroundInvokeAnnotation(interceptorMetaData.getAroundInvokes());
            if(annotation != null)
               return new SimpleAnnotationItem<T>(annotationType.cast(annotation));
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
         return super.retrieveAnnotation(annotationType);
      }
   }
   
   private InterceptorMetaData interceptorMetaData;
   
   public InterceptorClassMetaDataLoader(ScopeKey key, InterceptorMetaData interceptorMetaData)
   {
      super(key, interceptorMetaData);
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
