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
package org.jboss.ejb3.test.interceptors.lifecycle.unit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.AbstractDirectContainer;
import org.jboss.ejb3.interceptors.metadata.BeanInterceptorMetaDataBridge;
import org.jboss.ejb3.interceptors.metadata.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.interceptors.metadata.InterceptorMetaDataBridge;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.test.interceptors.common.AOPDeployer;
import org.jboss.ejb3.test.interceptors.lifecycle.SessionBeanCallbackBean;
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
 * Test bean lifecycle callbacks defined in the descriptor. 
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class CallbackMethodDescriptorTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(CallbackMethodDescriptorTestCase.class);
   
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
         annotations.addMetaDataBridge(new BeanInterceptorMetaDataBridge());
         
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
      log.info("======= CallbackMethodDescriptor.test()");
//      AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(MetadataBean.class.getClassLoader());
      
      AOPDeployer deployer = new AOPDeployer("proxy/jboss-aop.xml");
      try
      {
         // Bootstrap AOP
         // FIXME: use the right jboss-aop.xml
         log.info(deployer.deploy());
         
         // Bootstrap metadata
         UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
         Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
         URL url = Thread.currentThread().getContextClassLoader().getResource("lifecycle/META-INF/ejb-jar.xml");
         EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
         JBoss50MetaData jbossMetaData = new JBoss50MetaData();
         jbossMetaData.merge(null, metaData);
         
         JBossEnterpriseBeanMetaData beanMetaData = jbossMetaData.getEnterpriseBean("SessionBeanCallbackBean");
         assertNotNull(beanMetaData);
         
         assertEquals(0, SessionBeanCallbackBean.postConstructs);
         
         MyContainer<SessionBeanCallbackBean> container = new MyContainer<SessionBeanCallbackBean>("SessionBeanCallbackBean", "Test", SessionBeanCallbackBean.class, beanMetaData);
         
         BeanContext<SessionBeanCallbackBean> bean = container.construct();
         
         assertEquals("SessionBeanCallbackBean ejbCreate must have been called once", 1, SessionBeanCallbackBean.postConstructs);
         
         container.destroy(bean);
         assertEquals(1, SessionBeanCallbackBean.preDestroys);
         
         bean = null;
      }
      finally
      {
         log.info(deployer.undeploy());
      }
      log.info("======= Done");
   }
}
