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
package org.jboss.ejb3.test.interceptors.additive.unit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.AbstractDirectContainer;
import org.jboss.ejb3.interceptors.metadata.AdditiveBeanInterceptorMetaDataBridge;
import org.jboss.ejb3.interceptors.metadata.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.interceptors.metadata.InterceptorMetaDataBridge;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.test.interceptors.additive.MyInterceptor;
import org.jboss.ejb3.test.interceptors.additive.MySessionBean;
import org.jboss.ejb3.test.interceptors.additive.XMLInterceptor;
import org.jboss.ejb3.test.interceptors.common.AOPDeployer;
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
 * The binding of interceptors to classes is additive. If interceptors are bound at the class-level and/or
 * default-level as well as at the method-level, both class-level and/or default-level as well as method-level
 * interceptors will apply. The deployment descriptor may be used to augment the interceptors and inter-
 * ceptor methods defined by means of annotations. When the deployment descriptor is used to augment
 * the interceptors specified in annotations, the interceptor methods specified in the deployment descriptor
 * will be invoked after those specified in annotations, according to the ordering specified in sections
 * 12.3.1 and 12.4.1. The interceptor-order deployment descriptor element may be used to over-
 * ride this ordering.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class AdditiveTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(AdditiveTestCase.class);
   
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
         annotations.addMetaDataBridge(new AdditiveBeanInterceptorMetaDataBridge(beanClass));
         
         initializeAdvisor(name, getDomain(domainName), beanClass, annotations);
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

   public void test() throws Throwable
   {
      log.info("======= Additive.test()");
//      AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(MySessionBean.class.getClassLoader());
      
      // Bootstrap AOP
      // FIXME: use the right jboss-aop.xml
      AOPDeployer deployer = new AOPDeployer("proxy/jboss-aop.xml");
      try
      {
         log.info(deployer.deploy());

         // Bootstrap metadata
         UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
         Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
         URL url = Thread.currentThread().getContextClassLoader().getResource("additive/META-INF/ejb-jar.xml");
         assertNotNull("no ejb-jar.xml", url);
         EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
         JBoss50MetaData jbossMetaData = new JBoss50MetaData();
         jbossMetaData.merge(null, metaData);
         
         JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("MySessionBean");
         assertNotNull(beanMetaData);
         
         MyContainer<MySessionBean> container = new MyContainer<MySessionBean>("MySessionBean", "Test", MySessionBean.class, beanMetaData);
         
         BeanContext<MySessionBean> bean = container.construct();
         
         List<Class<?>> visits = new ArrayList<Class<?>>();
         container.invoke(bean, "doIt", visits);
         
         List<Class<?>> expected = new ArrayList<Class<?>>();
         expected.add(MyInterceptor.class);
         expected.add(XMLInterceptor.class);
         expected.add(MySessionBean.class);
         
         assertEquals(expected, visits);
      }
      finally
      {
         log.info(deployer.undeploy());
      }
      log.info("======= Done");
   }
}
