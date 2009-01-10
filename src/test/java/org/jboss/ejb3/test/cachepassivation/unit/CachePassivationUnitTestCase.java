/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.cachepassivation.unit;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.aop.Domain;
import org.jboss.cache.transaction.DummyTransactionManager;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactory;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactoryRegistry;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.core.test.common.MockEjb3Deployment;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.ejb3.test.cachepassivation.MockBean;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.ejb3.test.cachepassivation.MockStatefulContainer;
import org.jboss.ejb3.test.cachepassivation.MyStatefulSessionFilePersistenceManagerFactory;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.naming.JavaCompInitializer;
import org.jnp.server.SingletonNamingServer;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class CachePassivationUnitTestCase extends TestCase
{
   public void test1() throws Exception
   {
      new SingletonNamingServer();
      
      Hashtable ctxProperties = new Hashtable();
      ctxProperties.put("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      ctxProperties.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      
      JavaCompInitializer initializer = new JavaCompInitializer();
      initializer.setInitialContextProperties(ctxProperties);
      initializer.start();
      
      DummyTransactionManager tm = new DummyTransactionManager();
      InitialContext ic = new InitialContext(ctxProperties);
      ic.bind("java:/TransactionManager", tm);
      
      EmbeddedTestMcBootstrap bootstrap = EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap();
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));
      
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class<?> beanClass = MockBean.class;
      String beanClassName = beanClass.getName();
      String ejbName = beanClass.getSimpleName();
      Domain domain = new Domain(new AspectManager(), "Test", false);
      Map<String, Class<? extends PersistenceManagerFactory>> factories = new HashMap<String, Class<? extends PersistenceManagerFactory>>();
      factories.put("MyStatefulSessionFilePersistenceManager", MyStatefulSessionFilePersistenceManagerFactory.class);
      PersistenceManagerFactoryRegistry persistenceManagerFactoryRegistry = new PersistenceManagerFactoryRegistry();
      persistenceManagerFactoryRegistry.setFactories(factories);
      Ejb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit());
      deployment.setPersistenceManagerFactoryRegistry(persistenceManagerFactoryRegistry);
      MockStatefulContainer container = new MockStatefulContainer(cl, beanClassName, ejbName, domain, ctxProperties,
            deployment);
      container.instantiated();
      container.processMetadata();
      System.out.println("injectors = " + container.getInjectors());
      Ejb3Registry.register(container);
      try
      {
         container.create();
         container.setJaccContextId("none");
         container.start();
         
         Object id = container.createSession();
         
         StatefulBeanContext ctx = container.getCache().get(id, false);
         
         System.out.println("inUse = " + ctx.isInUse());
         MockBean bean = (MockBean) ctx.getInstance();
         System.out.println(bean.ctx);
         ctx.setInUse(false);
         ctx = null;
         
         synchronized (MockBean.notification)
         {
            MockBean.notification.wait(5000);
         }
         Thread.sleep(500);
         
         ctx = container.getCache().get(id, false);
         bean = (MockBean) ctx.getInstance();
         
         String a = ctx.getEJBContext().toString();
         String b = bean.ctx.toString();
         System.out.println(ctx.getEJBContext());
         System.out.println(bean.ctx);
         assertTrue(a.regionMatches(a.indexOf('{'), b, b.indexOf('{'), a.length() - a.indexOf('{')));
      }
      finally
      {
         bootstrap.shutdown();
//         container.stop();
//         container.destroy();
//         Ejb3Registry.unregister(container);
      }
   }
}
