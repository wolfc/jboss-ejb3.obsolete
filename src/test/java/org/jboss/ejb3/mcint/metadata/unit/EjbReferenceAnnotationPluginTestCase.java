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
package org.jboss.ejb3.mcint.metadata.unit;

import junit.framework.TestCase;

import org.jboss.beans.info.spi.BeanAccessMode;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.mcint.annotationadaptor.AddAnnotationPluginOnBeanAnnotationAdaptorCallbackService;
import org.jboss.ejb3.mcint.metadata.MockEjbReferenceResolver;
import org.jboss.ejb3.mcint.metadata.Pojo;
import org.jboss.ejb3.mcint.metadata.plugins.EjbReferenceAnnotationPlugin;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * EjbReferenceKernelRegistryPluginTestCase
 * 
 * Test cases to validate @EJB injection into MC Beans
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EjbReferenceAnnotationPluginTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   private static final Logger log = Logger.getLogger(EjbReferenceAnnotationPluginTestCase.class);

   private static final String EJB3_MC_NAMESPACE = "ejb3.";

   private static final String MC_BEAN_NAME = EJB3_MC_NAMESPACE + "TestPojo";

   private static EmbeddedTestMcBootstrap bootstrap;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   /**
    * Tests that an instance annotated w/ @EJB is injected
    * with values as expected from the resolver
    */
   @Test
   public void testInjectionSuccess() throws Throwable
   {
      // Get the bean
      Pojo bean = Ejb3RegistrarLocator.locateRegistrar().lookup(MC_BEAN_NAME, Pojo.class);

      // Get the injected instance
      Object injected = bean.getInjectedMember();
      log.info("Got injected instance: " + injected);

      // Ensure we've been injected
      TestCase.assertNotNull("Injected instance was null", injected);
   }

   // --------------------------------------------------------------------------------||
   // Lifefycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   @Before
   public void beforeTest() throws Throwable
   {
      // Install a POJO instance
      Pojo bean = new Pojo();
      BeanMetaDataBuilder bmdb = BeanMetaDataBuilder.createBuilder(MC_BEAN_NAME, bean.getClass().getName());
      bmdb.setAccessMode(BeanAccessMode.ALL);
      bootstrap.getKernel().getController().install(bmdb.getBeanMetaData(), bean);
   }

   @After
   public void afterTest() throws Throwable
   {
      // Uninstall 
      Ejb3RegistrarLocator.locateRegistrar().unbind(MC_BEAN_NAME);
   }

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and run a new Bootstrap
      bootstrap = new EmbeddedTestMcBootstrap();
      bootstrap.run();

      // Dig into MC
      Kernel kernel = bootstrap.getKernel();

      // Create and Bind the EJB3 Registrar
      Ejb3Registrar registrar = new Ejb3McRegistrar(bootstrap.getKernel());
      Ejb3RegistrarLocator.bindRegistrar(registrar);

      // Install the BeanAnnotationAdapter w/ callback to add annotation plugins on install
      AddAnnotationPluginOnBeanAnnotationAdaptorCallbackService addCallbacksService = new AddAnnotationPluginOnBeanAnnotationAdaptorCallbackService(
            kernel);
      String addCallbacksServiceBindName = EJB3_MC_NAMESPACE + "AddCallBacksService";
      registrar.bind(addCallbacksServiceBindName, addCallbacksService);

      // Install our annotation plugin, will be picked up in BAA callback
      String annotationPluginBindName = EJB3_MC_NAMESPACE + EjbReferenceAnnotationPlugin.class.getName();
      registrar.bind(annotationPluginBindName, new EjbReferenceAnnotationPlugin(new MockEjbReferenceResolver()));
   }

   @AfterClass
   public static void afterClass() throws Throwable
   {
      Ejb3RegistrarLocator.unbindRegistrar();
   }
}
