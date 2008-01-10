/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.ejbthree1062.unit;

import java.util.Date;

import javax.naming.NameNotFoundException;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1062.Tester;
import org.jboss.test.JBossTestCase;

/**
 * If bean class implements a single interface, that interface is assumed to be the busi-
 * ness interface of the bean. This business interface will be a local interface unless the
 * interface is designated as a remote business interface by use of the Remote annota-
 * tion on the bean class or interface or by means of the deployment descriptor.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class DefaultLocalBusinessUnitTestCase extends JBossTestCase
{
   public DefaultLocalBusinessUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(DefaultLocalBusinessUnitTestCase.class, "ejbthree1062.jar");
   }
   
   public void testCalculator() throws Exception
   {
      // Guard for errors
      try
      {
         getInitialContext().lookup("CalculatorBean/local");
      }
      catch(NameNotFoundException e)
      {
         fail("CalculatorBean was not deployed properly");
      }
      Tester tester = (Tester) getInitialContext().lookup("TesterBean/remote");
      int actual = tester.add(1, 2);
      assertEquals(3, actual);
   }
   
   public void testSayHiTo() throws Exception
   {
      // Guard for errors
      try
      {
         getInitialContext().lookup("MyStatelessBean/local");
      }
      catch(NameNotFoundException e)
      {
         fail("MyStatelessBean was not deployed properly");
      }
      Tester tester = (Tester) getInitialContext().lookup("TesterBean/remote");
      Date date = new Date();
      String expected = "Hi " + date;
      String actual = tester.sayHiTo(date.toString());
      assertEquals(expected, actual);
   }
}
