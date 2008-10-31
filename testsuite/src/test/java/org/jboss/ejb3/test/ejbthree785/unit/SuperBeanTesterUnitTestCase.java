/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree785.unit;

import java.util.Date;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree785.MyStatelessLocal;
import org.jboss.ejb3.test.ejbthree785.MyStatelessRemote;
import org.jboss.ejb3.test.ejbthree785.Tester;
import org.jboss.test.JBossTestCase;

/**
 * Test to see if a super can have the business interface.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author <a href="mailto:alr@alrubinger.com">ALR</a>
 * @version $Revision$
 */
public class SuperBeanTesterUnitTestCase extends JBossTestCase
{

   public SuperBeanTesterUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Ensures that a remote view of a business interface implemented by a
    * superclass of an EJB's Implementation Class is deployed and invokable
    * 
    * @throws Exception
    */
   public void testSuperRemoteInvokable() throws Exception
   {
      MyStatelessRemote session = (MyStatelessRemote) getInitialContext().lookup(MyStatelessRemote.JNDI_NAME_REMOTE);
      Date date = new Date();
      String expected = "Hi " + date.toString();
      String actual = session.sayHiTo(date.toString());
      assertEquals(expected, actual);
   }

   /**
    * Ensures that dependencies may be made upon EJBs with
    * 
    * @Local implemented by superclass of an EJB's Implementation Class, and
    *        that invocation succeeds.
    * 
    * @throws Exception
    */
   public void testSuperLocalViaRemoteDelegate() throws Exception
   {
      Tester session = (Tester) getInitialContext().lookup(Tester.JNDI_NAME_REMOTE);
      Date date = new Date();
      String expected = "Hi " + date.toString();
      String actual = session.sayHiTo(date.toString());
      assertEquals(expected, actual);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(SuperBeanTesterUnitTestCase.class, "ejbthree785.jar");
   }
}
