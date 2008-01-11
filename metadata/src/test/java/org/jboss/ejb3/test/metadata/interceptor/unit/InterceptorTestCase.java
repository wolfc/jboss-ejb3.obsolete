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
package org.jboss.ejb3.test.metadata.interceptor.unit;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import junit.framework.TestCase;

import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.test.metadata.interceptor.BeanInterceptorMetaDataBridge;
import org.jboss.ejb3.test.metadata.interceptor.DummyInterceptor;
import org.jboss.ejb3.test.metadata.interceptor.InterceptedBean;
import org.jboss.ejb3.test.metadata.interceptor.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.test.metadata.interceptor.InterceptorMetaDataBridge;
import org.jboss.ejb3.test.metadata.securitydomain.SecurityDomainBean;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.spec.EjbJar30MetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.builder.JBossXBBuilder;
import org.w3c.dom.ls.LSInput;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorTestCase extends TestCase
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(InterceptorTestCase.class);
   
   protected static SchemaBindingResolver schemaResolverForClass(final Class<?> root)
   {
      return new SchemaBindingResolver()
      {
         public String getBaseURI()
         {
            return null;
         }

         public SchemaBinding resolve(String nsUri, String baseURI, String schemaLocation)
         {
            return JBossXBBuilder.build(root);
         }

         public LSInput resolveAsLSInput(String nsUri, String baseUri, String schemaLocation)
         {
            return null;
         }

         public void setBaseURI(String baseURI)
         {
         }
      };
   }

   private void assertArrayEquals(Object expected[], Object actual[])
   {
      if(expected == actual)
         return;
      assertEquals(expected.length, actual.length);
      for(int i = 0; i < expected.length; i++)
      {
         assertEquals(expected[i], actual[i]);
      }
   }
   
   public void test1() throws Exception
   {
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("interceptor/ejb-jar.xml");
      EjbJar30MetaData ejbJarMetaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData metaData = new JBoss50MetaData();
      metaData.merge(null, ejbJarMetaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = metaData.getEnterpriseBean("InterceptedBean");
      assertNotNull("beanMetaData is null", beanMetaData);
      
      // Bootstrap meta data bridge
      String canonicalObjectName = "Not important";
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      AnnotationRepositoryToMetaData repository = new AnnotationRepositoryToMetaData(SecurityDomainBean.class, beanMetaData, canonicalObjectName, classLoader);
      List<MetaDataBridge<InterceptorMetaData>> interceptorBridges = new ArrayList<MetaDataBridge<InterceptorMetaData>>();
      interceptorBridges.add(new InterceptorMetaDataBridge());
      repository.addComponentMetaDataLoaderFactory(new InterceptorComponentMetaDataLoaderFactory(interceptorBridges));
      repository.addMetaDataBridge(new BeanInterceptorMetaDataBridge());
      
      Interceptors interceptors = (Interceptors) repository.resolveClassAnnotation(Interceptors.class);
      assertNotNull(interceptors);
      Class<?> expected[] = { DummyInterceptor.class };
      assertArrayEquals(expected, interceptors.value());
      
      Class<?> parameterTypes[] = { InvocationContext.class };
      
      Method aroundInvoke = DummyInterceptor.class.getMethod("aroundInvoke", parameterTypes);
      assertTrue(repository.hasAnnotation(DummyInterceptor.class, aroundInvoke, AroundInvoke.class));
      assertFalse(repository.hasAnnotation(DummyInterceptor.class, aroundInvoke, PostConstruct.class));
      
      Method postConstruct = DummyInterceptor.class.getMethod("postConstruct", parameterTypes);
      assertTrue(repository.hasAnnotation(DummyInterceptor.class, postConstruct, PostConstruct.class));
      
      Method preDestroy = DummyInterceptor.class.getMethod("preDestroy", parameterTypes);
      assertTrue(repository.hasAnnotation(DummyInterceptor.class, preDestroy, PreDestroy.class));
      
      Method beanAroundInvoke = InterceptedBean.class.getMethod("aroundInvoke", InvocationContext.class);
      assertTrue(repository.hasAnnotation(beanAroundInvoke, AroundInvoke.class));
   }
}
