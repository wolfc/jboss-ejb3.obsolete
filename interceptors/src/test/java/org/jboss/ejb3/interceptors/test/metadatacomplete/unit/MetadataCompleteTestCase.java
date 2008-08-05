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
package org.jboss.ejb3.interceptors.test.metadatacomplete.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.test.common.ManagedObjectContainer;
import org.jboss.ejb3.interceptors.test.metadatacomplete.TwoLifecycleMethodsBean;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.EjbJar30MetaData;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.builder.JBossXBBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.ls.LSInput;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MetadataCompleteTestCase
{
   private static final Logger log = Logger.getLogger(MetadataCompleteTestCase.class);
   private static final AOPDeployer deployer = new AOPDeployer("proxy/jboss-aop.xml");
   
   private JBossEnterpriseBeanMetaData beanMetaData;
   
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

   @Before
   public void setUp() throws Exception
   {
      TwoLifecycleMethodsBean.annotatedPostConstructRan = false;
      TwoLifecycleMethodsBean.otherPostConstructRan = false;
      
      // Bootstrap metadata
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      URL url = Thread.currentThread().getContextClassLoader().getResource("metadatacomplete/META-INF/ejb-jar.xml");
      assertNotNull("Can't find descriptor metadatacomplete/META-INF/ejb-jar.xml", url);
      EjbJar30MetaData metaData = (EjbJar30MetaData) unmarshaller.unmarshal(url.toString(), schemaResolverForClass(EjbJar30MetaData.class));
      JBoss50MetaData jbossMetaData = new JBoss50MetaData();
      jbossMetaData.merge(null, metaData);
      
      beanMetaData = jbossMetaData.getEnterpriseBean("TwoLifecycleMethodsBean");
      assertNotNull(beanMetaData);   
   }
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      log.info(deployer.deploy());
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      log.info(deployer.undeploy());
   }

   @Test
   public void testAnnotations() throws Throwable
   {
      // ignore the descriptor
      JBoss50MetaData metaData = new JBoss50MetaData();
      
      // To avoid NPE
      metaData.setAssemblyDescriptor(new JBossAssemblyDescriptorMetaData());
      
      JBossEnterpriseBeansMetaData enterpriseBeans = new JBossEnterpriseBeansMetaData();
      metaData.setEnterpriseBeans(enterpriseBeans);
      
      JBossEnterpriseBeanMetaData beanMetaData = new JBossSessionBeanMetaData();
      beanMetaData.setEjbName("TwoLifecycleMethodsBean");
      enterpriseBeans.add(beanMetaData);
      
      ManagedObjectContainer<TwoLifecycleMethodsBean> container = new ManagedObjectContainer<TwoLifecycleMethodsBean>(beanMetaData.getEjbName(), "Test", TwoLifecycleMethodsBean.class, beanMetaData);
      
      BeanContext<TwoLifecycleMethodsBean> bean = container.construct();
      
      container.destroy(bean);
      
      assertTrue(TwoLifecycleMethodsBean.annotatedPostConstructRan);
      assertFalse(TwoLifecycleMethodsBean.otherPostConstructRan);
   }
   
   @Test
   public void testMetadataComplete() throws Throwable
   {
      ManagedObjectContainer<TwoLifecycleMethodsBean> container = new ManagedObjectContainer<TwoLifecycleMethodsBean>(beanMetaData.getEjbName(), "Test", TwoLifecycleMethodsBean.class, beanMetaData );
      
      BeanContext<TwoLifecycleMethodsBean> bean = container.construct();
      
      container.destroy(bean);
      
      assertTrue(TwoLifecycleMethodsBean.otherPostConstructRan);
      assertFalse(TwoLifecycleMethodsBean.annotatedPostConstructRan);
   }
}
