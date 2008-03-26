/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree1059.unit;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.ejb3.test.ejbthree1059.AccessRemoteBusiness;
import org.jboss.ejb3.test.ejbthree1059.TestRemoteBusiness;
import org.jboss.ejb3.test.ejbthree1059.TestRemoteHome;
import org.jboss.test.JBossTestCase;

/**
 * Tests to ensure that a business interface is not returned from
 * an EJBHome/EJBLocalHome create<METHOD> method.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class NoBusinessInterfaceFromEjb21CreateUnitTestCase extends JBossTestCase
{
   public NoBusinessInterfaceFromEjb21CreateUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(NoBusinessInterfaceFromEjb21CreateUnitTestCase.class, "ejbthree1059.jar");
   }

   public void testRemoteBusinessFromEjb21CreateFails() throws Exception
   {

      // Obtain Home
      TestRemoteHome home = (TestRemoteHome) this.getInitialContext().lookup(TestRemoteHome.JNDI_NAME);

      // Attempt an invalid creation
      home.createInvalid();

   }

   public void testLocalBusinessFromEjb21CreateFails() throws Exception
   {

      // Obtain access bean
      AccessRemoteBusiness access = (AccessRemoteBusiness) this.getInitialContext().lookup(
            AccessRemoteBusiness.JNDI_NAME);

      // Attempt invalid creation
      try
      {
         access.testInvalid();
      }
      catch (Exception e)
      {
         TestCase.fail(e.getMessage());
      }

   }

}
