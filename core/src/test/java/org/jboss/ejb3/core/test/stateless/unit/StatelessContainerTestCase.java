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

import javax.naming.InitialContext;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.stateless.MyStateless;
import org.jboss.ejb3.core.test.stateless.MyStatelessBean;
import org.jboss.ejb3.session.SessionContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test is just to get some coverage in StatelessContainer.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatelessContainerTestCase extends AbstractEJB3TestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static SessionContainer container;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Simple test for SLSB invocation
    */
   @Test
   public void test1() throws Throwable
   {

      InitialContext ctx = new InitialContext();
      System.out.println("ctx = " + ctx);
      //System.out.println("  " + container.getInitialContext().list("MyStatelessBean").next());
      MyStateless bean = (MyStateless) ctx.lookup("MyStatelessBean/local");
      String actual = bean.sayHi("Me");
      assertEquals("Hi Me", actual);

   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();

      // Deploy the test SLSB
      container = deploySessionEjb(MyStatelessBean.class);
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      // Undeploy the test SLSB
      undeployEjb(container);

      AbstractEJB3TestCase.afterClass();
   }

}
