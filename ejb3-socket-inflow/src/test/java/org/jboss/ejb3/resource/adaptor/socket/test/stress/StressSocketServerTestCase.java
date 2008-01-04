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
package org.jboss.ejb3.resource.adaptor.socket.test.stress;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.jboss.ejb3.resource.adaptor.socket.NonBlockingSocketServer;
import org.jboss.ejb3.resource.adaptor.socket.handler.http.CopyHttpRequestToResponseRequestHandler;
import org.junit.Test;

/**
 * Test Cases for Stressing the Generic POJO Socket Server
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StressSocketServerTestCase extends TestCase
{
   // Instance Members

   /*
    * The Server Thread
    */
   private ServerThread serverThread = null;

   /*
    * A collection of pending requests requiring servicing 
    */
   private static final Collection<String> requestsPending = Collections
         .synchronizedCollection(new ArrayList<String>());

   // Test Methods
   @Test
   public void testStress50ConcurrentRequests()
   {
      this.runStressTest(50);
   }

   @Test
   public void testStress200ConcurrentRequests()
   {
      this.runStressTest(200);
   }

   @Test
   public void testStress500ConcurrentRequests()
   {
      this.runStressTest(500);
   }

   @Test
   public void testStress1000ConcurrentRequests()
   {
      this.runStressTest(1000);
   }

   @Test
   public void testStress1500ConcurrentRequests()
   {
      this.runStressTest(1500);
   }

   // Overridden Implementations
   /**
    * Setup
    */
   @Override
   protected void setUp() throws Exception
   {
      // Call super implementation
      super.setUp();

      // Create a new Server
      if (this.serverThread == null)
      {
         this.serverThread = new ServerThread();
      }

      // Start the ServerThread
      this.serverThread.start();

      // Block until Server is running
      while (this.serverThread.getServer() == null || !this.serverThread.getServer().isRunning())
      {
         Thread.sleep(10);
      }

      // Log Started
      System.out.println("Server Started");
   }

   /**
    * Tear Down
    */
   @Override
   protected void tearDown() throws Exception
   {
      // Call super Implementation
      super.tearDown();

      // Shutdown the Server
      this.serverThread.shutdown();

      // Block until Server Thread is dead
      while (this.serverThread.isAlive())
      {

      }

      // Log Shutdown
      System.out.println("Server Shutdown\n");
   }

   // Inner Classes

   /**
    * Creates a new Client HTTP Request in its own Thread of execution
    * 
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private static class ClientThread extends Thread implements Runnable
   {
      // Instance Members
      String testString;

      // Constructor
      public ClientThread(String testString)
      {
         this.testString = testString;
         this.setName("Client Thread (Request " + testString + "");
      }

      // Overridden Implementations
      @Override
      public void run()
      {
         // Call Super Implementation
         super.run();

         // Run the Test
         try
         {
            this.runTest();
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }

      public void runTest() throws IOException
      {
         // Create new Socket
         Socket socket = new Socket(NonBlockingSocketServer.DEFAULT_BIND_HOST,
               NonBlockingSocketServer.DEFAULT_BIND_PORT);

         // Create a Writer for the request
         PrintWriter writer = new PrintWriter(socket.getOutputStream());

         // Obtain a Reader for the response
         InputStreamReader reader = new InputStreamReader(socket.getInputStream());

         // Write the request to the Server
         writer.print(testString + new String(CopyHttpRequestToResponseRequestHandler.HTTP_REQUEST_EOF));
         writer.flush();
         //System.out.println("SENT: " + testString);

         // Read in the response
         char[] buffer = new char[1024];
         int i = 0;
         StringBuffer sb = new StringBuffer();
         while ((i = reader.read(buffer)) != -1)
         {
            sb.append(buffer);
         }

         // Parse the response
         String responseStringReceived = sb.toString().trim();

         // Remove this from the pending requests
         StressSocketServerTestCase.requestsPending.remove(responseStringReceived);

         // Log Response
         //System.out.println("RECEIVED: " + responseStringReceived);

         // Close the Client Socket
         socket.close();
      }
   }

   /**
    * Starts the Server in a separate process
    * 
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private static class ServerThread extends Thread implements Runnable
   {
      // Instance Members

      private NonBlockingSocketServer server;

      // Constructor

      public ServerThread()
      {
         this.setName("Server");
      }

      // Overridden Implementations

      @Override
      public void run()
      {
         // Call super implementation
         super.run();
         // Create new Server
         this.setServer(new NonBlockingSocketServer(new CopyHttpRequestToResponseRequestHandler()));
         // Start Server
         this.getServer().start();
      }

      // Functional Methods
      public void shutdown()
      {         
         // Delegate to Server to shutdown
         this.getServer().shutdown();

         // Block until shutdown
         while (this.getServer().isRunning())
         {
            
         }

         // Stop Thread
         this.interrupt();
      }

      // Accessors / Mutators
      private NonBlockingSocketServer getServer()
      {
         return server;
      }

      private void setServer(NonBlockingSocketServer server)
      {
         this.server = server;
      }
   }

   // Internal Helper Methods

   private void runStressTest(int numberOfRequests)
   {
      // Log
      System.out.println("Starting Stress Test for " + numberOfRequests + " Concurrent Requests...");
      // Initialize
      Collection<ClientThread> clients = new ArrayList<ClientThread>();
      // For each of the requests to make
      for (int i = 0; i < numberOfRequests; i++)
      {
         // Create a unique request String
         String requestString = "TEST " + i;
         // Add to the list of requests
         StressSocketServerTestCase.requestsPending.add(requestString);
         // Make a new Client for this request
         clients.add(new ClientThread(requestString));
      }
      // Record start time
      long startTime = System.currentTimeMillis();
      // Run all clients
      for (ClientThread client : clients)
      {
         client.start();
      }
      // Block until all requests are serviced and received
      while (StressSocketServerTestCase.requestsPending.size() > 0)
      {
      }

      // Record end time
      long endTime = System.currentTimeMillis();

      // Calculate total time
      float totalTime = endTime - startTime;
      float seconds = (float) totalTime / (float) 1000;

      // Log
      System.out.println(numberOfRequests + " Requests Serviced in " + totalTime + "ms (" + seconds + " seconds)");
      System.out.println(totalTime + "ms  / " + numberOfRequests + " Requests = " + (totalTime / numberOfRequests)
            + "ms Average Per Request");

      // Shutdown all clients
      for (ClientThread client : clients)
      {
         client.interrupt();
      }
   }
}
