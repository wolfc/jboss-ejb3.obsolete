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
package org.jboss.ejb3.profile3_1.test.nointerface.unit;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import org.jboss.ejb3.profile3_1.test.common.AbstractProfile3_1_TestCase;
import org.jboss.ejb3.profile3_1.test.nointerface.NoInterfaceStatelessBean;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * NoInterfaceBeanTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceBeanTestCase extends AbstractProfile3_1_TestCase
{
   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(NoInterfaceBeanTestCase.class);

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      bootstrap();
   }

   @AfterClass
   public static void afterClass() throws Throwable
   {
      shutdown();
   }

   /**
    * TODO: Jaikiran - This is currently in work-in-progress state. 
    * The intention of this test (at present) is to ensure that the profile3_1 test framework is ready
    * for use. This will just test that the bean gets deployed.
    * @throws Throwable
    */
   @Test
   public void testBeanDeployment() throws Throwable
   {

      // @see the javadoc of this method for more details about what this is expected to do
      deploy(NoInterfaceStatelessBean.class);
      
      Context ctx = new InitialContext();
      NamingEnumeration<Binding> bindings = ctx.listBindings("");
      while (bindings.hasMoreElements())
      {
         Binding binding = bindings.nextElement();
         logger.info("JNDI Binding: " + binding.getName() + " val " + binding.getObject() + " class " + binding.getClass());
      }

   }

}
