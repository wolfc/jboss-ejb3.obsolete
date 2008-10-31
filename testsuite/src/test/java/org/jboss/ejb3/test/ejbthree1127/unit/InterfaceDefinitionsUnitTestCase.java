/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1127.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1127.DelegateRemoteBusiness;
import org.jboss.ejb3.test.ejbthree1127.TestLocal;
import org.jboss.ejb3.test.ejbthree1127.TestLocalBusiness;
import org.jboss.ejb3.test.ejbthree1127.TestRemote;
import org.jboss.ejb3.test.ejbthree1127.TestRemoteBusiness;
import org.jboss.ejb3.test.ejbthree1127.TestRemoteHome;
import org.jboss.test.JBossTestCase;

/**
 * Tests that deployments succeed/fail based upon partially-denoted
 * Remote/Local, Remote/Local Business, and Remote/Local Home interfaces ensuring
 * Spec-compliant defaults. 
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class InterfaceDefinitionsUnitTestCase extends JBossTestCase
{
   // Constructor
   public InterfaceDefinitionsUnitTestCase(String name)
   {
      super(name);
   }

   // Suite
   public static Test suite() throws Exception
   {
      return getDeploySetup(InterfaceDefinitionsUnitTestCase.class, "ejbthree1127.jar");
   }

   // Tests 

   /**
    * Ensure that the Test EJB with 3.0 View defined only by @Remote to 
    * Remote Business interface properly deploys
    */
   public void test30ViewRemoteBusinessInterfaceDefined() throws Exception
   {
      // Lookup EJB 
      TestRemoteBusiness ejb = (TestRemoteBusiness) this.getInitialContext().lookup(TestRemoteBusiness.JNDI_NAME);

      // Invoke on it
      assertEquals(ejb.test(), TestRemoteBusiness.RETURN_VALUE);
   }

   /**
    * Ensure that the Test EJB with 2.1 View defined only by @RemoteHome properly deploys
    */
   public void test21ViewRemoteInterfaceNotExplicitlyDefined() throws Exception
   {
      // Lookup EJB Home
      TestRemoteHome home = (TestRemoteHome) this.getInitialContext().lookup(TestRemoteHome.JNDI_NAME);

      // Obtain instance of EJB
      TestRemote ejb = home.create();

      // Invoke on it
      assertEquals(ejb.test(), TestRemote.RETURN_VALUE);
   }

   /**
    * Ensure that the Test EJB with 3.0 View defined only by @Local to 
    * Local Business interface properly deploys
    */
   public void test30ViewLocalBusinessInterfaceDefined() throws Exception
   {
      // Lookup Delegate EJB 
      DelegateRemoteBusiness ejb = (DelegateRemoteBusiness) this.getInitialContext().lookup(
            DelegateRemoteBusiness.JNDI_NAME);

      // Invoke on it
      assertEquals(ejb.testLocalBusinessOnlyDefined(), TestLocalBusiness.RETURN_VALUE);
   }

   /**
    * Ensure that the Test EJB with 2.1 View defined only by @LocalHome properly deploys
    */
   public void test21ViewLocalInterfaceNotExplicitlyDefined() throws Exception
   {
      // Lookup Delegate EJB 
      DelegateRemoteBusiness ejb = (DelegateRemoteBusiness) this.getInitialContext().lookup(
            DelegateRemoteBusiness.JNDI_NAME);

      // Invoke 
      assertEquals(ejb.testNoLocalExplicitlyDefined(), TestLocal.RETURN_VALUE);
   }
}