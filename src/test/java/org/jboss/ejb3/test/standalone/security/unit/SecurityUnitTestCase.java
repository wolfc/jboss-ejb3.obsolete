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
package org.jboss.ejb3.test.standalone.security.unit;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.test.standalone.security.Secured;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ejb.EJBAccessException;
import java.util.Hashtable;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class SecurityUnitTestCase
        extends TestCase
{

   public SecurityUnitTestCase(String name)
   {

      super(name);

   }

   public void testSecurity() throws Exception
   {
      Hashtable env = new Hashtable();
      env.put(Context.SECURITY_PRINCIPAL, "bill");
      env.put(Context.SECURITY_CREDENTIALS, "password");
      env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      InitialContext ctx = new InitialContext(env);
      Secured test = (Secured)ctx.lookup("SecuredBean/local");
      boolean exceptionThrown = false;
      try
      {
         test.echo("bill");
      }
      catch (EJBAccessException e)
      {
         exceptionThrown = true;
         e.printStackTrace();
      }
      assertTrue(exceptionThrown);
      env.put(Context.SECURITY_PRINCIPAL, "kabir");
      env.put(Context.SECURITY_CREDENTIALS, "password");
      ctx = new InitialContext(env);
      test.echo("bill");
   }


   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(SecurityUnitTestCase.class);

      // setup test so that embedded JBoss is started/stopped once for all tests here.
      TestSetup wrapper = new TestSetup(suite)
      {
         protected void setUp()
         {
            SecurityUnitTestCase.startupEmbeddedJboss();
         }

         protected void tearDown()
         {
            SecurityUnitTestCase.shutdownEmbeddedJboss();
         }
      };

      return wrapper;
   }

   public static void startupEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.boot(null);
      EJB3StandaloneBootstrap.deployXmlResource("security-beans.xml");
      EJB3StandaloneBootstrap.scanClasspath("embedded-security.jar");
   }

   public static void shutdownEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.shutdown();
   }

}
