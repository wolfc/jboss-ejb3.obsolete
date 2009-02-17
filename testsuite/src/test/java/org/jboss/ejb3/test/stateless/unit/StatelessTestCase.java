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
package org.jboss.ejb3.test.stateless.unit;

import javax.ejb.EJBAccessException;

import junit.framework.Test;

import org.jboss.ejb3.test.stateless.AnonymousStateless;
import org.jboss.ejb3.test.stateless.CheckedStateless;
import org.jboss.ejb3.test.stateless.RunAsStateless;
import org.jboss.ejb3.test.stateless.UnsecuredStateless;
import org.jboss.logging.Logger;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
public class StatelessTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(StatelessTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public StatelessTestCase(String name)
   {
      super(name);
   }
 
   public void testCallerPrincipal() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
       
      RunAsStateless runAs = (RunAsStateless) getInitialContext().lookup("RunAsStatelessEjbName/remote");
      assertNotNull(runAs);
      
      String principal = runAs.getCallerPrincipal();
      assertEquals("somebody", principal);
      
      principal = runAs.getCheckedCallerPrincipal();
      /** Because a run-as annotation would switch the caller context. So unless
       * a run as principal is configured by the bean provider, an anonymous
       * caller principal is seen.
       */
      //assertEquals("somebody", principal);
      assertEquals("anonymous", principal);
   }

   public void testRunAs() throws Exception
   {
      CheckedStateless checked = (CheckedStateless)getInitialContext().lookup("CheckedStatelessBean/remote");
      
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
      
      int result = checked.method(1);
      assertEquals(1,result);
      
      client.logout();
      
      client.setSimple("rolefail", "password");
      client.login();
      
      try {
         checked.method(2);
         assertTrue(false);
      } catch (Exception e){
         assertTrue(e instanceof EJBAccessException);
      }
      
      RunAsStateless runAs =
            (RunAsStateless) getInitialContext().lookup("RunAsStatelessEjbName/remote");
      
      int i = runAs.method(3);
      assertEquals(3, i);
   } 
   
   public void testAnonymous() throws Exception
   {
      AnonymousStateless anonymous = (AnonymousStateless)getInitialContext().lookup("AnonymousStatelessBean/remote");
      
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.logout();
      
      try
      {
         anonymous.method(1);
//         fail();
      } 
      catch (Exception e)
      {
 //        throw e;
      }
   }
   
   /**
    * EJBTHREE-1051: A bean without a security domain should still 
    * propagate the caller.
    * 
    * @throws Exception
    */
   public void testUnsecureToSecureWithCredentials() throws Exception
   {
      UnsecuredStateless stateless = (UnsecuredStateless)getInitialContext().lookup("UnsecuredStatelessBean/remote");
      
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
      
      try
      {
         assertEquals(1, stateless.method(1));
      }
      catch(EJBAccessException e)
      {
         fail("EJBTHREE-1051: caller wasn't propagated");
      }
   }

   public void testUnsecureToSecureWithoutCredentials() throws Exception
   {
      UnsecuredStateless stateless = (UnsecuredStateless)getInitialContext().lookup("UnsecuredStatelessBean/remote");
      
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.logout();
      
      try
      {
         stateless.method(2);
         fail("Should not have been allowed");
      }
      catch (EJBAccessException e)
      {
         // okay
      }
   }
   
   public void testTemplateInterfaceTx() throws Exception
   {
      UnsecuredStateless stateful = (UnsecuredStateless)getInitialContext().lookup("UnsecuredStatelessBean/remote");
      assertNotNull(stateful);
      
      try
      {
         stateful.testMandatoryTx("123");
         fail("should have caught exception");
      }
      catch (javax.ejb.EJBTransactionRequiredException e)
      {
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(StatelessTestCase.class, "stateless-test.jar");
   }
}