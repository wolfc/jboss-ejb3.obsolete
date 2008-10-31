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
package org.jboss.ejb3.test.ejbthree1119.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1119.TestRemoteBusiness;
import org.jboss.test.JBossTestCase;

/**
 * Tests to ensure that a bean with the unspecified "value"
 * attribute on @Pool is properly handled by the container and
 * does not result in a Deployment Exception
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class EmptyPoolValueUnitTestCase extends JBossTestCase
{
   // Constructor
   public EmptyPoolValueUnitTestCase(String name)
   {
      super(name);
   }

   // Suite
   public static Test suite() throws Exception
   {
      return getDeploySetup(EmptyPoolValueUnitTestCase.class, "ejbthree1119.jar");
   }

   // Tests 

   /**
    * Test the EJB
    */
   public void testEmptyPoolValue() throws Exception
   {
      // Initialize
      TestRemoteBusiness test = null;

      // Lookup
      try
      {
         test = (TestRemoteBusiness) this.getInitialContext().lookup(TestRemoteBusiness.JNDI_NAME);
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
}
