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

import java.lang.reflect.Constructor;

import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.transport.Connector;

/**
 * MockServer
 * 
 * Launches a new MC Bootstrap, EJB Containers, and performs
 * all initialization to mock a remote server environment
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MockServer
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(MockServer.class);

   /**
    * Various possible server status
    */
   public enum MockServerStatus {
      STARTED, STOPPED
   }

   /**
    * 
    * Various possible server requests
    */
   public enum MockServerRequest {
      START, STOP
   }

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private EmbeddedTestMcBootstrap bootstrap;

   /**
    * Accept requests from client using this {@link Connector} 
    */
   private Connector remoteConnector;

   /**
    * The current state of the server 
    */
   private MockServerStatus currentStatus = MockServerStatus.STOPPED;

   /**
    * The Test Class using this launcher
    */
   private Class<?> testClass;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Required no-arg Constructor 
    */
   public MockServer()
   {

   }

   // --------------------------------------------------------------------------------||
   // Main ---------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Runtime Entry Point
    * 
    * @param args
    */
   public static void main(String... args) throws Throwable
   {

      // Log out the arguments
      if (log.isDebugEnabled())
      {
         StringBuffer argList = new StringBuffer();
         for (String arg : args)
         {
            argList.append(arg);
            argList.append(' ');
         }
         log.debug("Arguments: " + argList);
      }

      // Assert test class passed in
      assert args.length == 4 : "Parameters requried (in this order): <Fully qualified test case name> <serverFQClassName> <serverBindAddress> <serverPort>";

      // Get Test Class
      String testClassname = args[0];
      Class<?> testClass = null;
      try
      {
         testClass = Class.forName(testClassname);
      }
      catch (ClassNotFoundException cnfe)
      {
         throw new RuntimeException("Specified Test Class, \"" + testClassname + "\" could not be found", cnfe);
      }

      // Get bind information
      String bindHost = args[args.length - 2];
      int bindPort = Integer.parseInt(args[args.length - 1]);

      // Get Mock Server implementation 
      String mockServerClassName = args[1];

      // Create a new Launcher
      // the serverBindAddress and the port are always the last two arguments
      log.debug("Creating a MockServer for testclass = " + testClass + " and serverBindAddr = " + args[args.length - 2]
            + " and port = " + args[args.length - 1] + " using " + MockServer.class.getSimpleName()
            + " implementation: " + mockServerClassName);

      // Get the mock server class 
      Class<?> mockServerClass = null;
      try
      {
         mockServerClass = Class.forName(mockServerClassName, true, Thread.currentThread().getContextClassLoader());
      }
      catch (ClassNotFoundException e1)
      {
         throw new RuntimeException("Cannot create " + MockServer.class.getSimpleName() + " with implementation of "
               + mockServerClassName, e1);
      }
      assert MockServer.class.isAssignableFrom(mockServerClass) : "Specified implementation " + mockServerClassName
            + " is not of type " + MockServer.class.getName();
      Constructor<?> serverCtor = null;
      try
      {
         serverCtor = mockServerClass.getConstructor();
      }
      catch (NoSuchMethodException e1)
      {
         throw new RuntimeException("MockServer implementation " + mockServerClassName
               + " must have a public no-arg ctor", e1);
      }

      // Create launcher
      MockServer launcher = (MockServer) serverCtor.newInstance();

      // Set Test Class
      launcher.setTestClass(testClass);

      try
      {
         // Ready to receive (start/stop) requests
         String bindUri = "socket://" + bindHost + ":" + bindPort;
         InvokerLocator invokerLocator = new InvokerLocator(bindUri);
         launcher.remoteConnector = new Connector(invokerLocator);
         launcher.remoteConnector.create();
         launcher.remoteConnector.addInvocationHandler("EJB3Test", new MockServerInvocationHandler(launcher));
         launcher.acceptRequests();
      }
      catch (Throwable e)
      {
         log.error("Exception in MockServer while wating for requests ", e);
         throw new RuntimeException("Exception while waiting for requests ", e);
      }

   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Initializes the instance by starting up an MC Bootstrap, 
    * deploying relevant *-beans.xml, creating and installing EJB Containers
    */
   protected void initialize() throws Throwable
   {
      // Create and set a new MC Bootstrap 
      this.setBootstrap(EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap());

      // Add a Shutdown Hook
      //Runtime.getRuntime().addShutdownHook(new ShutdownHook());

      // Bind the Ejb3Registrar
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));
   }

   /**
    * Starts the server <br>
    * 
    * @throws IllegalStateException If the server is not in {@link MockServerStatus.STOPPED}
    *           state 
    * @throws Throwable
    */
   public void start() throws Throwable
   {
      // Server will be started only if current state is STOPPED
      if (!this.currentStatus.equals(MockServerStatus.STOPPED))
      {
         throw new IllegalStateException("Cannot start MockServer when its in " + getStatus() + " state");
      }
      initialize();
      this.currentStatus = MockServerStatus.STARTED;
      log.info("MockServer started");
   }

   /**
    * Stops the server <br>
    * 
    * @throws IllegalStateException If the server is not in {@link MockServerStatus.STARTED} 
    *           state
    */
   public void stop()
   {
      // Server will be stopped only if current state is STARTED
      if (!this.currentStatus.equals(MockServerStatus.STARTED))
      {
         throw new IllegalStateException("Cannot stop MockServer when its in " + getStatus() + " state");
      }
      this.bootstrap.shutdown();
      this.currentStatus = MockServerStatus.STOPPED;
      log.info("MockServer stopped");

      // Note: Do not stop the Connector which is waiting for clients to 
      // connect. Letting the Connector remain in waiting state will allow
      // clients to restart this MockServer by sending the MockServerRequest.START
      // request again.
   }

   /**
    * 
    * @return Returns the current status of the server
    */
   public MockServerStatus getStatus()
   {
      return this.currentStatus;
   }

   /**
    * Start accepting requests <br>
    * This is a blocking call and will wait for clients to connect
    * 
    * @see {@link Connector#start()}
    * @throws Throwable
    */
   protected void acceptRequests() throws Throwable
   {
      this.remoteConnector.start();
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public EmbeddedTestMcBootstrap getBootstrap()
   {
      return this.bootstrap;
   }

   public void setBootstrap(EmbeddedTestMcBootstrap bootstrap)
   {
      this.bootstrap = bootstrap;
   }

   public Class<?> getTestClass()
   {
      return testClass;
   }

   public void setTestClass(Class<?> testClass)
   {
      this.testClass = testClass;
   }

}
