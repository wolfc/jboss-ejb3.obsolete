/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.naming.unit;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

import org.jboss.ejb3.test.naming.Stateful;

/**
 * @version <tt>$Revision: 61136 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class BindFailureTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(BindFailureTestCase.class);
   
   /**
    * Constructor for the ENCUnitTestCase object
    *
    * @param name  Testcase name
    */
   public BindFailureTestCase(String name)
   {
      super(name);
   }
   
   public void testJndiBindingExceptions() throws Exception
   {
      try
      {
// test was originally to catch redeploying with the same jndi name, but now we are
// using rebind instead of bind
         this.redeploy("bind-failure-test.jar");
         this.redeploy("bind-failure-test.jar");
//         fail();
      } catch (Exception e)
      {
         log.info("caught " + e.getClass().getName() + " " + e.getMessage() + " " + e.getCause());
         System.out.println("caught " + e.getClass().getName() + " " + e.getMessage() + " " + e.getCause());
         fail();
      }
      
      this.undeploy("bind-failure-test.jar");
      
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      return suite;
   }

}
