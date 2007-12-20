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
package org.jboss.ejb3.test.ejbthree1148.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1148.TestRemoteBusiness;
import org.jboss.test.JBossTestCase;

/**
 * Tests that a Stateless EJB with an ejbCreate callback 
 * is not called when the EJB has no 2.1 View
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class EjbCreateWithout21ViewUnitTestCase extends JBossTestCase
{
   // Constructor
   public EjbCreateWithout21ViewUnitTestCase(String name)
   {
      super(name);
   }

   // Suite
   public static Test suite() throws Exception
   {
      return getDeploySetup(EjbCreateWithout21ViewUnitTestCase.class, "ejbthree1148.jar");
   }

   // Tests 

   /**
    * Ensure ejbCreate() is not used as a valid callback when no 2.1 view is defined
    */
   public void testRemoteInterfaceNotExplicitlyDefined() throws Exception
   {
      // Lookup Business Interface
      TestRemoteBusiness ejb = (TestRemoteBusiness) this.getInitialContext().lookup(TestRemoteBusiness.JNDI_NAME);

      // Invoke on it, ensuring value is expected
      assertEquals(ejb.test(), TestRemoteBusiness.RETURN_VALUE);
   }
}