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
import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.metadata.ComponentMetaDataLoaderFactory;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.logging.Logger;
import org.jboss.metadata.spi.retrieval.AnnotationItem;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.retrieval.simple.SimpleAnnotationItem;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.metadata.spi.signature.DeclaredMethodSignature;
import org.jboss.metadata.spi.signature.Signature;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class BridgedMetaDataLoader<M> extends AbstractMetaDataLoader
{
   private static final Logger log = Logger.getLogger(BridgedMetaDataLoader.class);
   
   /**
    * MethodMetaDataRetrieval.
    */
   private class MethodMetaDataRetrieval extends AbstractMethodMetaDataLoader
   {
      /** The signature */
      private DeclaredMethodSignature signature;
      
      /**
       * Create a new MethodMetaDataRetrieval.
       * 
       * @param methodSignature the signature
       */
      public MethodMetaDataRetrieval(DeclaredMethodSignature methodSignature)
      {
         this.signature = methodSignature;
      }

      public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
      {
         if(metaData == null)
            return null;
         
         for(MetaDataBridge<M> bridge : bridges)
         {
            T annotation = bridge.retrieveAnnotation(annotationType, metaData, classLoader, signature);
            if(annotation != null)
               return new SimpleAnnotationItem<T>(annotation);
         }
         return null;
      }
   }
   
   private List<ComponentMetaDataLoaderFactory<M>> factories = new ArrayList<ComponentMetaDataLoaderFactory<M>>();
   
   private List<MetaDataBridge<M>> bridges = new ArrayList<MetaDataBridge<M>>();
   
   private M metaData;

   private ClassLoader classLoader;
   
   /**
    * 
    * @param key
    * @param metaData       the meta data or null
    * @param classLoader
    */
   public BridgedMetaDataLoader(ScopeKey key, M metaData, ClassLoader classLoader)
   {
      this(key, metaData, classLoader, null);
   }
   
   public BridgedMetaDataLoader(ScopeKey key, M metaData, ClassLoader classLoader, List<MetaDataBridge<M>> defaultBridges)
   {
      super(key);
      
      assert classLoader != null : "classLoader is null";
      
      this.metaData = metaData;
      this.classLoader = classLoader;
      if(defaultBridges != null)
         bridges.addAll(defaultBridges);
   }
   
   public boolean addComponentMetaDataLoaderFactory(ComponentMetaDataLoaderFactory<M> componentMetaDataLoaderFactory)
   {
      return factories.add(componentMetaDataLoaderFactory);
   }
   
   public boolean addMetaDataBridge(MetaDataBridge<M> bridge)
   {
      return bridges.add(bridge);
   }
   
   @Override
   public MetaDataRetrieval getComponentMetaDataRetrieval(Signature signature)
   {
      if(metaData == null)
         return null;
      
      for(ComponentMetaDataLoaderFactory<M> factory : factories)
      {
         MetaDataRetrieval retrieval = factory.createComponentMetaDataRetrieval(metaData, signature, getScope(), classLoader);
         if(retrieval != null)
            return retrieval;
      }
      
      // TODO: shouldn't this be a factory?
      if(signature instanceof DeclaredMethodSignature)
         return new MethodMetaDataRetrieval((DeclaredMethodSignature) signature);
      
      return super.getComponentMetaDataRetrieval(signature);
   }
   
   @Override
   public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
   {
      if(metaData == null)
         return null;
      
      // Resources, EJBs etc
      for(MetaDataBridge<M> bridge : bridges)
      {
         T annotation = bridge.retrieveAnnotation(annotationType, metaData, classLoader);
         if(annotation != null)
            return new SimpleAnnotationItem<T>(annotation);
      }
      return null;
   }
}
