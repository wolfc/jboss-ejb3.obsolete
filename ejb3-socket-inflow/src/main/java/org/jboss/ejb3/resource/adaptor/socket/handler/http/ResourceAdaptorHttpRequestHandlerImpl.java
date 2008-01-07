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
package org.jboss.ejb3.resource.adaptor.socket.handler.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Set;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;

import org.jboss.ejb3.resource.adaptor.socket.HttpRequestMessage;
import org.jboss.ejb3.resource.adaptor.socket.SocketResourceAdaptor;
import org.jboss.ejb3.resource.adaptor.socket.handler.RequestHandlingException;
import org.jboss.ejb3.resource.adaptor.socket.handler.SocketBasedRequestHandler;
import org.jboss.ejb3.resource.adaptor.socket.listener.SocketMessageListener;
import org.jboss.logging.Logger;

/**
 * 
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ResourceAdaptorHttpRequestHandlerImpl implements SocketBasedRequestHandler, Work
{

   // Class Members

   /*
    * Logger
    */
   private static final Logger logger = Logger.getLogger(ResourceAdaptorHttpRequestHandlerImpl.class);

   /*
    * Byte Pattern designating HTTP Request EOF
    */
   public static final byte[] HTTP_REQUEST_EOF = "\r\n\r\n".getBytes();

   // Instance Members
   
   private String requestContents;

   private SocketResourceAdaptor ra;

   // Constructor

   public ResourceAdaptorHttpRequestHandlerImpl(Socket clientSocket, SocketResourceAdaptor ra)
   {
      this.setRequestContents(this.getRequest(clientSocket));
      this.setRa(ra);
   }

   // Required Implementations

   /**
    * 
    * 
    * @param clientSocket
    * @throws RequestHandlingException
    */
   public void handleClientRequest() throws RequestHandlingException
   {
      // Initialize
      MessageEndpointFactory factory = null;
      SocketMessageListener endpoint = null;

      // Obtain all ActivationSpecs
      Set<ActivationSpec> activationSpecs = this.getRa().getMessageEndpointFactories().keySet();

      // For each ActivationSpec
      for (ActivationSpec spec : activationSpecs)
      {
         // Obtain the associated EndpointFactory
         factory = (MessageEndpointFactory) this.getRa().getMessageEndpointFactories().get(spec);

         // Create the Endpoint
         try
         {
            endpoint = (SocketMessageListener) factory.createEndpoint(null);
         }
         catch (UnavailableException e)
         {
            throw new RuntimeException(e);
         }
         
         // Send the Message to the Endpoint
         endpoint.onMessage(new HttpRequestMessage(this.getRequestContents()));

      }

   }

   public void release()
   {

   }

   public void run()
   {
      try
      {
         this.handleClientRequest();
      }
      catch (RequestHandlingException e)
      {
         logger.error(e);
         throw new RuntimeException(e);
      }
      catch (Throwable t)
      {
         logger.error(t);
      }
   }

   // Internal Helper Methods

   private String getRequest(Socket clientSocket)
   {
      // Initialize 
      StringBuffer sb = new StringBuffer();
      InputStream inStream = null;

      // Obtain Streams
      try
      {
         inStream = clientSocket.getInputStream();

         // Copy request content to response
         int bufferSize = 1024;
         byte[] buffer = new byte[bufferSize];
         int i = 0;
         while ((i = inStream.read(buffer)) != -1)
         {

            // Find HTTP EOF
            int httpEof = CopyHttpRequestToResponseRequestHandler.indexOf(buffer,
                  CopyHttpRequestToResponseRequestHandler.HTTP_REQUEST_EOF);

            // If EOF is encountered
            if (httpEof != -1)
            {
               // Print only part of buffer until EOF is reached
               int useableSize = httpEof + CopyHttpRequestToResponseRequestHandler.HTTP_REQUEST_EOF.length;
               byte[] newBuffer = new byte[useableSize];
               System.arraycopy(buffer, 0, newBuffer, 0, newBuffer.length);
               sb.append(new String(newBuffer));

               // Stop
               break;
            }
            // EOF not encountered
            else
            {
               // Print entire buffer
               sb.append(new String(buffer));
            }
         }

         // Return
         return sb.toString();
      }
      catch (IOException ioe)
      {
         throw new RuntimeException(ioe);
      }
   }

   // Accessors / Mutators

   public SocketResourceAdaptor getRa()
   {
      return ra;
   }

   public void setRa(SocketResourceAdaptor ra)
   {
      this.ra = ra;
   }

   public String getRequestContents()
   {
      return requestContents;
   }

   public void setRequestContents(String requestContents)
   {
      this.requestContents = requestContents;
   }
}