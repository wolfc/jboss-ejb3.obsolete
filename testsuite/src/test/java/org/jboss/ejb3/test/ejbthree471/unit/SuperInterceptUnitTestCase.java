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
package org.jboss.ejb3.test.ejbthree471.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree471.B;
import org.jboss.test.JBossTestCase;

/**
 * Interceptor must also apply to super-methods.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class SuperInterceptUnitTestCase extends JBossTestCase
{
   public SuperInterceptUnitTestCase(String name)
   {
      super(name);
   }

   public void testHasBeenIntercepted() throws Exception
   {
      B b = (B) getInitialContext().lookup("BBean/remote");
      
      String result = b.getOtherMessage();
      assertEquals("Intercepted: The Other Message", result);
   }
   
   public void testHasSuperBeenIntercepted() throws Exception
   {
      B b = (B) getInitialContext().lookup("BBean/remote");
      
      String result = b.getMessage();
      assertEquals("Intercepted: The Message", result);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(SuperInterceptUnitTestCase.class, "ejbthree471.jar");
   }
}
