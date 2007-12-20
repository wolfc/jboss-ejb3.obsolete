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

import javax.naming.InitialContext;

/**
 * @version <tt>$Revision: 61136 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class BindingTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(BindingTestCase.class);
   
   /**
    *
    * @param name  Testcase name
    */
   public BindingTestCase(String name)
   {
      super(name);
   }
   
   public void testCleanup() throws Exception
   {
      this.redeploy("naming.jar");
      this.undeploy("naming.jar");
      
      InitialContext jndiContext = new InitialContext();
      
      try
      {
         Object o = jndiContext.lookup("ENCTests");
         org.jnp.interfaces.NamingContext nc = (org.jnp.interfaces.NamingContext)o;
         Object ejbs = nc.lookup("ejbs");
         nc.destroySubcontext("ejbs");
         fail("ENCTests should no longer be bound");
      }
      catch (javax.naming.NamingException e)
      {
      }
      
   }

   public static Test suite() throws Exception
   {
      return new TestSuite(BindingTestCase.class);
   }

}
