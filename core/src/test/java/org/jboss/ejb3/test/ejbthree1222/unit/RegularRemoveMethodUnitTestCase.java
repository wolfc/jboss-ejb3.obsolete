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
package org.jboss.ejb3.test.ejbthree1222.unit;

import javax.ejb.NoSuchEJBException;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.ejb3.test.ejbthree1222.TestStatefulWithRemoveMethodRemote;
import org.jboss.test.JBossTestCase;

/**
 * RegularRemoveMethodUnitTestCase
 * 
 * Tests that "void remove()" method not annotated as
 * @Remove acts like any plain, traditional method
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class RegularRemoveMethodUnitTestCase extends JBossTestCase
{
   // Class Members
   private static final Log logger = LogFactory.getLog(RegularRemoveMethodUnitTestCase.class);

   // Constructor

   public RegularRemoveMethodUnitTestCase(String name)
   {
      super(name);
   }

   // Tests

   /**
    * Tests that a call to an unannotated "void remove()"
    * method is a traditional call
    */
   public void testNormalMethodNamedRemove() throws Exception
   {
      // Lookup Bean
      TestStatefulWithRemoveMethodRemote bean = (TestStatefulWithRemoveMethodRemote) this.getInitialContext().lookup(
            TestStatefulWithRemoveMethodRemote.JNDI_NAME);

      // Reset the number of calls, if any
      bean.reset();

      // Make a call
      try
      {
         bean.remove();
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
         TestCase.fail(e.getMessage());
      }

      try
      {
         // Ensure the call was received and the bean instance is still available
         TestCase.assertEquals(1, bean.getCalls());
      }
      catch (NoSuchEJBException nsee)
      {
         // "void remove()" should not have been handled as EJB2.1 call
         TestCase.fail("Bean should not have been removed: " + nsee.getMessage());
      }

   }

   // Suite

   public static Test suite() throws Exception
   {
      return getDeploySetup(RegularRemoveMethodUnitTestCase.class, "ejbthree1222.jar");
   }
}
