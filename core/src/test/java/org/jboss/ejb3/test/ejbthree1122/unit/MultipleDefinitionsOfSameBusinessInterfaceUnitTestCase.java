/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1122.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1122.TestRemoteBusinessInterface;
import org.jboss.test.JBossTestCase;

/**
 * Tests to ensure that a bean with the same @Remote business interface
 * defined multiple times (both explicitly and via inheritance) 
 * deploys successfully
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class MultipleDefinitionsOfSameBusinessInterfaceUnitTestCase extends JBossTestCase
{
   // Constructor
   public MultipleDefinitionsOfSameBusinessInterfaceUnitTestCase(String name)
   {
      super(name);
   }

   // Suite
   public static Test suite() throws Exception
   {
      return getDeploySetup(MultipleDefinitionsOfSameBusinessInterfaceUnitTestCase.class, "ejbthree1122.jar");
   }

   // Tests 

   /**
    * Test the Stateless Remote EJB
    */
   public void testTestStatelessRemoteEjb() throws Exception
   {
      this.internalTestRemoteView(TestRemoteBusinessInterface.JNDI_NAME_STATELESS_REMOTE);
   }

   /**
    * Test the Stateless Local EJB
    */
   public void testTestStatelessLocalEjb() throws Exception
   {
      this.internalTestLocalView(TestRemoteBusinessInterface.JNDI_NAME_STATELESS_REMOTE);
   }

   /**
    * Test the Service Remote EJB
    */
   public void testTestServiceRemoteEjb() throws Exception
   {
      this.internalTestRemoteView(TestRemoteBusinessInterface.JNDI_NAME_SERVICE_REMOTE);
   }

   /**
    * Test the Service Local EJB
    */
   public void testTestServiceLocalEjb() throws Exception
   {
      this.internalTestLocalView(TestRemoteBusinessInterface.JNDI_NAME_SERVICE_REMOTE);
   }

   /**
    * Test the Stateful Remote EJB
    */
   public void testTestStatefulRemoteEjb() throws Exception
   {
      this.internalTestRemoteView(TestRemoteBusinessInterface.JNDI_NAME_STATEFUL_REMOTE);
   }

   /**
    * Test the Stateful Local EJB
    */
   public void testTestStatefulLocalEjb() throws Exception
   {
      this.internalTestLocalView(TestRemoteBusinessInterface.JNDI_NAME_STATEFUL_REMOTE);
   }

   // Internal Helper Methods

   private void internalTestRemoteView(String jndiName)
   {
      // Initialize
      TestRemoteBusinessInterface test = null;

      // Lookup
      try
      {
         test = (TestRemoteBusinessInterface) this.getInitialContext().lookup(
               jndiName);
      }
      catch (Throwable t)
      {
         log.error(t);
         JBossTestCase.fail(t.getMessage());
      }
      // Ensure lookup succeeds; bean is deployed
      JBossTestCase.assertNotNull(test);

      // Invoke
      try
      {
         test.test();
      }
      catch (Throwable t)
      {
         log.error(t);
         JBossTestCase.fail("Invocation failed: " + t.getMessage());
      }
   }

   private void internalTestLocalView(String remoteDelegateJndiName)
   {
      // Initialize
      TestRemoteBusinessInterface test = null;

      // Lookup
      try
      {
         test = (TestRemoteBusinessInterface) this.getInitialContext().lookup(remoteDelegateJndiName);
      }
      catch (Throwable t)
      {
         log.error(t);
         JBossTestCase.fail(t.getMessage());
      }
      // Ensure lookup succeeds; bean is deployed
      JBossTestCase.assertNotNull(test);

      // Invoke on local
      try
      {
         test.testLocal();
      }
      catch (Throwable t)
      {
         log.error(t);
         JBossTestCase.fail("Invocation failed: " + t.getMessage());
      }
   }

}
