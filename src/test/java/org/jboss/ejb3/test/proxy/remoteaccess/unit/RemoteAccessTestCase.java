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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.common.thread.RedirectProcessOutputToSystemOutThread;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulRemoteBusiness;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessRemote;
import org.jboss.ejb3.test.proxy.remoteaccess.JndiPropertiesToJndiRemotePropertiesHackCl;
import org.jboss.ejb3.test.proxy.remoteaccess.MockServer;
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

   private static final String ENV_VAR_JAVAHOME = "JAVA_HOME";

   private static final String EXECUTABLE_JAVA = "bin" + File.separator + "java";

   private static final String LOCATION_BASEDIR = System.getProperty("basedir");

   private static final String LOCATION_TARGET = RemoteAccessTestCase.LOCATION_BASEDIR + File.separator + "target";

   private static final String LOCATION_TEST_CLASSES = RemoteAccessTestCase.LOCATION_TARGET + File.separator
         + "tests-classes";

   private static final String LOCATION_CLASSES = RemoteAccessTestCase.LOCATION_TARGET + File.separator + "classes";

   private static final String LOCATION_CONF = RemoteAccessTestCase.LOCATION_BASEDIR + File.separator + "conf";

   private static final String FILENAME_DEPENDENCY_CP = RemoteAccessTestCase.LOCATION_TARGET + File.separator
         + "cp.txt";

   private static final String JNDI_NAME_SLSB_LOCAL = "MyStatelessBean/local";

   private static final String JNDI_NAME_SLSB_REMOTE = "MyStatelessBean/remote";

   private static final String JNDI_NAME_SFSB_REMOTE = "MyStatefulBean/remote";

   private static Process remoteProcess;

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
      // Switch up to the hacky CL so that "jndi.properties" is not loaded, and uses instead "jndi-remote.properties"
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(new JndiPropertiesToJndiRemotePropertiesHackCl());

      RemoteAccessTestCase.setContext(new InitialContext());

      // Replace the CL
      Thread.currentThread().setContextClassLoader(oldLoader);

      // Start Server
      RemoteAccessTestCase.invokeRemoteMockServerProcess(RemoteAccessTestCase.class.getName());

      // Wait for Server to start
      Thread.sleep(5000);
   }

   /**
    * Stops the MockServer
    * 
    * @throws Throwable
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      /*
       * This is far from a graceful shutdown, but hey, this is only a test
       */
      Process p = RemoteAccessTestCase.getRemoteProcess();
      p.destroy();

   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Invokes on the MockServer, spinning up as a new Process
    * 
    * @param argument
    * @throws Throwable
    */
   protected static void invokeRemoteMockServerProcess(String argument) throws Throwable
   {
      // Get the current System Properties and Environment Variables
      String javaHome = System.getenv(RemoteAccessTestCase.ENV_VAR_JAVAHOME);
      String conf = RemoteAccessTestCase.LOCATION_CONF;
      String testClasses = RemoteAccessTestCase.LOCATION_TEST_CLASSES;
      String classes = RemoteAccessTestCase.LOCATION_CLASSES;

      // Get the contents of the dependency classpath file
      String dependencyClasspathFilename = RemoteAccessTestCase.FILENAME_DEPENDENCY_CP;
      File dependencyClasspath = new File(dependencyClasspathFilename);
      assert dependencyClasspath.exists() : "File " + dependencyClasspathFilename
            + " is required to denote the dependency CP";
      BufferedReader reader = new BufferedReader(new FileReader(dependencyClasspath));
      StringBuffer contents = new StringBuffer();
      String line = null;
      while ((line = reader.readLine()) != null)
      {
         contents.append(line);
         contents.append(System.getProperty("line.separator"));
      }
      String depCp = contents.toString().trim();

      // Build the command
      StringBuffer command = new StringBuffer();
      command.append(javaHome); // JAVA_HOME
      command.append(File.separatorChar);
      command.append(RemoteAccessTestCase.EXECUTABLE_JAVA);
      command.append(" -cp "); // Classpath

      command.append(classes);
      command.append(File.pathSeparatorChar);
      command.append(testClasses);
      command.append(File.pathSeparatorChar);
      command.append(conf);
      command.append(File.pathSeparatorChar);
      command.append(depCp); // Dependency CP
      command.append(" -ea "); // Enable Assertions
      command.append(MockServer.class.getName());
      command.append(' ');
      command.append(argument); // Argument

      // Create a Remote Launcher
      String cmd = command.toString();
      String[] cmds = cmd.split(" ");
      ProcessBuilder builder = new ProcessBuilder();
      builder.command(cmds);
      builder.redirectErrorStream(true);
      File pwd = new File(RemoteAccessTestCase.LOCATION_BASEDIR);
      assert pwd.exists() : "Present working directory for execution of remote process, " + pwd.getAbsolutePath()
            + ", could not be found.";
      log.debug("Remote Process working directory: " + pwd.getAbsolutePath());
      builder.directory(pwd);
      log.info("Launching in separate process: " + cmd);
      try
      {
         RemoteAccessTestCase.setRemoteProcess(builder.start());
         // Redirect output from the separate process
         new RedirectProcessOutputToSystemOutThread(RemoteAccessTestCase.getRemoteProcess()).start();
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Could not execute remote process", t);
      }
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public static Process getRemoteProcess()
   {
      return remoteProcess;
   }

   protected static void setRemoteProcess(Process remoteProcess)
   {
      RemoteAccessTestCase.remoteProcess = remoteProcess;
   }

   public static Context getContext()
   {
      return context;
   }

   protected static void setContext(Context context)
   {
      RemoteAccessTestCase.context = context;
   }

}
