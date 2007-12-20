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
package org.jboss.ejb3.test.ejbthree1154.unit;

import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1154.DelegateRemoteBusiness;
import org.jboss.ejb3.test.ejbthree1154.TestRemote;
import org.jboss.ejb3.test.ejbthree1154.TestRemoteBusiness;
import org.jboss.ejb3.test.ejbthree1154.TestRemoteHome;
import org.jboss.test.JBossTestCase;

/**
 * A Test to ensure that an EJB with the 2.1 Local/Remote interfaces
 * not explicitly-defined can be properly returned from the "create"
 * methods of the Home interfaces
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class NotExplicitlyDefined21InterfaceReturnsCorrectTypeFromHomeUnitTestCase extends JBossTestCase
{
   // Constructor

   public NotExplicitlyDefined21InterfaceReturnsCorrectTypeFromHomeUnitTestCase(String name)
   {
      super(name);
   }

   // Suite

   public static Test suite() throws Exception
   {
      return getDeploySetup(NotExplicitlyDefined21InterfaceReturnsCorrectTypeFromHomeUnitTestCase.class,
            "ejbthree1154.jar");
   }

   // Tests

   /**
    * Tests the 2.1 Remote view from a Home create() method
    */
   public void testRemoteInterfaceFromHomeCreate() throws Exception
   {
      // Initialize
      TestRemoteHome home = null;

      // Lookup Home
      home = (TestRemoteHome) PortableRemoteObject.narrow(this.getInitialContext().lookup(TestRemoteHome.JNDI_NAME),
            TestRemoteHome.class);

      // Get 2.1 View
      TestRemote ejb = home.create();

      // Invoke and ensure correct
      assertEquals(ejb.test(), TestRemote.RETURN_VALUE);

   }

   /**
    * Tests the Business Remote view from a JNDI Lookup
    */
   public void testRemoteBusinessInterfaceFromLookup() throws Exception
   {
      // Initialize
      TestRemoteBusiness ejb = null;

      // Lookup Business
      ejb = (TestRemoteBusiness) PortableRemoteObject.narrow(this.getInitialContext().lookup(
            TestRemoteBusiness.JNDI_NAME), TestRemoteBusiness.class);

      // Invoke and ensure correct
      assertEquals(ejb.test(), TestRemoteBusiness.RETURN_VALUE);

   }

   /**
    * Tests the 2.1 Local view from a Home create() method, 
    * via a Remote Business Delegate
    */
   public void testLocalInterfaceFromHomeCreate() throws Exception
   {
      // Initialize
      DelegateRemoteBusiness delegate = this.getRemoteDelegate();

      // Invoke and ensure correct
      assertTrue(delegate.testBeanReturnsCorrectLocal21ViewFromHomeCreate());

   }

   /**
    * Tests the 2.1 Local view from a Home create() method, 
    * via a Remote Business Delegate
    */
   public void testLocalBusinessInterface() throws Exception
   {
      // Initialize
      DelegateRemoteBusiness delegate = this.getRemoteDelegate();

      // Invoke and ensure correct
      assertTrue(delegate.testBeanReturnsCorrectlyFromLocalBusinessInterface());

   }

   // Internal Helper Methods
   private DelegateRemoteBusiness getRemoteDelegate() throws Exception
   {
      // Initialize
      DelegateRemoteBusiness delegate = null;

      // Lookup Delegate
      delegate = (DelegateRemoteBusiness) PortableRemoteObject.narrow(this.getInitialContext().lookup(
            DelegateRemoteBusiness.JNDI_NAME), DelegateRemoteBusiness.class);

      // Return 
      return delegate;
   }
}
