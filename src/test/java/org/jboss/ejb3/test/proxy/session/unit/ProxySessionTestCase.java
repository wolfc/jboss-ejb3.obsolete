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
package org.jboss.ejb3.test.proxy.session.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashSet;

import javax.naming.InitialContext;

import org.jboss.ejb3.test.proxy.common.EmbeddedTestMcBootstrap;
import org.jboss.ejb3.test.proxy.common.StatelessContainer;
import org.jboss.ejb3.test.proxy.session.MyStatelessBean;
import org.jboss.ejb3.test.proxy.session.MyStatelessLocal;
import org.jboss.ejb3.test.proxy.session.MyStatelessLocalHome;
import org.jboss.ejb3.test.proxy.session.MyStatelessRemote;
import org.jboss.ejb3.test.proxy.session.MyStatelessRemoteHome;
import org.jboss.metadata.annotation.creator.ejb.EjbJar30Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.EjbJar30MetaData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ProxySessionTestCase
{
   private static EmbeddedTestMcBootstrap bootstrap;
   
   /**
    * @throws java.lang.Exception
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Throwable
   {
      bootstrap = new EmbeddedTestMcBootstrap();
      bootstrap.run();
      bootstrap.deploy(ProxySessionTestCase.class);
      
      // emulate annotation deployer
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      Collection<Class<?>> classes = new HashSet<Class<?>>();
      classes.add(MyStatelessBean.class);
      EjbJar30MetaData metaData = new EjbJar30Creator(finder).create(classes);
      
      // emulate merge deployer
      JBossMetaData mergedMetaData = new JBossMetaData();
      mergedMetaData.merge(null, metaData);
      
      JBossSessionBeanMetaData beanMetaData = (JBossSessionBeanMetaData) mergedMetaData.getEnterpriseBean("MyStatelessBean");
      
      System.out.println(beanMetaData.determineJndiName());         // MyStatelessBean/remote
      System.out.println(beanMetaData.determineLocalJndiName());    // MyStatelessBean/local
      
      StatelessContainer container = new StatelessContainer(beanMetaData);
      
      bootstrap.installInstance("jboss.j2ee:service=EJB3,name=" + beanMetaData.getEjbName(), container);
   }

   /**
    * @throws java.lang.Exception
    */
   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if(bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }

   @Test
   public void testLocal() throws Exception
   {
      InitialContext ctx = new InitialContext();
      
      Object bean = ctx.lookup("MyStatelessBean/local");
      assertTrue(bean instanceof MyStatelessLocal);
      
      String result = ((MyStatelessLocal) bean).sayHi("testLocal");
      assertEquals("Hi testLocal", result);
   }

   @Test
   public void testLocalHome() throws Exception
   {
      InitialContext ctx = new InitialContext();
      
      Object bean = ctx.lookup("MyStatelessBean/localHome");
      assertTrue(bean instanceof MyStatelessLocalHome);
   }

   @Test
   public void testRemote() throws Exception
   {
      InitialContext ctx = new InitialContext();
      
      Object bean = ctx.lookup("MyStatelessBean/remote");
      assertTrue(bean instanceof MyStatelessRemote);
   }

   @Test
   public void testRemoteHome() throws Exception
   {
      InitialContext ctx = new InitialContext();
      
      Object bean = ctx.lookup("MyStatelessBean/home");
      assertTrue(bean instanceof MyStatelessRemoteHome);
   }
}
