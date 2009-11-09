/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.proxy.impl.ejbthree1889.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.UUID;

import javax.naming.InitialContext;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.ServiceContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.service.MyService;
import org.jboss.ejb3.test.proxy.impl.common.ejb.service.MyServiceBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.service.MyServiceRemoteBusiness;
import org.jboss.ejb3.test.proxy.impl.ejbthree1889.RedefiningClassLoader;
import org.jboss.util.LRUCachePolicy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import sun.corba.Bridge;


/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RemoteServiceTestCase extends SessionTestCaseBase
{
   @AfterClass
   public static void afterClass()
   {
      if(bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
   
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      System.err.println(LRUCachePolicy.class.getProtectionDomain().getCodeSource());
      
      setUpBeforeClass();
      
      bootstrap.deploy(SessionTestCaseBase.class);
      
      URLClassLoader master = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      ClassLoader cl = new RedefiningClassLoader(master, MyServiceBean.class, MyService.class, MyServiceRemoteBusiness.class);
      Thread.currentThread().setContextClassLoader(cl);
      try
      {
         Class<?> beanClass = cl.loadClass(MyServiceBean.class.getName());
         
         ServiceContainer container = Utils.createService(beanClass);
         
         Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(master);
      }
   }
   
   @Test
   public void test1() throws Exception
   {
      InitialContext ctx = new InitialContext();
      System.err.println("   " + MyServiceRemoteBusiness.class.getClassLoader());
      //MyServiceRemoteBusiness bean = (MyServiceRemoteBusiness) ctx.lookup("MyServiceBean/remote");
      Object obj = ctx.lookup("MyServiceBean/remote");
      System.err.println("   latestUserDefinedLoader = " + Bridge.get().getLatestUserDefinedLoader());
      System.err.println("   " + obj.getClass().getInterfaces()[0].getClassLoader());
      MyServiceRemoteBusiness bean = (MyServiceRemoteBusiness) obj;
      UUID uuid = bean.getUuid();
      assertNotNull(uuid);
   }
   
   @Test
   public void testDifferentClassLoader() throws Exception
   {
      URLClassLoader master = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      ClassLoader cl = new RedefiningClassLoader(master, MyServiceRemoteBusiness.class, MyService.class, RemoteServiceTestCase.class);
      Thread.currentThread().setContextClassLoader(cl);
      try
      {
         System.err.println("X  latestUserDefinedLoader = " + Bridge.get().getLatestUserDefinedLoader());
         // setup a proper call stack class loader
         Class<?> testClass = cl.loadClass(RemoteServiceTestCase.class.getName());
         assertEquals(cl, testClass.getClassLoader());
         Method testMethod = testClass.getMethod("test1");
         Object obj = testClass.newInstance();
         testMethod.invoke(obj);
         
         Class<?> intf = cl.loadClass(MyServiceRemoteBusiness.class.getName());
         InitialContext ctx = new InitialContext();
         Method method = intf.getMethod("getUuid");
         System.err.println(method.getDeclaringClass().getClassLoader());
         Object bean = ctx.lookup("MyServiceBean/remote");
         System.err.println(bean.getClass().getInterfaces()[0].getClassLoader());
         //assertTrue(bean.getClass().isAssignableFrom(intf));
         assertTrue(intf.isAssignableFrom(bean.getClass()));
         UUID uuid = (UUID) method.invoke(bean);
         assertNotNull(uuid);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(master);
      }
   }
}
