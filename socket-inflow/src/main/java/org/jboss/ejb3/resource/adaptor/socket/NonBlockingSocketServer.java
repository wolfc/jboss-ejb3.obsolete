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
package org.jboss.ejb3.resource.adaptor.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * A Generic Non-Blocking Socket Server with pluggable
 * request handling implementation
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class NonBlockingSocketServer
{
   // Class Members

   /*
    * Logger
    */
   private static final Logger logger = Logger.getLogger(NonBlockingSocketServer.class);

   /*
    * The Default Port 
    */
   public static final int DEFAULT_BIND_PORT = 9001;

   /*
    * The Default Bind Host 
    */
   public static final InetAddress DEFAULT_BIND_HOST;
   static
   {
      try
      {
         DEFAULT_BIND_HOST = InetAddress.getLocalHost();
      }
      catch (UnknownHostException e)
      {
         throw new RuntimeException(e);
      }
   }

   // Instance Members

   private int numberOfConnections;

   private InetSocketAddress bindAddress;

   private boolean running;

   private Selector multiplexor;

   private ServerSocketChannel channel;

   // Constructors

   public NonBlockingSocketServer()
   {
      this(NonBlockingSocketServer.DEFAULT_BIND_HOST, NonBlockingSocketServer.DEFAULT_BIND_PORT);
   }

   public NonBlockingSocketServer(InetAddress bindHost, int bindPort)
   {
      // Set as not Running
      this.setRunning(false);

      // Set Bind Address
      this.setBindAddress(new InetSocketAddress(bindHost, bindPort));

      // Ensure Valid Bind Address
      if (this.getBindAddress().isUnresolved())
      {
         throw new RuntimeException("Bind Address of " + this.getBindAddress().toString() + " could not be resolved.");
      }
   }

   // Functional Methods

   /**
    * Starts the Server
    */
   public synchronized void start()
   {
      // Only Start if not Running
      if (!this.isRunning())
      {
         try
         {
            // Bind the Server and Register the MultiPlexor
            this.bindAndRegisterConnection();

            // Set as running
            this.setRunning(true);

            // Log
            logger.info("Server at " + this.getBindAddress().toString() + " Started");

            // Listen for incoming connections
            this.listenForIncomingConnections();

         }
         catch (IOException ioe)
         {
            throw new RuntimeException(ioe);
         }
      }
   }

   /**
    * Shuts down the Server
    */
   public void shutdown()
   {
      // Only Shutdown if Running
      if (this.isRunning())
      {
         // Set Flag
         this.setRunning(false);

         // Shutdown
         this.getMultiplexor().wakeup();

         // Close Channel
         try
         {
            this.getChannel().close();
         }
         // Ignore
         catch (IOException e)
         {
         }

         // Log
         logger.info("Server at " + this.getBindAddress().toString() + " Shutdown");
      }
   }

   // Internal Helper Methods

   /**
    * Binds the Server to the appropriate binding address and registers
    * the multiplexor with the underlying communications channel
    */
   private void bindAndRegisterConnection() throws IOException
   {
      // Create ServerSocketChannel
      this.setChannel(ServerSocketChannel.open());

      // Set Channel to Non-Blocking IO
      this.getChannel().configureBlocking(false);

      // Create Multiplexor
      this.setMultiplexor(this.getChannel().provider().openSelector());

      // Bind Channel to Bind Address 
      this.getChannel().socket().bind(this.getBindAddress());

      // Register the multiplexor with the channel
      this.getChannel().register(this.getMultiplexor(), SelectionKey.OP_ACCEPT);
   }

   /**
    * Listens for incoming connections and dispatches to the 
    * Client Request Handler for processing
    * 
    * @throws IOException
    */
   private void listenForIncomingConnections() throws IOException
   {
      // While the server is running
      while (this.isRunning())
      {
         // If new clients have been established
         if (this.getMultiplexor().select() > 0)
         {
            // Record and log new connection
            this.setNumberOfConnections(this.getNumberOfConnections() + 1);
            logger.debug("New Connection, Number " + this.getNumberOfConnections());

            // At least one new connection has been requested, obtain the keys
            Set<SelectionKey> selectionKeys = multiplexor.selectedKeys();

            // For all new selections/clients
            for (SelectionKey client : selectionKeys)
            {
               // Initialize client socket
               Socket clientSocket = null;

               try
               {
                  // Obtain the channel from this client
                  ServerSocketChannel clientChannel = (ServerSocketChannel) client.channel();

                  // Accept the client channel, and get a reference to its socket                  
                  clientSocket = clientChannel.accept().socket();

                  // Dispatch the client request
                  this.dispatchClientRequest(clientSocket);

               }
               finally
               {
                  // Take this client out of the selection keys so only processed once
                  selectionKeys.remove(client);

                  // Close
                  if (clientSocket != null)
                  {
                     clientSocket.close();
                  }
               }
            }
         }
      }
   }

   /**
    * 
    * 
    * @param clientSocket
    */
   protected abstract void dispatchClientRequest(Socket clientSocket);

   // Accessors / Mutators

   protected InetSocketAddress getBindAddress()
   {
      return bindAddress;
   }

   protected void setBindAddress(InetSocketAddress bindAddress)
   {
      this.bindAddress = bindAddress;
   }

   public boolean isRunning()
   {
      return running;
   }

   protected void setRunning(boolean running)
   {
      this.running = running;
   }

   protected Selector getMultiplexor()
   {
      return multiplexor;
   }

   protected void setMultiplexor(Selector multiplexor)
   {
      this.multiplexor = multiplexor;
   }

   protected ServerSocketChannel getChannel()
   {
      return channel;
   }

   protected void setChannel(ServerSocketChannel channel)
   {
      this.channel = channel;
   }

   public int getNumberOfConnections()
   {
      return numberOfConnections;
   }

   protected void setNumberOfConnections(int numberOfConnections)
   {
      this.numberOfConnections = numberOfConnections;
   }

}
