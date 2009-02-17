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
package org.jboss.ejb3.test.initial.unit;

import javax.ejb.EJBAccessException;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import junit.framework.Test;

import org.jboss.ejb3.test.initial.ClassInjected;
import org.jboss.ejb3.test.initial.InterceptedSFTest;
import org.jboss.ejb3.test.initial.SecuredTest;
import org.jboss.ejb3.test.initial.StatefulTestRemote;
import org.jboss.ejb3.test.initial.TestRemote;
import org.jboss.logging.Logger;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class RemoteUnitTestCase
extends JBossTestCase
{
   private static Logger log = Logger.getLogger(RemoteUnitTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public RemoteUnitTestCase(String name)
   {

      super(name);

   }

   public void testStateful() throws Exception
   {
      StatefulTestRemote test = (StatefulTestRemote) getInitialContext().lookup("initial-ejb3-test/StatefulTestBean/remote");
      test.setState("hello world");
      assertEquals(test.getState(), "hello world");
      test.endSession();

   }

   public void testSimple() throws Exception
   {
      TestRemote test = (TestRemote) this.getInitialContext().lookup("initial-ejb3-test/TestBean/remote");
      String echo = test.testMe("echo");
      assertEquals(echo, "echo");
   }

   public void testTx() throws Exception
   {
      // Test basic tx propagation
      UserTransaction ut = (UserTransaction) getInitialContext().lookup("UserTransaction");
      TestRemote test = (TestRemote) this.getInitialContext().lookup("initial-ejb3-test/TestBean/remote");
      ut.begin();
      test.mandatory();
      // call mandatory a second time to test that TX has been dissacciated correctly
      test.mandatory();
      ut.commit();

   }

   public void testSecurity() throws Exception
   {
      // Test basic security propagation
      InitialContext ctx = getInitialContext();
      SecuredTest test = (SecuredTest) ctx.lookup("initial-ejb3-test/SecuredTestBean/remote");
      
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();

      System.out.println("Calling initial security tests....");
      test.unchecked();
      System.out.println("Calling testDefault()....");
      test.testDefault();
      System.out.println("Calling secured()....");
      test.secured();
      client.logout();

      System.out.println("Calling security fine grain tests....");
      client.setSimple("authfail", "password");
      client.login();

      boolean securityFailure = true;
      try
      {
         test.secured();
      }
      catch (EJBAccessException ignored)
      {
         System.out.println("log="+log+":ignored:"+ignored);
         log.info(ignored.getMessage());
         securityFailure = false;
      }

      if (securityFailure) throw new RuntimeException("auth failure was not caught for method");

      securityFailure = true;
      client.logout();
      client.setSimple("rolefail", "password");
      client.login();
      try
      {
         test.secured();
      }
      catch (EJBAccessException ignored)
      {
         log.info(ignored.getMessage());
         securityFailure = false;
      }
      if (securityFailure) throw new RuntimeException("role failure was not caught for method");

      client.logout();
      client.setSimple("somebody","password");
      client.login();
      log.info("test exclusion");
      securityFailure = true;
      try
      {
         test.excluded();
      }
      catch (EJBAccessException ignored)
      {
         log.info(ignored.getMessage());
         securityFailure = false;
      }
      if (securityFailure) throw new RuntimeException("excluded failure was not caught for method");
      client.logout();
   }

   public void testInterceptors() throws Exception
   {
      InterceptedSFTest test = (InterceptedSFTest) this.getInitialContext().lookup("initial-ejb3-test/InterceptedSFTestBean/remote");
      int ret = test.testMethod(5);
      int expected = 1010;
      if (ret != expected) throw new Exception("return value was not " + expected + ", it was: " + ret);
   }

   public void testCallbacks() throws Exception
   {
      org.jboss.ejb3.test.initial.TestStatus status =
      (org.jboss.ejb3.test.initial.TestStatus) getInitialContext().lookup("initial-ejb3-test/TestStatusBean/remote");
      status.clear();
      InterceptedSFTest test = (InterceptedSFTest) getInitialContext().lookup("initial-ejb3-test/InterceptedSFTestBean/remote");

      test.testMethod(5);
      int val = test.getVal();
      if (val != 5) throw new Exception("test.getVal() returned " + val + " instead of 5");
      if (!status.postConstruct()) throw new Exception("PostConstruct should be called for SFSB");

      //Exhaust cache to force passivation
      InterceptedSFTest test2 = (InterceptedSFTest) getInitialContext().lookup("initial-ejb3-test/InterceptedSFTestBean/remote");
      test2.testMethod(3);

      if (!status.prePassivate()) throw new Exception("PrePassivate should be called for SFSB");

      //Activate original bean
      val = test.getVal();
      if (val != 5) throw new Exception("test.getVal() returned " + val + " instead of 5");
      if (!status.postActivate()) throw new Exception("PostActivate should be called for SFSB");

      //Remove method
      test.clear();
      if (!status.preDestroy()) throw new Exception("PreDestroy should be called for SFSB");

      test2.clear();
   }

   public void testClassInjected() throws Exception
   {
      ClassInjected test = (ClassInjected)getInitialContext().lookup("initial-ejb3-test/ClassInjectedBean/remote");
      assertTrue(test.isInjected());
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(RemoteUnitTestCase.class, "initial-ejb3-test.sar");
   }

}
