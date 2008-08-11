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
package org.jboss.ejb3.core.test.ejbthree1060.unit;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1060.BusinessInterface1;
import org.jboss.ejb3.core.test.ejbthree1060.BusinessInterface2;
import org.jboss.ejb3.core.test.ejbthree1060.InvokedBusinessInterfaceBean;
import org.jboss.ejb3.session.SessionContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * InvokedBusinessInterfaceUnitTestCase
 * 
 * Test Cases to validate SessionContext.getInvokedBusinessInterface
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class InvokedBusinessInterfaceUnitTestCase extends AbstractEJB3TestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static SessionContainer container;

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Test
   public void testGetInvokedBusinessInterface() throws Throwable
   {
      // Define JNDI Targets for Lookup
      String jndiNameBase = InvokedBusinessInterfaceBean.class.getSimpleName() + "/" + "local-";
      String jndiNameInterface1 = jndiNameBase + BusinessInterface1.class.getName();
      String jndiNameInterface2 = jndiNameBase + BusinessInterface2.class.getName();

      // Get JNDI Context
      Context context = new InitialContext();

      // Obtain
      BusinessInterface1 busiface1 = (BusinessInterface1) context.lookup(jndiNameInterface1);
      BusinessInterface2 busiface2 = (BusinessInterface2) context.lookup(jndiNameInterface2);

      // Invoke getInvokedBusinessInterface
      Class<?> invoked1 = busiface1.getInvokedBusinessInterface();
      Class<?> invoked2 = busiface2.getInvokedBusinessInterface();

      // Test
      TestCase.assertEquals(BusinessInterface1.class, invoked1);
      TestCase.assertEquals(BusinessInterface2.class, invoked2);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();

      // Deploy the test SLSB
      container = deploySessionEjb(InvokedBusinessInterfaceBean.class);
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      // Undeploy the test SLSB
      undeployEjb(container);

      AbstractEJB3TestCase.afterClass();
   }

}
