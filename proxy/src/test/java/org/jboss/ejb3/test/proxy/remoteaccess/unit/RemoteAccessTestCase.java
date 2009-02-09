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
package org.jboss.ejb3.test.proxy.remoteaccess.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulRemoteBusiness;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessRemote;
import org.jboss.ejb3.test.proxy.remoteaccess.JndiPropertiesToJndiRemotePropertiesHackCl;
import org.jboss.ejb3.test.proxy.remoteaccess.MockServerController;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * RemoteAccessTestCase
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */

public class RemoteAccessTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(RemoteAccessTestCase.class);

   private static final String JNDI_NAME_SLSB_LOCAL = "MyStatelessBean/local";

   private static final String JNDI_NAME_SLSB_REMOTE = "MyStatelessBean/remote";

   private static final String JNDI_NAME_SFSB_REMOTE = "MyStatefulBean/remote";

   private static MockServerController mockServerController;

   /**
    * The server host on which the MockServer will be available for requests
    */
   private static final String serverHost = "localhost";

   /**
    * The server port on which the MockServer will be available for requests
    */
   private static final int serverPort = 12345;

   private static Context context;

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that a SLSB Remote invocation succeeds
    */
   @Test
   public void testStatelessSessionRemoteInvocation() throws Throwable
   {
      Object bean = RemoteAccessTestCase.getContext().lookup(JNDI_NAME_SLSB_REMOTE);
      assertTrue("Bean was not of expected type " + MyStatelessRemote.class.getName() + " but was instead " + bean,
            bean instanceof MyStatelessRemote);

      String result = ((MyStatelessRemote) bean).sayHi("testRemote");
      String expected = "Hi testRemote";
      assertEquals("Result was not expected", expected, result);
   }

   /**
    * Ensures that more than one SLSB Remote invocations succeed
    */
   @Test
   public void testStatelessSessionDuplicateRemoteInvocations() throws Throwable
   {
      Object bean = RemoteAccessTestCase.getContext().lookup(JNDI_NAME_SLSB_REMOTE);
      assertTrue("Bean was not of expected type " + MyStatelessRemote.class.getName() + " but was instead " + bean,
            bean instanceof MyStatelessRemote);

      MyStatelessRemote slsb = ((MyStatelessRemote) bean);
      String result = slsb.sayHi("testRemote");
      String expected = "Hi testRemote";
      assertEquals("Result was not expected", expected, result);
      result = slsb.sayHi("testRemote");
      expected = "Hi testRemote";
      assertEquals("Result was not expected", expected, result);
   }

   /**
    * Ensures that a SFSB Remote invocation succeeds
    */
   @Test
   public void testStatefulSessionRemoteInvocation() throws Throwable
   {
      // Obtain the Proxy
      Object bean = RemoteAccessTestCase.getContext().lookup(JNDI_NAME_SFSB_REMOTE);
      assertTrue("Bean must be assignable to " + MyStatefulRemoteBusiness.class.getSimpleName() + " but was instead "
            + bean.getClass(), bean instanceof MyStatefulRemoteBusiness);

      // Invoke and Test Result
      int result = ((MyStatefulRemoteBusiness) bean).getNextCounter();
      assertEquals(result, 0);
   }

   /**
    * Ensures that more than one SFSB Remote invocations succeed
    */
   @Test
   public void testStatefulSessionDuplicateRemoteInvocation() throws Throwable
   {
      // Obtain the Proxy
      Object bean = RemoteAccessTestCase.getContext().lookup(JNDI_NAME_SFSB_REMOTE);
      assertTrue("Bean must be assignable to " + MyStatefulRemoteBusiness.class.getSimpleName() + " but was instead "
            + bean.getClass(), bean instanceof MyStatefulRemoteBusiness);

      // Invoke and Test Result
      MyStatefulRemoteBusiness sfsb = ((MyStatefulRemoteBusiness) bean);
      int result = sfsb.getNextCounter();
      assertEquals(result, 0);
      result = sfsb.getNextCounter();
      assertEquals(result, 1);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Starts the MockServer
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      
      // get the input stream for jndi-remote.properties file, which will be available
      // in classpath
      InputStream inputStream = RemoteAccessTestCase.class.getClassLoader().getResourceAsStream(
            "jndi-remote.properties");

      // load the jndi-remote.properties 
      Properties jndiRemoteProperties = new Properties();
      jndiRemoteProperties.load(inputStream);

      // Use the non-default constructor of InitialContext and pass the 
      // properties
      RemoteAccessTestCase.setContext(new InitialContext(jndiRemoteProperties));  
      
      // create a controller for mockserver
      mockServerController = new MockServerController(serverHost, serverPort);

      // Start Server
      long start = System.currentTimeMillis();
      mockServerController.startServer(new String[]{RemoteAccessTestCase.class.getName()});
      long end = System.currentTimeMillis();
      log.info("MockServer started in " + (end - start) + " milli sec.");

   }

   /**
    * Stops the MockServer
    * 
    * @throws Throwable
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      mockServerController.stopServer();

   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public static Context getContext()
   {
      return context;
   }

   protected static void setContext(Context context)
   {
      RemoteAccessTestCase.context = context;
   }

}
