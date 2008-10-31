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
package org.jboss.ejb3.test.ejbthree1157.unit;

import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1157.TestHome;
import org.jboss.test.JBossTestCase;

/**
 * Tests to ensure that EJBs with 2.1 Homes defining no create methods
 * fail deployment according to EJB 3.0 Core Specification 4.6.8 Bullet 4 and 
 * 4.6.10 Bullet 4
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class NoCreatesDefinedInHomeShouldFailDeploymentUnitTestCase extends JBossTestCase
{
   // Constructor

   public NoCreatesDefinedInHomeShouldFailDeploymentUnitTestCase(String name)
   {
      super(name);
   }

   // Suite

   public static Test suite() throws Exception
   {
      return getDeploySetup(NoCreatesDefinedInHomeShouldFailDeploymentUnitTestCase.class, "ejbthree1157.jar");
   }

   // Tests

   /**
    * Tests that a Stateless EJB with no create methods in the 2.1 home
    * fails deployment
    */
   public void testStatelessWithNoCreatesInHome() throws Exception
   {
      try
      {
         // Lookup Home
         PortableRemoteObject.narrow(this.getInitialContext().lookup(TestHome.JNDI_NAME_STATELESS), TestHome.class);
      }
      catch (NamingException ne)
      {
         // Expected
         return;
      }

      JBossTestCase.fail("Test Bean should fail deployment");

   }

   /**
    * Tests that a Stateful EJB with no create methods in the 2.1 home
    * fails deployment
    */
   public void testStatefulWithNoCreatesInHome() throws Exception
   {
      try
      {
         // Lookup Home
         PortableRemoteObject.narrow(this.getInitialContext().lookup(TestHome.JNDI_NAME_STATEFUL), TestHome.class);
      }
      catch (NamingException ne)
      {
         // Expected
         return;
      }

      JBossTestCase.fail("Test Bean should fail deployment");

   }
}
