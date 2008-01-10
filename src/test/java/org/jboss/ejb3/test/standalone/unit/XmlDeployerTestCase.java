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

import java.util.Hashtable;
import javax.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.test.standalone.CalculatorRemote;
import org.jboss.ejb3.test.standalone.Customer;
import org.jboss.ejb3.test.standalone.CustomerDAO;
import org.jboss.ejb3.test.standalone.ShoppingCart;
import junit.framework.TestCase;

/**
 * POJO Environment tests
 * 
 * @author <a href="bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class XmlDeployerTestCase extends TestCase
{
   private static boolean booted = false;

   public XmlDeployerTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      // set bad properties to make sure that we're injecting InitialContext correct
//      System.setProperty("java.naming.factory.initial", "ERROR");
//      System.setProperty("java.naming.factory.url.pkgs", "ERROR");

      super.setUp();
      long start = System.currentTimeMillis();
      try
      {
         if (!booted)
         {
            booted = true;
            EJB3StandaloneBootstrap.boot("");
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
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


   public void testXmlDeployer() throws Throwable
   {
      InitialContext ctx = getInitialContext();
      EJB3StandaloneBootstrap.deployXmlResource("deployer.xml");

      executeEJBs(ctx);

      EJB3StandaloneBootstrap.getKernel().getController().uninstall("EJBDeployment");
   }

   public void testCleanup() throws Throwable
   {
      boolean exceptionThrown = false;
      try
      {
         executeEJBs(getInitialContext());
      }
      catch (Exception e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);

   }

   private void executeEJBs(InitialContext ctx)
           throws NamingException
   {
      CustomerDAO local = (CustomerDAO)ctx.lookup("CustomerDAOBean/local");
      long id = local.createCustomer();
      Customer cust = local.findCustomer(id);
      assertEquals("Bill", cust.getName());

      ShoppingCart cart = (ShoppingCart)ctx.lookup("ShoppingCartBean/local");
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

      CalculatorRemote calc = (CalculatorRemote)ctx.lookup("CalculatorBean/remote");
      assertEquals(2, calc.add(1, 1));
   }

   protected void configureLoggingAfterBootstrap()
   {
      //enableTrace("org.jboss.tm");
   }
}