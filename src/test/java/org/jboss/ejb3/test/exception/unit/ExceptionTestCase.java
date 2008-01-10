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
package org.jboss.ejb3.test.exception.unit;

import javax.naming.InitialContext;
import org.jboss.ejb3.test.exception.Foo1;
import org.jboss.ejb3.test.exception.FooException1;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class ExceptionTestCase
    extends JBossTestCase {

   private static final Logger log = Logger
         .getLogger(ExceptionTestCase.class);

   public ExceptionTestCase(String name)
   {
      super(name);
   }
   
   public void testException() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      Foo1 foo = (Foo1) jndiContext.lookup("FooBean1/remote");
      assertNotNull(foo);
      
      try {
         foo.bar();
         assertTrue(false);
      } catch (Throwable t){
         t.printStackTrace();
         assertTrue(t instanceof FooException1);
      }
      
      String status = foo.getStatus();
      assertEquals("Caught FooException1", status);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ExceptionTestCase.class, "exception.jar");
   }

}
