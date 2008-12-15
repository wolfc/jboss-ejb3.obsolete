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
package org.jboss.ejb3.test.ejbthree1624.unit;

import javax.naming.Context;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.ejb3.test.ejbthree1624.AccessBean;
import org.jboss.ejb3.test.ejbthree1624.AccessRemoteBusiness;
import org.jboss.test.JBossTestCase;

/**
 * Ejb3IntoMcBeanInjectionTestCase
 * 
 * Test Cases to ensure @EJB Injection into MC Beans succeeds
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Ejb3IntoMcBeanInjectionTestCase extends JBossTestCase
{

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   public Ejb3IntoMcBeanInjectionTestCase(String name)
   {
      super(name);
   }

   // --------------------------------------------------------------------------------||
   // Suite --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   public static Test suite() throws Exception
   {
      return getDeploySetup(Ejb3IntoMcBeanInjectionTestCase.class, "ejbthree1625.jar");
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /**
    * Tests that a Local Business interface can be resolved/injected 
    * into an MC Bean
    */
   public void testLocalBusinessInterfaceInjectionIntoMcBean() throws Throwable
   {
      // Get the naming context
      Context context = this.getInitialContext();

      // Get the access bean
      AccessRemoteBusiness access = (AccessRemoteBusiness) context.lookup(AccessBean.class.getSimpleName() + "/remote");

      // Invoke
      int result = access.add(1, 2, 3);
      int expected = 6;

      // Test
      TestCase.assertEquals(expected, result);
   }

}
