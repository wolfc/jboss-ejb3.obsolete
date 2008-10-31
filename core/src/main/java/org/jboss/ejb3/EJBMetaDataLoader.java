/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.ejb3;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.impl.SecurityDomainImpl;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.plugins.loader.BasicMetaDataLoader;
import org.jboss.metadata.spi.retrieval.AnnotationItem;
import org.jboss.metadata.spi.retrieval.AnnotationsItem;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.retrieval.simple.SimpleAnnotationItem;
import org.jboss.metadata.spi.retrieval.simple.SimpleAnnotationsItem;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.metadata.spi.signature.MethodSignature;
import org.jboss.metadata.spi.signature.Signature;

/**
 * EJBMetaDataLoader.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class EJBMetaDataLoader extends BasicMetaDataLoader
{
   /** The container */
   private EJBContainer container;
   
   /** Component cache */
   private Map<Signature, MetaDataRetrieval> cache = new ConcurrentHashMap<Signature, MetaDataRetrieval>();
   
   /**
    * Create a new EJBMetaDataLoader.
    * 
    * @param key the scope
    * @param container the container
    */
   public EJBMetaDataLoader(ScopeKey key, EJBContainer container)
   {
      super(key);
      if (container == null)
         throw new IllegalArgumentException("Null container");
      this.container = container;
   }
   
   /**
    * Get the bean metadata
    * 
    * @return the bean metadata
    */
   protected JBossEnterpriseBeanMetaData getBeanMetaData()
   {
      return container.getXml();
   }
   
   public MetaDataRetrieval getComponentMetaDataRetrieval(Signature signature)
   {
      JBossEnterpriseBeanMetaData beanMetaData = getBeanMetaData();
      if (beanMetaData == null)
         return null;

      if (signature instanceof MethodSignature == false)
         return null;
      
      MetaDataRetrieval retrieval = cache.get(signature);
      if (retrieval != null)
         return retrieval;

      retrieval = new MethodMetaDataRetrieval((MethodSignature) signature);
      cache.put(signature, retrieval);
      return retrieval;
   }

   public boolean isEmpty()
   {
      return getBeanMetaData() != null;
   }

   public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
   {
      JBossEnterpriseBeanMetaData beanMetaData = getBeanMetaData();
      if (beanMetaData == null)
         return null;
      
      if (annotationType == SecurityDomain.class)
      {
         String securityDomain = beanMetaData.getSecurityDomain();
         if (securityDomain != null)
            return new SimpleAnnotationItem(new SecurityDomainImpl(securityDomain));
      }
      return null;
   }
   
   public AnnotationsItem retrieveAnnotations()
   {
      List<AnnotationItem> annotations = new ArrayList<AnnotationItem>();
      AnnotationItem annotation = retrieveAnnotation(SecurityDomain.class);
      if (annotation != null)
         annotations.add(annotation);
      if (annotations.isEmpty())
         return SimpleAnnotationsItem.NO_ANNOTATIONS;
      else
         return new SimpleAnnotationsItem(annotations.toArray(new AnnotationItem[annotations.size()]));
   }

   /**
    * MethodMetaDataRetrieval.
    */
   private class MethodMetaDataRetrieval extends BasicMetaDataLoader
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

      public MetaDataRetrieval getComponentMetaDataRetrieval(Signature signature)
      {
         return null;
      }

      public boolean isEmpty()
      {
         return false;
      }

      public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
      {
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

      public AnnotationsItem retrieveAnnotations()
      {
         // TODO
         return SimpleAnnotationsItem.NO_ANNOTATIONS;
      }
   }
}
