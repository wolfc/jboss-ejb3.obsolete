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
package org.jboss.ejb3.test.interceptors.badbinding.unit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.interceptors.direct.AbstractDirectContainer;
import org.jboss.ejb3.interceptors.metadata.BeanInterceptorMetaDataBridge;
import org.jboss.ejb3.interceptors.metadata.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.interceptors.metadata.InterceptorMetaDataBridge;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.test.interceptors.common.CommonInterceptor;
import org.jboss.ejb3.test.interceptors.metadata.MetadataBean;
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
 * Test direct container advisement in combination with metadata.
 * 
 * There is no special class loader needed, because all invocations
 * are routed through the direct container.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 72166 $
 */
public class BadBindingTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(BadBindingTestCase.class);
   
   private class MyContainer<T> extends AbstractDirectContainer<T, MyContainer<T>>
   {
      public MyContainer(String name, String domainName, Class<? extends T> beanClass, JBossEnterpriseBeanMetaData beanMetaData)
      {
         super();
         
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         AnnotationRepositoryToMetaData annotations = new AnnotationRepositoryToMetaData(beanClass, beanMetaData, name, classLoader);
         List<MetaDataBridge<InterceptorMetaData>> interceptorBridges = new ArrayList<MetaDataBridge<InterceptorMetaData>>();
         interceptorBridges.add(new InterceptorMetaDataBridge());
         annotations.addComponentMetaDataLoaderFactory(new InterceptorComponentMetaDataLoaderFactory(interceptorBridges));
         annotations.addMetaDataBridge(new BeanInterceptorMetaDataBridge(beanClass, classLoader, beanMetaData));
         
         initializeAdvisor(name, getDomain(domainName), beanClass, annotations);
      }

      public void testAdvisor()
      {
         MyContainer<?> container = getAdvisor().getContainer();
         assertNotNull("container not set in managed object advisor", container);
         assertTrue(container == this);
      }
   }
   
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

   // FIXME: use the right jboss-aop.xml
   AOPDeployer deployer = new AOPDeployer("proxy/jboss-aop.xml");
   
   @Override
   protected void setUp() throws Exception
   {
      log.info(deployer.deploy());
      CommonInterceptor.aroundInvokes = 0;
      CommonInterceptor.postConstructs = 0;
      CommonInterceptor.preDestroys = 0;
   }

   @Override
   protected void tearDown() throws Exception
   {
      log.info(deployer.undeploy());
   }
   
   public void testBadBeanInterceptorBinding() throws Throwable
   {
      try
      {
         testBinding("badbinding/badbean-interceptor-binding/META-INF/ejb-jar.xml");
         fail("Should not have been able to create container since we have an interceptor-binding with the wrong name");
      }
      catch(Exception expected)
      {
      }
   }

   public void testDefaultInterceptorBinding() throws Throwable
   {
      testBinding("badbinding/default-interceptor-binding/META-INF/ejb-jar.xml");
   }
   
   public void testGoodBeanInterceptorBinding() throws Throwable
   {
      testBinding("badbinding/goodbean-interceptor-binding/META-INF/ejb-jar.xml");
   }

   public void testBadBeanInterceptorOrderBinding() throws Throwable
   {
      try
      {
         testBinding("badbinding/badbean-interceptor-order-binding/META-INF/ejb-jar.xml");
         fail("Should not have been able to create container since we have an interceptor-binding with the wrong name");
      }
      catch(Exception expected)
      {
      }
   }

   public void testDefaultInterceptorOrderBinding() throws Throwable
   {
      testBinding("badbinding/default-interceptor-order-binding/META-INF/ejb-jar.xml");
   }

   
   public void testGoodBeanInterceptorOrderBinding() throws Throwable
   {
      testBinding("badbinding/goodbean-interceptor-order-binding/META-INF/ejb-jar.xml");
   }

   public void testBadBeanInterceptorBindingWithMethod() throws Throwable
   {
      try
      {
         testBinding("badbinding/badbean-interceptor-binding-with-method/META-INF/ejb-jar.xml");
         fail("Should not have been able to create container since we have an interceptor-binding with the wrong name");
      }
      catch(Exception expected)
      {
      }
   }

   public void testGoodBeanInterceptorBindingWithMethod() throws Throwable
   {
      testBinding("badbinding/goodbean-interceptor-binding-with-method/META-INF/ejb-jar.xml");
   }

   public void testBadBeanInterceptorOrderBindingWithMethod() throws Throwable
   {
      try
      {
         testBinding("badbinding/badbean-interceptor-order-binding-with-method/META-INF/ejb-jar.xml");
         fail("Should not have been able to create container since we have an interceptor-binding with the wrong name");
      }
      catch(Exception expected)
      {
      }
   }

   public void testGoodBeanInterceptorOrderBindingWithMethod() throws Throwable
   {
      testBinding("badbinding/goodbean-interceptor-order-binding-with-method/META-INF/ejb-jar.xml");
   }

   private MyContainer<MetadataBean> testBinding(String ejbJarXml) throws Throwable
   {
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(MetadataBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource(ejbJarXml);
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("MetadataBean");
      assertNotNull(beanMetaData);
      
      assertEquals(0, CommonInterceptor.postConstructs);
      
      MyContainer<MetadataBean> container = new MyContainer<MetadataBean>("MetadataBean", "Test", MetadataBean.class, beanMetaData);

      container.testAdvisor();
      
      return container;
   }
}
