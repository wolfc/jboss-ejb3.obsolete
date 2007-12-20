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
package org.jboss.ejb3.test.ejbthree1155.unit;

import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1155.TestHome;
import org.jboss.ejb3.test.ejbthree1155.TestRemote1;
import org.jboss.ejb3.test.ejbthree1155.TestRemote2;
import org.jboss.test.JBossTestCase;

/**
 * A Test to ensure that an EJB with many 2.1 Local/Remote interfaces
 * not explicitly-defined can be properly returned from the "create"
 * methods of the Home interfaces
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class MultipleCreateMethodsDefineMultipleRemoteInterfacesUnitTestCase extends JBossTestCase
{
   // Constructor

   public MultipleCreateMethodsDefineMultipleRemoteInterfacesUnitTestCase(String name)
   {
      super(name);
   }

   // Suite

   public static Test suite() throws Exception
   {
      return getDeploySetup(MultipleCreateMethodsDefineMultipleRemoteInterfacesUnitTestCase.class, "ejbthree1155.jar");
   }

   // Tests

   /**
    * Tests Remote Interface 1 from a Home create<METHOD>() method
    */
   public void testRemoteInterface1FromHomeCreate() throws Exception
   {
      // Initialize
      TestHome home = null;

      // Lookup Home
      home = (TestHome) PortableRemoteObject
            .narrow(this.getInitialContext().lookup(TestHome.JNDI_NAME), TestHome.class);

      // Get 2.1 View
      TestRemote1 ejb = home.createRemote1();

      // Invoke
      ejb.test1();

      // Remove
      ejb.remove();
   }

   /**
    * Tests Remote Interface 2 from a Home create<METHOD>() method
    */
   public void testRemoteInterface2FromHomeCreate() throws Exception
   {
      // Initialize
      TestHome home = null;

      // Lookup Home
      home = (TestHome) PortableRemoteObject
            .narrow(this.getInitialContext().lookup(TestHome.JNDI_NAME), TestHome.class);

      // Get 2.1 View
      TestRemote2 ejb = home.createRemote2();

      // Invoke
      ejb.test2();

      // Remove
      ejb.remove();
   }
}
