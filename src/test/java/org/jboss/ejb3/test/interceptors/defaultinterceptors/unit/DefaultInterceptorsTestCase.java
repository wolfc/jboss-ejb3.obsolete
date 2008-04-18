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
package org.jboss.ejb3.test.interceptors.defaultinterceptors.unit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.AbstractDirectContainer;
import org.jboss.ejb3.interceptors.metadata.BeanInterceptorMetaDataBridge;
import org.jboss.ejb3.interceptors.metadata.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.interceptors.metadata.InterceptorMetaDataBridge;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.test.interceptors.defaultinterceptors.AnnotatedBean;
import org.jboss.ejb3.test.interceptors.defaultinterceptors.ClassInterceptor;
import org.jboss.ejb3.test.interceptors.defaultinterceptors.DefaultInterceptor;
import org.jboss.ejb3.test.interceptors.defaultinterceptors.Interceptions;
import org.jboss.ejb3.test.interceptors.defaultinterceptors.MethodInterceptor;
import org.jboss.ejb3.test.interceptors.defaultinterceptors.XMLBean;
import org.jboss.ejb3.test.interceptors.defaultinterceptors.XMLOrderedBean;
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
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class DefaultInterceptorsTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(DefaultInterceptorsTestCase.class);
   
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
   }

   @Override
   protected void tearDown() throws Exception
   {
      log.info(deployer.undeploy());
   }
   
   public void test() throws Throwable
   {
      AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(AnnotatedBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("defaultinterceptors/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData annotatedBeanMetaData = jbossMetaData.getEnterpriseBean("AnnotatedBean");
      assertNotNull(annotatedBeanMetaData);
      MyContainer<AnnotatedBean> annotatedBeanContainer = new MyContainer<AnnotatedBean>("AnnotatedBean", "Test", AnnotatedBean.class, annotatedBeanMetaData);
      annotatedBeanContainer.testAdvisor();
      BeanContext<AnnotatedBean> annotatedBean = annotatedBeanContainer.construct();

      JBossEnterpriseBeanMetaData xmlBeanMetaData = jbossMetaData.getEnterpriseBean("XMLBean");
      assertNotNull(xmlBeanMetaData);
      MyContainer<XMLBean> xmlBeanContainer = new MyContainer<XMLBean>("XMLBean", "Test", XMLBean.class, xmlBeanMetaData);
      xmlBeanContainer.testAdvisor();
      BeanContext<XMLBean> xmlBean = xmlBeanContainer.construct();

      JBossEnterpriseBeanMetaData xmlOrderedBeanMetaData = jbossMetaData.getEnterpriseBean("XMLOrderedBean");
      assertNotNull(xmlOrderedBeanMetaData);
      MyContainer<XMLOrderedBean> xmlOrderedBeanContainer = new MyContainer<XMLOrderedBean>("XMLOrderedBean", "Test", XMLOrderedBean.class, xmlOrderedBeanMetaData);
      xmlOrderedBeanContainer.testAdvisor();
      BeanContext<XMLOrderedBean> xmlOrderedBean = xmlOrderedBeanContainer.construct();

      Interceptions.clear();
      annotatedBeanContainer.invoke(annotatedBean, "defaultOrderMethod", new Object[0]);
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals("Interceptions were " + interceptions, 4, interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(MethodInterceptor.class, interceptions.get(2));
      assertEquals(AnnotatedBean.class, interceptions.get(3));
      
      Interceptions.clear();
      annotatedBeanContainer.invoke(annotatedBean, "xmlOrderedMethod", new Object[] {1, "Hello"});
      interceptions = Interceptions.getInterceptions();
      assertEquals("Interceptions were " + interceptions, 4, interceptions.size());      
      assertEquals(MethodInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(DefaultInterceptor.class, interceptions.get(2));
      assertEquals(AnnotatedBean.class, interceptions.get(3));
      
      Interceptions.clear();
      xmlBeanContainer.invoke(xmlBean, "method", new Object[0]);
      interceptions = Interceptions.getInterceptions();
      assertEquals("Interceptions were " + interceptions, 3, interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(XMLBean.class, interceptions.get(2));
      
      Interceptions.clear();
      xmlOrderedBeanContainer.invoke(xmlOrderedBean, "method", new Object[0]);
      interceptions = Interceptions.getInterceptions();
      assertEquals("Interceptions were " + interceptions, 3, interceptions.size());      
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(DefaultInterceptor.class, interceptions.get(1));
      assertEquals(XMLOrderedBean.class, interceptions.get(2));
      
      
      log.info("======= Done");
   }
   
}
