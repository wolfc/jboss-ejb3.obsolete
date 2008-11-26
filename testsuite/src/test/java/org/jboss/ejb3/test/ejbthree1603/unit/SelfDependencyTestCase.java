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
package org.jboss.ejb3.test.ejbthree1603.unit;

import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.ejb3.test.ejbthree1603.SelfDependencyBean;
import org.jboss.ejb3.test.ejbthree1603.SelfDependencyRemoteBusiness;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * SelfDependencyTestCase
 * 
 * Test Cases to ensure an EJB with 
 * dependencies upon its own views succeeds
 * deployment and invocation.
 * 
 * EJBTHREE-1603
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SelfDependencyTestCase extends JBossTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SelfDependencyTestCase.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public SelfDependencyTestCase(String name)
   {
      super(name);
   }

   // --------------------------------------------------------------------------------||
   // Harness ------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public static Test suite() throws Exception
   {
      return getDeploySetup(SelfDependencyTestCase.class, "ejbthree1603.jar");
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that an EJB with dependencies upon its own views
    * succeeds deployment and invocation
    */
   public void testSelfDependencyInvocationsSucceed() throws Exception
   {
      // Define JNDI Name
      String jndiName = SelfDependencyBean.class.getSimpleName() + "/remote";

      // Get a naming Context
      InitialContext context = this.getInitialContext();

      // Lookup the bean
      SelfDependencyRemoteBusiness ejb = (SelfDependencyRemoteBusiness) context.lookup(jndiName);

      // Define the expected return value
      String expected = SelfDependencyRemoteBusiness.RETURN_VALUE;
      log.info("Expected value: " + expected);

      // Obtain the return value from both remote and local invocations
      String fromRemote = ejb.getReturnValue();
      log.info("From remote: " + fromRemote);
      String fromLocal = ejb.getReturnValueFromLocal();
      log.info("From local: " + fromLocal);

      // Test
      TestCase.assertEquals("The remote invocation did not return as expected", expected, fromRemote);
      TestCase.assertEquals("The local invocation did not return as expected", expected, fromLocal);
   }

}
