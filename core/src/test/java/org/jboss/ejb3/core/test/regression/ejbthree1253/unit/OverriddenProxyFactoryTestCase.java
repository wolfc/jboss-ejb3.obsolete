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
package org.jboss.ejb3.core.test.regression.ejbthree1253.unit;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;

import javax.naming.InitialContext;

import org.jboss.aop.Domain;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactoryRegistry;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.common.MockEjb3Deployment;
import org.jboss.ejb3.core.test.regression.ejbthree1253.MyStateful;
import org.jboss.ejb3.core.test.regression.ejbthree1253.MyStatefulBean;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.ejb3.test.common.MetaDataHelper;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class OverriddenProxyFactoryTestCase extends AbstractEJB3TestCase
{
   @Test
   public void test1() throws Throwable
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      System.out.println(cl.getResource("jndi.properties"));
      String beanClassname = MyStatefulBean.class.getName();
      String ejbName = MyStatefulBean.class.getSimpleName();
      Domain domain = getDomain("Stateful Bean");
      Hashtable<?,?> ctxProperties = null;
      Ejb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit());
      deployment.setPersistenceManagerFactoryRegistry(getBootstrap().lookup("EJB3PersistenceManagerFactoryRegistry", PersistenceManagerFactoryRegistry.class));
      JBossSessionBeanMetaData beanMetaData = MetaDataHelper.getMetadataFromBeanImplClass(MyStatefulBean.class);
      StatefulContainer container = new StatefulContainer(cl, beanClassname, ejbName, domain, ctxProperties, deployment, beanMetaData);
      
      // TODO: wickedness
      container.instantiated();
      
      // A container does not register itself
      Ejb3Registry.register(container);
      
      // Register the Container in ObjectStore (MC)
      String containerName = container.getObjectName().getCanonicalName();
//      AbstractBeanMetaData bmd = new AbstractBeanMetaData(containerName, StatefulContainer.class.getName());
//      KernelControllerContext context = getBootstrap().getKernel().getController().install(bmd, container);
//      if(context.getError() != null)
//         throw context.getError();
      Ejb3RegistrarLocator.locateRegistrar().bind(containerName, container);

      
      InitialContext ctx = new InitialContext();
      System.out.println("ctx = " + ctx);
      //System.out.println("  " + container.getInitialContext().list("MyStatelessBean").next());
      MyStateful bean = (MyStateful) ctx.lookup("MyStatefulBean/remote");
      
      bean.setName("Me");
      String actual = bean.sayHi();
      assertEquals("Hi Me", actual);
      
      getBootstrap().getKernel().getController().uninstall(containerName);
      Ejb3Registry.unregister(container);
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      deploy("org/jboss/ejb3/core/test/regression/ejbthree1253/remoteproxyfactoryregistry-beans.xml");
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      AbstractEJB3TestCase.afterClass();
   }
}
