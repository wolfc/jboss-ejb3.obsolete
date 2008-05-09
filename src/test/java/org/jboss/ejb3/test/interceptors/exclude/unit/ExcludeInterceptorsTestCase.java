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
package org.jboss.ejb3.test.interceptors.exclude.unit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.AbstractDirectContainer;
import org.jboss.ejb3.interceptors.metadata.AdditiveBeanInterceptorMetaDataBridge;
import org.jboss.ejb3.interceptors.metadata.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.interceptors.metadata.InterceptorMetaDataBridge;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.test.interceptors.exclude.AnnotatedAllInterceptorsBean;
import org.jboss.ejb3.test.interceptors.exclude.AnnotatedExcludeClassAndDefaultBean;
import org.jboss.ejb3.test.interceptors.exclude.AnnotatedExcludeClassAndDefaultForMethodBean;
import org.jboss.ejb3.test.interceptors.exclude.AnnotatedExcludeClassBean;
import org.jboss.ejb3.test.interceptors.exclude.AnnotatedExcludeDefaultBean;
import org.jboss.ejb3.test.interceptors.exclude.AnnotatedExcludeDefaultForMethodBean;
import org.jboss.ejb3.test.interceptors.exclude.ClassInterceptor;
import org.jboss.ejb3.test.interceptors.exclude.DefaultInterceptor;
import org.jboss.ejb3.test.interceptors.exclude.Interceptions;
import org.jboss.ejb3.test.interceptors.exclude.MethodInterceptor;
import org.jboss.ejb3.test.interceptors.exclude.XmlAllInterceptorsBean;
import org.jboss.ejb3.test.interceptors.exclude.XmlExcludeClassAndDefaultBean;
import org.jboss.ejb3.test.interceptors.exclude.XmlExcludeClassAndDefaultForMethodBean;
import org.jboss.ejb3.test.interceptors.exclude.XmlExcludeClassBean;
import org.jboss.ejb3.test.interceptors.exclude.XmlExcludeDefaultBean;
import org.jboss.ejb3.test.interceptors.exclude.XmlExcludeDefaultForMethodBean;
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
public class ExcludeInterceptorsTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(ExcludeInterceptorsTestCase.class);
   
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
         annotations.addMetaDataBridge(new AdditiveBeanInterceptorMetaDataBridge(beanClass, classLoader, beanMetaData));
         
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

   public void testAnnotatedAllInterceptors() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(AnnotatedAllInterceptorsBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("AnnotatedAllInterceptorsBean");
      assertNotNull(beanMetaData);
      MyContainer<AnnotatedAllInterceptorsBean> container = new MyContainer<AnnotatedAllInterceptorsBean>("AnnotatedAllInterceptorsBean", "Test", AnnotatedAllInterceptorsBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<AnnotatedAllInterceptorsBean> bean = container.construct();
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedAllInterceptorsBean.class, interceptions.get(2));
 
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(AnnotatedAllInterceptorsBean.class.getName(), ret);
      assertEquals(4,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(MethodInterceptor.class, interceptions.get(2));
      assertEquals(AnnotatedAllInterceptorsBean.class, interceptions.get(3));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedAllInterceptorsBean.class, interceptions.get(2));
      
      log.info("======= Done");
   }
   

   public void testAnnotatedExcludeDefaultInterceptors() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(AnnotatedExcludeDefaultBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("AnnotatedExcludeDefaultBean");
      assertNotNull(beanMetaData);
      MyContainer<AnnotatedExcludeDefaultBean> container = new MyContainer<AnnotatedExcludeDefaultBean>("AnnotatedExcludeDefaultBean", "Test", AnnotatedExcludeDefaultBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<AnnotatedExcludeDefaultBean> bean = container.construct();
      assertEquals(2,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(AnnotatedExcludeDefaultBean.class, interceptions.get(1));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(AnnotatedExcludeDefaultBean.class.getName(), ret);
      assertEquals(3,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(MethodInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedExcludeDefaultBean.class, interceptions.get(2));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(2,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(AnnotatedExcludeDefaultBean.class, interceptions.get(1));
      
      log.info("======= Done");
   }

   public void testAnnotatedExcludeDefaultInterceptorsForMethod() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(AnnotatedExcludeDefaultForMethodBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("AnnotatedExcludeDefaultForMethodBean");
      assertNotNull(beanMetaData);
      MyContainer<AnnotatedExcludeDefaultForMethodBean> container = new MyContainer<AnnotatedExcludeDefaultForMethodBean>("AnnotatedExcludeDefaultForMethodBean", "Test", AnnotatedExcludeDefaultForMethodBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<AnnotatedExcludeDefaultForMethodBean> bean = container.construct();
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedExcludeDefaultForMethodBean.class, interceptions.get(2));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(AnnotatedExcludeDefaultForMethodBean.class.getName(), ret);
      assertEquals(3,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(MethodInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedExcludeDefaultForMethodBean.class, interceptions.get(2));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedExcludeDefaultForMethodBean.class, interceptions.get(2));
      
      log.info("======= Done");
   }
   
   public void testAnnotatedExcludeClassInterceptors() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(AnnotatedExcludeClassBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("AnnotatedExcludeClassBean");
      assertNotNull(beanMetaData);
      MyContainer<AnnotatedExcludeClassBean> container = new MyContainer<AnnotatedExcludeClassBean>("AnnotatedExcludeClassBean", "Test", AnnotatedExcludeClassBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<AnnotatedExcludeClassBean> bean = container.construct();
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedExcludeClassBean.class, interceptions.get(2));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(AnnotatedExcludeClassBean.class.getName(), ret);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(MethodInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedExcludeClassBean.class, interceptions.get(2));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedExcludeClassBean.class, interceptions.get(2));
      
      log.info("======= Done");
   }
   
   public void testAnnotatedExcludeClassAndDefaultInterceptors() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(AnnotatedExcludeClassAndDefaultBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("AnnotatedExcludeClassAndDefaultBean");
      assertNotNull(beanMetaData);
      MyContainer<AnnotatedExcludeClassAndDefaultBean> container = new MyContainer<AnnotatedExcludeClassAndDefaultBean>("AnnotatedExcludeClassAndDefaultBean", "Test", AnnotatedExcludeClassAndDefaultBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<AnnotatedExcludeClassAndDefaultBean> bean = container.construct();
      assertEquals(2,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(AnnotatedExcludeClassAndDefaultBean.class, interceptions.get(1));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(AnnotatedExcludeClassAndDefaultBean.class.getName(), ret);
      assertEquals(2,  interceptions.size());
      assertEquals(MethodInterceptor.class, interceptions.get(0));
      assertEquals(AnnotatedExcludeClassAndDefaultBean.class, interceptions.get(1));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(2,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(AnnotatedExcludeClassAndDefaultBean.class, interceptions.get(1));
      
      log.info("======= Done");
   }
   
   public void testAnnotatedExcludeClassAndDefaultInterceptorsForMethod() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(AnnotatedExcludeClassAndDefaultForMethodBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("AnnotatedExcludeClassAndDefaultForMethodBean");
      assertNotNull(beanMetaData);
      MyContainer<AnnotatedExcludeClassAndDefaultForMethodBean> container = new MyContainer<AnnotatedExcludeClassAndDefaultForMethodBean>("AnnotatedExcludeClassAndDefaultForMethodBean", "Test", AnnotatedExcludeClassAndDefaultForMethodBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<AnnotatedExcludeClassAndDefaultForMethodBean> bean = container.construct();
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedExcludeClassAndDefaultForMethodBean.class, interceptions.get(2));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(AnnotatedExcludeClassAndDefaultForMethodBean.class.getName(), ret);
      assertEquals(2,  interceptions.size());
      assertEquals(MethodInterceptor.class, interceptions.get(0));
      assertEquals(AnnotatedExcludeClassAndDefaultForMethodBean.class, interceptions.get(1));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(AnnotatedExcludeClassAndDefaultForMethodBean.class, interceptions.get(2));
      
      log.info("======= Done");
   }
   
   public void testXmlAllInterceptors() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(AnnotatedAllInterceptorsBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("XmlAllInterceptorsBean");
      assertNotNull(beanMetaData);
      MyContainer<XmlAllInterceptorsBean> container = new MyContainer<XmlAllInterceptorsBean>("XmlAllInterceptorsBean", "Test", XmlAllInterceptorsBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<XmlAllInterceptorsBean> bean = container.construct();
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(XmlAllInterceptorsBean.class, interceptions.get(2));
 
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(XmlAllInterceptorsBean.class.getName(), ret);
      assertEquals(4,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(MethodInterceptor.class, interceptions.get(2));
      assertEquals(XmlAllInterceptorsBean.class, interceptions.get(3));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(XmlAllInterceptorsBean.class, interceptions.get(2));
      
      log.info("======= Done");
   }
   
   public void testXmlExcludeDefaultInterceptors() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(XmlExcludeDefaultBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("XmlExcludeDefaultBean");
      assertNotNull(beanMetaData);
      MyContainer<XmlExcludeDefaultBean> container = new MyContainer<XmlExcludeDefaultBean>("XmlExcludeDefaultBean", "Test", XmlExcludeDefaultBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<XmlExcludeDefaultBean> bean = container.construct();
      assertEquals(2,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(XmlExcludeDefaultBean.class, interceptions.get(1));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(XmlExcludeDefaultBean.class.getName(), ret);
      assertEquals(3,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(MethodInterceptor.class, interceptions.get(1));
      assertEquals(XmlExcludeDefaultBean.class, interceptions.get(2));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(2,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(XmlExcludeDefaultBean.class, interceptions.get(1));
      
      log.info("======= Done");
   }

   public void testXmlExcludeDefaultInterceptorsForMethod() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(XmlExcludeDefaultForMethodBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("XmlExcludeDefaultForMethodBean");
      assertNotNull(beanMetaData);
      MyContainer<XmlExcludeDefaultForMethodBean> container = new MyContainer<XmlExcludeDefaultForMethodBean>("XmlExcludeDefaultForMethodBean", "Test", XmlExcludeDefaultForMethodBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<XmlExcludeDefaultForMethodBean> bean = container.construct();
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(XmlExcludeDefaultForMethodBean.class, interceptions.get(2));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(XmlExcludeDefaultForMethodBean.class.getName(), ret);
      assertEquals(3,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(MethodInterceptor.class, interceptions.get(1));
      assertEquals(XmlExcludeDefaultForMethodBean.class, interceptions.get(2));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(XmlExcludeDefaultForMethodBean.class, interceptions.get(2));
      
      log.info("======= Done");
   }
   
   public void testXmlExcludeClassInterceptors() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(XmlExcludeClassBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("XmlExcludeClassBean");
      assertNotNull(beanMetaData);
      MyContainer<XmlExcludeClassBean> container = new MyContainer<XmlExcludeClassBean>("XmlExcludeClassBean", "Test", XmlExcludeClassBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<XmlExcludeClassBean> bean = container.construct();
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(XmlExcludeClassBean.class, interceptions.get(2));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(XmlExcludeClassBean.class.getName(), ret);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(MethodInterceptor.class, interceptions.get(1));
      assertEquals(XmlExcludeClassBean.class, interceptions.get(2));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(XmlExcludeClassBean.class, interceptions.get(2));
      
      log.info("======= Done");
   }
   
   public void testXmlExcludeClassAndDefaultInterceptors() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(XmlExcludeClassAndDefaultBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("XmlExcludeClassAndDefaultBean");
      assertNotNull(beanMetaData);
      MyContainer<XmlExcludeClassAndDefaultBean> container = new MyContainer<XmlExcludeClassAndDefaultBean>("XmlExcludeClassAndDefaultBean", "Test", XmlExcludeClassAndDefaultBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<XmlExcludeClassAndDefaultBean> bean = container.construct();
      assertEquals(2,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(XmlExcludeClassAndDefaultBean.class, interceptions.get(1));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(XmlExcludeClassAndDefaultBean.class.getName(), ret);
      assertEquals(2,  interceptions.size());
      assertEquals(MethodInterceptor.class, interceptions.get(0));
      assertEquals(XmlExcludeClassAndDefaultBean.class, interceptions.get(1));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(2,  interceptions.size());
      assertEquals(ClassInterceptor.class, interceptions.get(0));
      assertEquals(XmlExcludeClassAndDefaultBean.class, interceptions.get(1));
      
      log.info("======= Done");
   }
   
   public void testXmlExcludeClassAndDefaultInterceptorsForMethod() throws Throwable
   {
     AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(XmlExcludeClassAndDefaultForMethodBean.class.getClassLoader());
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("exclude/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("XmlExcludeClassAndDefaultForMethodBean");
      assertNotNull(beanMetaData);
      MyContainer<XmlExcludeClassAndDefaultForMethodBean> container = new MyContainer<XmlExcludeClassAndDefaultForMethodBean>("XmlExcludeClassAndDefaultForMethodBean", "Test", XmlExcludeClassAndDefaultForMethodBean.class, beanMetaData);
      container.testAdvisor();

      Interceptions.clear();
      ArrayList<Class<?>> interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      BeanContext<XmlExcludeClassAndDefaultForMethodBean> bean = container.construct();
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(XmlExcludeClassAndDefaultForMethodBean.class, interceptions.get(2));
 
      Interceptions.clear();
      interceptions = Interceptions.getInterceptions();
      assertEquals(0, interceptions.size());
      
      String ret = container.invoke(bean, "method");
      assertEquals(XmlExcludeClassAndDefaultForMethodBean.class.getName(), ret);
      assertEquals(2,  interceptions.size());
      assertEquals(MethodInterceptor.class, interceptions.get(0));
      assertEquals(XmlExcludeClassAndDefaultForMethodBean.class, interceptions.get(1));
      
      Interceptions.clear();
      assertEquals(0, interceptions.size());
      container.destroy(bean);
      assertEquals(3,  interceptions.size());
      assertEquals(DefaultInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(XmlExcludeClassAndDefaultForMethodBean.class, interceptions.get(2));
      
      log.info("======= Done");
   }
   
}
