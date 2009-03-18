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
package org.jboss.ejb3.testremote.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.ejb3.common.thread.RedirectProcessOutputToSystemOutThread;
import org.jboss.ejb3.testremote.server.MockServer.MockServerRequest;
import org.jboss.logging.Logger;
import org.jboss.remoting.CannotConnectException;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;

/**
 * MockServerController
 * 
 * Controls the startup/shutdown of the {@link MockServer} <br/>
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MockServerController
{

   /**
    * Instance of logger
    */
   private static Logger logger = Logger.getLogger(MockServerController.class);

   /**
    * JAVA_HOME env variable
    */
   private static final String ENV_VAR_JAVAHOME = "JAVA_HOME";

   /**
    * Java executable
    */
   private static final String EXECUTABLE_JAVA = "bin" + File.separator + "java";

   /**
    * The basedir
    */
   private static final String LOCATION_BASEDIR = System.getProperty("basedir");

   private static final String LOCATION_TARGET = MockServerController.LOCATION_BASEDIR + File.separator + "target";

   private static final String LOCATION_TEST_CLASSES = MockServerController.LOCATION_TARGET + File.separator
         + "tests-classes";

   private static final String LOCATION_CLASSES = MockServerController.LOCATION_TARGET + File.separator + "classes";

   private static final String LOCATION_CONF = MockServerController.LOCATION_BASEDIR + File.separator + "conf";

   private static final String FILENAME_DEPENDENCY_CP = MockServerController.LOCATION_TARGET + File.separator
         + "cp.txt";

   /**
    * Timeout in milli sec. for server startup. Defaults to 2 minutes.
    */
   private int serverStartupTimeout = 120000;

   /**
    * Timeout in milli sec. for the server to shutdown. Defaults to 2 minutes.
    */
   private int serverStopTimeout = 120000;

   /**
    * The port number on which the {@link MockServer}
    * is available for requests
    */
   private int port;

   /**
    * The host on which the {@link MockServer}
    * is available for requests
    */
   private String serverHost;

   /**
    * Remote process in which the {@link MockServer} will run
    */
   private Process remoteProcess;

   /**
    * {@link Client} for sending requests to the {@link MockServer}
    */
   private Client mockServerClient;

   /**
    * Constructor <br>
    * Creates a {@link Client} to send requests to the remote {@link MockServer}
    *   
    * @param host The host on which the {@link MockServer} is available
    * @param port The port on which the {@link MockServer} is listening
    */
   public MockServerController(String host, int port)
   {
      this.serverHost = host;
      this.port = port;
      String uri = null;
      try
      {
         uri = "socket://" + this.serverHost + ":" + this.port;
         InvokerLocator invokerLocator = new InvokerLocator(uri);
         this.mockServerClient = new Client(invokerLocator);

      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create server controller: ", e);
      }

   }

   /**
    * Creates a remote process (JVM) to launch the {@link MockServer}
    * and then sends a {@link MockServerRequest#START} request to start the
    * server
    *   
    * @param serverClass The class of the server to be used
    * @param arguments The arguments that will be passed to the {@link MockServer} 
    *       as JVM program arguments
    *       
    * @throws Throwable
    */
   public void startServer(Class<? extends MockServer> serverClass, String[] arguments) throws Throwable
   {
      // Along with the arguments that the client passes to the server,
      // append the the serverHost and port number on which the mockserver is
      // expected to listen
      int numberOfArgs = arguments.length;
      String[] processArgs = new String[numberOfArgs + 3];
      System.arraycopy(arguments, 0, processArgs, 3, numberOfArgs);
      // now prepend the server host and port
      processArgs[0] = serverClass.getName();
      processArgs[1] = this.serverHost;
      processArgs[2] = String.valueOf(this.port);

      createRemoteProcess(serverClass, processArgs);

      /* 
       * Wait a max of 5 seconds for the remote process (remember, not the server)
       * to start
       */
      long start = System.currentTimeMillis();
      int timeoutIntervalSeconds = 5;
      long timeout = timeoutIntervalSeconds * 1000 + start;
      boolean started = false;
      while (!started)
      {
         try
         {
            // Start the server
            sendStartRequestToServer();
            // the server's started now
            started = true;
         }
         // Couldn't start server
         catch (CannotConnectException cce)
         {
            // If we haven't yet timed out
            long current = System.currentTimeMillis();
            if (current < timeout)
            {
               // Swallow exception and try again
               logger.trace("Can't connect to server @ " + new Date(current) + ", trying until " + new Date(timeout));
               Thread.sleep(100);
               continue;
            }
            logger.error("The remote process was not up, even after 5 seconds. Aborting");
            // Throw the exception, timeout's past
            throw cce;
         }
      }

   }

   /**
    * Sends a {@link MockServerRequest#STOP} request to the server
    * and also kills the process in whic the server was running
    * 
    * @see MockServerController#stopServer(boolean)
    * @throws Throwable
    */
   public void stopServer() throws Throwable
   {
      stopServer(true);
      logger.debug("Stopped the server and killed the remote process");

   }

   /**
    * Sends a {@link MockServerRequest#STOP} request to the server 
    * and if the <code>killProcess</code> is true then it also kills
    * the process in which the server was running.
    * 
    * @param killProcess If true, kills the process in which the {@link MockServer}
    *           was running. Else, just sends a {@link MockServerRequest#STOP} request
    *           to the server.
    * @throws Throwable
    */
   public void stopServer(boolean killProcess) throws Throwable
   {
      // If there is nothing to stop, then return
      if (this.remoteProcess == null)
      {
         logger.info("No remote process to stop. Returning");
         return;
      }
      try
      {
         sendStopRequestToServer();
         logger.debug("Stopped server");
         // disconnect the client
         this.mockServerClient.disconnect();

      }
      finally
      {
         if (killProcess)
         {
            // destroy the remote process
            this.remoteProcess.destroy();
            logger.debug("Remote process killed");
         }
      }
   }

   /**
    * Sets the timeout for the server startup.
    * <br/>
    * This method has to be called before calling the {@link #startServer(String[])} method,
    * for the timeout to be considered.
    * 
    * @param timeout The timeout value in milli seconds.
    */
   public void setServerStartupTimeout(int timeout)
   {
      this.serverStartupTimeout = timeout;
   }

   /**
    * Returns the timeout set for the server startup.
    * 
    * @return
    */
   public int getServerStartupTimeout()
   {
      return this.serverStartupTimeout;
   }

   /**
    * Returns the timeout set for server to stop
    * 
    * @return
    */
   public int getServerStopTimeout()
   {
      return this.serverStopTimeout;
   }

   /**
    * Sets the timeout for the server to stop.
    * <br/>
    * This method has to be called before calling the {@link #stopServer()} method,
    * for the timeout to be considered.
    * 
    * @param timeout The timeout value in milli seconds.
    */
   public void setServerStopTimeout(int timeout)
   {
      this.serverStopTimeout = timeout;
   }

   /**
    * Sends a {@link MockServerRequest#STOP} to the server
    * 
    * @throws Throwable
    */
   protected void sendStopRequestToServer() throws Throwable
   {
      this.mockServerClient.connect();
      // set a timeout - The client will wait for this amount of time 
      // for the mockserver to shutdown
      Map<String, String> configParams = new HashMap<String, String>();
      configParams.put("timeout", String.valueOf(this.serverStopTimeout));
      Object serverStatus = this.mockServerClient.invoke(MockServerRequest.STOP, configParams);
      logger.debug("Stop request returned Status = " + serverStatus);

   }

   /**
    * Sends a {@link MockServerRequest#START} to the server
    * @throws Throwable
    */
   protected void sendStartRequestToServer() throws Throwable
   {
      this.mockServerClient.connect();
      // set a timeout - The client will wait for this amount of time 
      // for the mockserver to shutdown
      Map<String, String> configParams = new HashMap<String, String>();
      configParams.put("timeout", String.valueOf(this.serverStartupTimeout));
      Object serverStatus = this.mockServerClient.invoke(MockServerRequest.START, configParams);
      logger.info("Server started. Status = " + serverStatus);

   }

   /**
    * Creates a new JVM process in which the {@link MockServer} will be active
    * 
    * @param serverClass The class of the Server to start
    * @param arguments The arguments to the passed to the {@link MockServer}
    * 
    * @throws Throwable
    */
   private void createRemoteProcess(Class<? extends MockServer> serverClass, String arguments[]) throws Throwable
   {
      // Get the current System Properties and Environment Variables
      String javaHome = System.getenv(MockServerController.ENV_VAR_JAVAHOME);
      String conf = MockServerController.LOCATION_CONF;
      String testClasses = MockServerController.LOCATION_TEST_CLASSES;
      String classes = MockServerController.LOCATION_CLASSES;

      // Get the contents of the dependency classpath file
      String dependencyClasspathFilename = MockServerController.FILENAME_DEPENDENCY_CP;
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
      List<String> command = new ArrayList<String>();

      // Executable (Java)
      StringBuffer executable = new StringBuffer();
      executable.append(javaHome); // JAVA_HOME
      executable.append(File.separatorChar);
      executable.append(MockServerController.EXECUTABLE_JAVA);

      command.add(executable.toString());

      // Classpath
      command.add("-classpath");

      StringBuffer classPath = new StringBuffer();
      classPath.append(classes);
      classPath.append(File.pathSeparator);
      classPath.append(testClasses);
      classPath.append(File.pathSeparatorChar);
      classPath.append(conf);
      classPath.append(File.pathSeparatorChar);
      classPath.append(depCp); // Dependency CP

      command.add(classPath.toString());
      // Other params to the JVM
      command.add("-ea");
      // The class to run
      command.add(serverClass.getName());
      // The arguments to the main class
      for (int i = 0; i < arguments.length; i++)
      {
         command.add(arguments[i]);
      }

      // Create a Remote Launcher
      ProcessBuilder builder = new ProcessBuilder();
      builder.command(command);
      builder.redirectErrorStream(true);
      File pwd = new File(MockServerController.LOCATION_BASEDIR);
      assert pwd.exists() : "Present working directory for execution of remote process, " + pwd.getAbsolutePath()
            + ", could not be found.";
      logger.debug("Remote Process working directory: " + pwd.getAbsolutePath());
      builder.directory(pwd);
      logger.info("Launching in separate process: " + getPrintableCommand(command));
      try
      {
         this.remoteProcess = builder.start();
         logger.info("Remote process = " + this.remoteProcess);
         // Redirect output from the separate process
         new RedirectProcessOutputToSystemOutThread(this.remoteProcess).start();

      }
      catch (Throwable t)
      {
         throw new RuntimeException("Could not execute remote process", t);
      }

   }

   /**
    * Utility method to generate a printable string from a List.
    * The elements of the list will be separated by a space (unlike the
    * default comma separated List.toString()).
    * 
    * @param command The command
    * @return Returns a printable form of the <code>command</code>
    */
   private String getPrintableCommand(List<String> command)
   {
      StringBuffer printableCmd = new StringBuffer();
      for (String cmd : command)
      {
         printableCmd.append(cmd);
         printableCmd.append(" ");
      }
      return printableCmd.toString();
   }

}
