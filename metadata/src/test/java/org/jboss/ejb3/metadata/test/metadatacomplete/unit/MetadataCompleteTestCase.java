/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.metadata.test.metadatacomplete.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.metadata.test.metadatacomplete.TwoLifecycleMethodsBean;
import org.jboss.ejb3.test.metadata.interceptor.BeanInterceptorMetaDataBridge;
import org.jboss.ejb3.test.metadata.interceptor.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.test.metadata.interceptor.InterceptorMetaDataBridge;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnvironmentRefsGroupMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.junit.Test;

/**
 * Test to make sure that setting metadata complete to true in the descriptor
 * will make the annotation repository ignore real annotations.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MetadataCompleteTestCase
{
   @Test
   public void testAnnotations() throws Exception
   {
      JBossEnterpriseBeanMetaData beanMetaData = null;
      
      // Bootstrap meta data bridge
      String canonicalObjectName = "Not important";
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      AnnotationRepositoryToMetaData repository = new AnnotationRepositoryToMetaData(TwoLifecycleMethodsBean.class, beanMetaData, canonicalObjectName, classLoader);
      List<MetaDataBridge<InterceptorMetaData>> interceptorBridges = new ArrayList<MetaDataBridge<InterceptorMetaData>>();
      interceptorBridges.add(new InterceptorMetaDataBridge());
      repository.addComponentMetaDataLoaderFactory(new InterceptorComponentMetaDataLoaderFactory(interceptorBridges));
      repository.addMetaDataBridge(new BeanInterceptorMetaDataBridge());
      
      Method annotatedPostConstruct = TwoLifecycleMethodsBean.class.getMethod("annotatedPostConstruct");
      assertTrue("Failed to find the annotated post construct", repository.hasAnnotation(annotatedPostConstruct, PostConstruct.class));
      
      Method otherPostConstruct = TwoLifecycleMethodsBean.class.getMethod("otherPostConstruct");
      assertFalse(repository.hasAnnotation(otherPostConstruct, PostConstruct.class));
   }
   
   @Test
   public void testMetadataComplete() throws Exception
   {
      // Setup meta data
      LifecycleCallbackMetaData postConstruct = new LifecycleCallbackMetaData();
      postConstruct.setMethodName("otherPostConstruct");
      
      LifecycleCallbacksMetaData postConstructs = new LifecycleCallbacksMetaData();
      postConstructs.add(postConstruct);
      
      JBossEnvironmentRefsGroupMetaData env = new JBossEnvironmentRefsGroupMetaData();
      env.setPostConstructs(postConstructs);
      
      JBossSessionBeanMetaData beanMetaData = new JBossSessionBeanMetaData();
      beanMetaData.setEjbName("TwoLifecycleMethodsBean");
      beanMetaData.setJndiEnvironmentRefsGroup(env);
      
      JBossEnterpriseBeansMetaData enterpriseBeans = new JBossEnterpriseBeansMetaData();
      enterpriseBeans.add(beanMetaData);

      JBoss50MetaData metaData = new JBoss50MetaData();
      metaData.setMetadataComplete(true);
      metaData.setEnterpriseBeans(enterpriseBeans);
      
      // Bootstrap meta data bridge
      String canonicalObjectName = "Not important";
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      AnnotationRepositoryToMetaData repository = new AnnotationRepositoryToMetaData(TwoLifecycleMethodsBean.class, beanMetaData, canonicalObjectName, classLoader);
      List<MetaDataBridge<InterceptorMetaData>> interceptorBridges = new ArrayList<MetaDataBridge<InterceptorMetaData>>();
      interceptorBridges.add(new InterceptorMetaDataBridge());
      repository.addComponentMetaDataLoaderFactory(new InterceptorComponentMetaDataLoaderFactory(interceptorBridges));
      repository.addMetaDataBridge(new BeanInterceptorMetaDataBridge());
      
      Method annotatedPostConstruct = TwoLifecycleMethodsBean.class.getMethod("annotatedPostConstruct");
      assertFalse("Found annotated post construct, but metadata complete is true", repository.hasAnnotation(annotatedPostConstruct, PostConstruct.class));
      
      Method otherPostConstruct = TwoLifecycleMethodsBean.class.getMethod("otherPostConstruct");
      assertTrue(repository.hasAnnotation(otherPostConstruct, PostConstruct.class));
   }
}
