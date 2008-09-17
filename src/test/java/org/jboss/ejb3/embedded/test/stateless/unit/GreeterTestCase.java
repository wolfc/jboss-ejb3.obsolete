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
package org.jboss.ejb3.embedded.test.stateless.unit;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.ejb.EJBContainer;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.embedded.test.stateless.Greeter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class GreeterTestCase
{
   @AfterClass
   public static void afterClass()
   {
      EJBContainer current = EJBContainer.getCurrentEJBContainer();
      if(current != null)
         current.close();
   }
   
   @BeforeClass
   public static void beforeClass()
   {
      Properties properties = new Properties();
      String module = getURLToTestClasses();
      EJBContainer.createEJBContainer(properties, module);
   }
   
   private static String getURLToTestClasses()
   {
      String p = "org/jboss/ejb3/embedded/test";
      URL url = Thread.currentThread().getContextClassLoader().getResource(p);
      String s = url.toString();
      return s.substring(0, s.length() - p.length());
   }
   
   @Test
   public void test1() throws NamingException
   {
      InitialContext ctx = new InitialContext();
      Greeter greeter = (Greeter) ctx.lookup("GreeterBean/local");
      String now = new Date().toString();
      String actual = greeter.sayHi(now);
      assertEquals("Hi " + now, actual);
   }
   
   @Test
   public void testGreeter2() throws NamingException
   {
      InitialContext ctx = new InitialContext();
      Greeter greeter = (Greeter) ctx.lookup("Greeter2/local");
      String now = new Date().toString();
      String actual = greeter.sayHi(now);
      assertEquals("Hi " + now, actual);
   }
}
