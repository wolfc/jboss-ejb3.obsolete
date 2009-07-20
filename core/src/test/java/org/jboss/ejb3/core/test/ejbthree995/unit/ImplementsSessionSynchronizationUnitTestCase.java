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
package org.jboss.ejb3.core.test.ejbthree995.unit;

import javax.ejb.SessionSynchronization;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree995.ImplementsSessionSynchronizationBean;
import org.jboss.ejb3.core.test.ejbthree995.ImplementsSessionSynchronizationRemoteBusiness;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ImplementsSessionSynchronizationUnitTestCase
 * 
 * Test Cases to validate EJBTHREE-995
 * is resolved
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ImplementsSessionSynchronizationUnitTestCase extends AbstractEJB3TestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ImplementsSessionSynchronizationUnitTestCase.class);

   private static SessionContainer container;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that an invocation to a SFSB implementing SessionSynchronization
    * does not result in 2 calls to any of the SessionSynchronization callbacks
    */
   @Test
   public void testSfsbImplementingSessionSynchronizationForDoubleCallbacks() throws Throwable
   {
      // Define JNDI Target for Lookup
      String jndiName = ImplementsSessionSynchronizationBean.class.getSimpleName() + "/" + "remote";

      // Get JNDI Context
      Context context = new InitialContext();

      // Obtain
      ImplementsSessionSynchronizationRemoteBusiness sfsb = (ImplementsSessionSynchronizationRemoteBusiness) context
            .lookup(jndiName);

      // Check callbacks are reset
      this.assertCallbacksReset();

      // Invoke
      sfsb.call();

      // Check callbacks have been made exactly once
      this.assertCallsExpected(1);
      
      // Invoke again
      sfsb.call();

      // Check callbacks have been made exactly twice
      this.assertCallsExpected(2);      
      
      // Invoke again
      sfsb.call();

      // Check callbacks have been made exactly three times
      this.assertCallsExpected(3);  

      // Reset
      ImplementsSessionSynchronizationBean.resetCounters();

      // Check callbacks are reset
      this.assertCallbacksReset();
   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that all SessionSynchronization callbacks are reset to 0
    */
   protected void assertCallbacksReset()
   {
      int expectedCalls = 0;
      this.assertCallsExpected(expectedCalls);
   }

   /**
    * Ensures that all SessionSynchronization callbacks have been
    * made the specified number of times
    * 
    * @param numCalls
    */
   protected void assertCallsExpected(int numCalls)
   {
      TestCase.assertEquals(numCalls, ImplementsSessionSynchronizationBean.CALLS_AFTER_BEGIN);
      TestCase.assertEquals(numCalls, ImplementsSessionSynchronizationBean.CALLS_AFTER_COMPLETION);
      TestCase.assertEquals(numCalls, ImplementsSessionSynchronizationBean.CALLS_BEFORE_COMPLETION);
      log.info("All " + SessionSynchronization.class.getSimpleName() + " callbacks were obtained expected " + numCalls
            + " times.");
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();

      // Deploy the test SLSB
      container = deploySessionEjb(ImplementsSessionSynchronizationBean.class);
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      // Undeploy the test SLSB
      undeployEjb(container);

      AbstractEJB3TestCase.afterClass();
   }

}
