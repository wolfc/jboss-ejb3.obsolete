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
package org.jboss.ejb3.test.interceptors.inheritance.unit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.aop.AspectManager;
import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.AbstractDirectContainer;
import org.jboss.ejb3.interceptors.metadata.AdditiveBeanInterceptorMetaDataBridge;
import org.jboss.ejb3.interceptors.metadata.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.interceptors.metadata.InterceptorMetaDataBridge;
import org.jboss.ejb3.interceptors.proxy.ProxyContainer;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.test.interceptors.inheritance.AnnotatedBase;
import org.jboss.ejb3.test.interceptors.inheritance.AnnotatedBean;
import org.jboss.ejb3.test.interceptors.inheritance.ClassBaseInterceptor;
import org.jboss.ejb3.test.interceptors.inheritance.ClassInterceptor;
import org.jboss.ejb3.test.interceptors.inheritance.Interceptions;
import org.jboss.ejb3.test.interceptors.inheritance.MethodBaseInterceptor;
import org.jboss.ejb3.test.interceptors.inheritance.MethodInterceptor;
import org.jboss.ejb3.test.interceptors.inheritance.MyInterface;
import org.jboss.ejb3.test.interceptors.inheritance.XmlBase;
import org.jboss.ejb3.test.interceptors.inheritance.XmlBean;
import org.jboss.ejb3.test.interceptors.inheritance.XmlClassBaseInterceptor;
import org.jboss.ejb3.test.interceptors.inheritance.XmlClassInterceptor;
import org.jboss.ejb3.test.interceptors.inheritance.XmlMethodBaseInterceptor;
import org.jboss.ejb3.test.interceptors.inheritance.XmlMethodInterceptor;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.spec.EjbJar30MetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.builder.JBossXBBuilder;
import org.w3c.dom.ls.LSInput;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class InheritanceTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(InheritanceTestCase.class);
   
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
   
   public void testAnnotatedInheritanceAroundInvoke() throws Throwable
   {
      Thread.currentThread().setContextClassLoader(MyInterface.class.getClassLoader());
      
      ProxyContainer<AnnotatedBean> container = new ProxyContainer<AnnotatedBean>("AnnotatedContainer", "InterceptorContainer", AnnotatedBean.class);
      Interceptions.clear();
      
      Class<?> interfaces[] = { MyInterface.class };
      MyInterface proxy = container.constructProxy(interfaces);
      
      ArrayList<Class<?>> interceptions = Interceptions.getAroundInvokes();
      assertEquals(0, interceptions.size());
      
      proxy.method();
      
      assertEquals(6,  interceptions.size());
      assertEquals(ClassBaseInterceptor.class, interceptions.get(0));
      assertEquals(ClassInterceptor.class, interceptions.get(1));
      assertEquals(MethodBaseInterceptor.class, interceptions.get(2));
      assertEquals(MethodInterceptor.class, interceptions.get(3));
      assertEquals(AnnotatedBase.class, interceptions.get(4));
      assertEquals(AnnotatedBean.class, interceptions.get(5));
   }
   
   public void testXmlInheritanceAroundInvoke() throws Throwable
   {
      AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(MyInterface.class.getClassLoader());
      
      JBossEnterpriseBeanMetaData beanMetaData = getJBossEnterpriseBeanMetaData("XmlBean");
      
      MyContainer<XmlBean> container = new MyContainer<XmlBean>("XmlBean", "Test", XmlBean.class, beanMetaData);
      container.testAdvisor();
      
      BeanContext<XmlBean> bean = container.construct();
      
      Interceptions.clear();
      container.invoke(bean, "method");
      
      ArrayList<Class<?>> interceptions = Interceptions.getAroundInvokes();
      assertEquals("Wrong number of interceptions " + interceptions, 6,  interceptions.size());  
      assertEquals(XmlClassBaseInterceptor.class, interceptions.get(0));
      assertEquals(XmlClassInterceptor.class, interceptions.get(1));
      assertEquals(XmlMethodBaseInterceptor.class, interceptions.get(2));
      assertEquals(XmlMethodInterceptor.class, interceptions.get(3));
      assertEquals(XmlBase.class, interceptions.get(4));
      assertEquals(XmlBean.class, interceptions.get(5));            
   }

   public void testAnnotatedInheritanceLifecycle() throws Throwable
   {
      Thread.currentThread().setContextClassLoader(MyInterface.class.getClassLoader());
      
      JBossEnterpriseBeanMetaData beanMetaData = getJBossEnterpriseBeanMetaData("AnnotatedBean");
      
      MyContainer<AnnotatedBean> container = new MyContainer<AnnotatedBean>("AnnotatedBean", "Test", AnnotatedBean.class, beanMetaData);
      container.testAdvisor();
      
      Interceptions.clear();

      BeanContext<AnnotatedBean> bean = container.construct();
      ArrayList<Class<?>> postConstructs = Interceptions.getPostConstructs();
      assertEquals("Wrong number of interceptions " + postConstructs, 4, postConstructs.size());
      assertEquals(ClassBaseInterceptor.class, postConstructs.get(0));
      assertEquals(ClassInterceptor.class, postConstructs.get(1));
      assertEquals(AnnotatedBase.class, postConstructs.get(2));
      assertEquals(AnnotatedBean.class, postConstructs.get(3));
      
      container.invoke(bean, "method");
      
      container.destroy(bean);
      ArrayList<Class<?>> preDestroy = Interceptions.getPreDestroys();
      assertEquals("Wrong number of interceptions " + preDestroy, 4, preDestroy.size());
      assertEquals(ClassBaseInterceptor.class, preDestroy.get(0));
      assertEquals(ClassInterceptor.class, preDestroy.get(1));
      assertEquals(AnnotatedBase.class, preDestroy.get(2));
      assertEquals(AnnotatedBean.class, preDestroy.get(3));
   }
   
   public void testXmlInheritanceLifecycle() throws Throwable
   {
      AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(MyInterface.class.getClassLoader());
      
      JBossEnterpriseBeanMetaData beanMetaData = getJBossEnterpriseBeanMetaData("XmlBean");
      
      MyContainer<XmlBean> container = new MyContainer<XmlBean>("XmlBean", "Test", XmlBean.class, beanMetaData);
      container.testAdvisor();
      
      Interceptions.clear();
      
      BeanContext<XmlBean> bean = container.construct();
      ArrayList<Class<?>> postConstructs = Interceptions.getPostConstructs();
      assertEquals("Wrong number of interceptions " + postConstructs, 4, postConstructs.size());
      assertEquals(XmlClassBaseInterceptor.class, postConstructs.get(0));
      assertEquals(XmlClassInterceptor.class, postConstructs.get(1));
      assertEquals(XmlBase.class, postConstructs.get(2));
      assertEquals(XmlBean.class, postConstructs.get(3));
 
      container.invoke(bean, "method");
      
      container.destroy(bean);
      ArrayList<Class<?>> preDestroy = Interceptions.getPreDestroys();
      assertEquals("Wrong number of interceptions " + preDestroy, 4, preDestroy.size());
      assertEquals(XmlClassBaseInterceptor.class, preDestroy.get(0));
      assertEquals(XmlClassInterceptor.class, preDestroy.get(1));
      assertEquals(XmlBase.class, preDestroy.get(2));
      assertEquals(XmlBean.class, preDestroy.get(3));
   }
   
   private JBossEnterpriseBeanMetaData getJBossEnterpriseBeanMetaData(String name) throws JBossXBException
   {
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("inheritance/META-INF/ejb-jar.xml");
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean(name);
      assertNotNull(beanMetaData);

      return beanMetaData;
   }
}
