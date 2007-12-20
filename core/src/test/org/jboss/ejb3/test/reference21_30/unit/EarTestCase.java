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
package org.jboss.ejb3.test.reference21_30.unit;

import javax.naming.*;

import org.jboss.ejb3.test.reference21_30.*;
import org.jboss.logging.Logger;

import org.jboss.ejb3.test.reference21_30.Test3;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * Test for EJB3.0/EJB2.1 references
 * 
 * @version <tt>$Revision: 61219 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class EarTestCase
    extends JBossTestCase {

   private static final Logger log = Logger
   .getLogger(EarTestCase.class);

   public EarTestCase(String name)
   {
      super(name);
   }

   public void testEjbInjection() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      Test3 test3 = (Test3)jndiContext.lookup("Test3Remote");
      assertNotNull(test3);
      test3.testAccess();
      
      Test2Home home = (Test2Home)jndiContext.lookup("Test2");
      assertNotNull(home);
      Test2 test2 = home.create();
      assertNotNull(test2);
      test2.testAccess();
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(EarTestCase.class, "multideploy.ear");
   }
}
