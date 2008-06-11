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
package org.jboss.ejb3.core.test.stateless.unit;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;

import javax.naming.InitialContext;

import org.jboss.aop.Domain;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.stateless.MyStateless;
import org.jboss.ejb3.core.test.stateless.MyStatelessBean;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.ejb3.test.cachepassivation.MockEjb3Deployment;
import org.jboss.ejb3.test.common.MetaDataHelper;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.junit.Test;

/**
 * This test is just to get some coverage in StatelessContainer.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class StatelessContainerTestCase extends AbstractEJB3TestCase
{
   @Test
   public void test1() throws Throwable
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      System.out.println(cl.getResource("jndi.properties"));
      String beanClassname = MyStatelessBean.class.getName();
      String ejbName = MyStatelessBean.class.getSimpleName();
      Domain domain = getDomain("Stateless Bean");
      Hashtable<?,?> ctxProperties = null;
      Ejb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit(), null);
      JBossSessionBeanMetaData beanMetaData = MetaDataHelper.getMetadataFromBeanImplClass(MyStatelessBean.class);
      StatelessContainer container = new StatelessContainer(cl, beanClassname, ejbName, domain, ctxProperties, deployment, beanMetaData);
      
      // TODO: wickedness
      container.instantiated();
      
      // Register the Container in ObjectStore (MC)
//      String serviceName = "jboss.ejb3:name=MyStatelessBean,service=EJB3";
//      AbstractBeanMetaData bmd = new AbstractBeanMetaData(serviceName, StatelessContainer.class.getName());
//    bootstrap.getKernel().getController().install(bmd, container);
      String containerName = container.getName();
      Ejb3RegistrarLocator.locateRegistrar().bind(containerName, container);

      
      InitialContext ctx = new InitialContext();
      System.out.println("ctx = " + ctx);
      //System.out.println("  " + container.getInitialContext().list("MyStatelessBean").next());
      MyStateless bean = (MyStateless) ctx.lookup("MyStatelessBean/local");
      
      String actual = bean.sayHi("Me");
      assertEquals("Hi Me", actual);
      
      getBootstrap().getKernel().getController().uninstall(containerName);
   }
}
