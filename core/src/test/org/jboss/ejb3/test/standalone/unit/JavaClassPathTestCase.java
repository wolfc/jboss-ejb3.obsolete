/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.standalone.unit;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.test.standalone.CalculatorBean;
import org.jboss.ejb3.test.standalone.CalculatorRemote;
import org.jboss.ejb3.test.standalone.Customer;
import org.jboss.ejb3.test.standalone.CustomerDAO;
import org.jboss.ejb3.test.standalone.ShoppingCart;
import org.jboss.ejb3.test.standalone.StupidInterceptor;

import javax.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * POJO Environment tests
 *
 * @author <a href="bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class JavaClassPathTestCase extends TestCase
{
   public JavaClassPathTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(JavaClassPathTestCase.class);

      // setup test so that embedded JBoss is started/stopped once for all tests here.
      TestSetup wrapper = new TestSetup(suite)
      {
         protected void setUp()
         {
            startupEmbeddedJboss();
         }

         protected void tearDown()
         {
            shutdownEmbeddedJboss();
         }
      };

      return wrapper;
   }

   public static void startupEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.boot(null);
      EJB3StandaloneBootstrap.scanClasspath("standalone.jar");
   }

   public static void shutdownEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.shutdown();
   }

   protected InitialContext getInitialContext() throws Exception
   {
      return new InitialContext(getInitialContextProperties());
   }

   protected Hashtable getInitialContextProperties()
   {
      return EJB3StandaloneBootstrap.getInitialContextProperties();
   }


   public void testJavaClassPath() throws Throwable
   {
      InitialContext ctx = getInitialContext();

      executeEJBs(ctx);
   }

   private void executeEJBs(InitialContext ctx)
           throws NamingException
   {
      CustomerDAO local = (CustomerDAO) ctx.lookup("CustomerDAOBean/local");
      long id = local.createCustomer();
      Customer cust = local.findCustomer(id);
      assertEquals("Bill", cust.getName());

      ShoppingCart cart = (ShoppingCart) ctx.lookup("ShoppingCartBean/local");
      cart.getCart().add("beer");
      cart.getCart().add("wine");
      assertEquals(2, cart.getCart().size());

      cart.checkout();

      boolean exceptionThrown = false;
      try
      {
         cart.getCart();
      }
      catch (NoSuchEJBException e)
      {
         exceptionThrown = true;
      }

      assertTrue(exceptionThrown);

      CalculatorRemote calc = (CalculatorRemote) ctx.lookup("CalculatorBean/remote");
      StupidInterceptor.count = 0;
      CalculatorBean.test = true;
      try
      {
         assertEquals(2, calc.add(1, 1));
         assertEquals(1, StupidInterceptor.count);
         StupidInterceptor.count = 0;
         CalculatorBean.test = true;
         assertEquals(0, calc.sub(1, 1));
         assertEquals(1, StupidInterceptor.count);
      }
      finally
      {
         CalculatorBean.test = false;
      }
   }

   protected void configureLoggingAfterBootstrap()
   {
      //enableTrace("org.jboss.tm");
   }
}