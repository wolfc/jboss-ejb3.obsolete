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

import java.net.URL;
import java.util.Hashtable;

import javax.naming.InitialContext;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.core.test.common.MetaDataHelper;
import org.jboss.ejb3.core.test.stateless.MyStateless;
import org.jboss.ejb3.core.test.stateless.MyStatelessBean;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.ejb3.test.cachepassivation.MockEjb3Deployment;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test is just to get some coverage in StatelessContainer.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class StatelessContainerTestCase
{
   private static EmbeddedTestMcBootstrap bootstrap;
   
   @AfterClass
   public static void afterClass() throws Exception
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource("ejb3-interceptors-aop.xml");
      AspectXmlLoader.undeployXML(url);
      
      if(bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      // FIXME: weirdness in InitialContextFactory (see EJBTHREE-1097)
      InitialContextFactory.close(null, null);
      
      bootstrap = EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap();
      deploy("basicbootstrap-beans.xml");
      
      // TODO: AspectDeployment
      URL url = Thread.currentThread().getContextClassLoader().getResource("ejb3-interceptors-aop.xml");
      AspectXmlLoader.deployXML(url);
   }
   
   private static void deploy(String resourceName)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
      if(url == null)
         throw new IllegalArgumentException("Can't find a resource named '" + resourceName + "'");
      assert bootstrap != null : "Can't deploy a resource, bootstrap is null";
      bootstrap.deploy(url);
   }
   
   private static Domain getDomain(String domainName)
   {
      DomainDefinition domainDef = AspectManager.instance().getContainer(domainName);
      if(domainDef == null) throw new IllegalArgumentException("No such domain '" + domainName + "'");
      return (Domain) domainDef.getManager();
   }
   
   @Test
   public void test1() throws Throwable
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      System.out.println(cl.getResource("jndi.properties"));
      String beanClassname = MyStatelessBean.class.getName();
      String ejbName = "MyStatelessBean";
      Domain domain = getDomain("Stateless Bean");
      Hashtable ctxProperties = null;
      Ejb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit(), null);
      JBossSessionBeanMetaData beanMetaData = MetaDataHelper.createMockBeanMetaData();
      StatelessContainer container = new StatelessContainer(cl, beanClassname, ejbName, domain, ctxProperties, deployment, beanMetaData);
      
      // TODO: wickedness
      container.instantiated();
      
      String serviceName = "jboss.ejb3:name=MyStatelessBean,service=EJB3";
      AbstractBeanMetaData bmd = new AbstractBeanMetaData(serviceName, StatelessContainer.class.getName());
      bootstrap.getKernel().getController().install(bmd, container);
      
      InitialContext ctx = new InitialContext();
      System.out.println("ctx = " + ctx);
      //System.out.println("  " + container.getInitialContext().list("MyStatelessBean").next());
      MyStateless bean = (MyStateless) ctx.lookup("MyStatelessBean/local");
      
      String actual = bean.sayHi("Me");
      assertEquals("Hi Me", actual);
      
      bootstrap.getKernel().getController().uninstall(serviceName);
   }
}
