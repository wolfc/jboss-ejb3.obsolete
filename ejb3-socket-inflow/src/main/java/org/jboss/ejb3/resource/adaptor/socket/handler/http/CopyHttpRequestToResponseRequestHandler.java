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
import java.io.PrintStream;
import java.net.Socket;

import org.jboss.ejb3.resource.adaptor.socket.handler.RequestHandlingException;
import org.jboss.ejb3.resource.adaptor.socket.handler.SocketBasedRequestHandler;

/**
 * Request Handler implementation to read in the client request and 
 * echo it as a response.  Mostly used in testing without practical application.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class CopyHttpRequestToResponseRequestHandler implements SocketBasedRequestHandler
{

   // Class Members
   /*
    * Byte Pattern designating HTTP Request EOF
    */
   public static final byte[] HTTP_REQUEST_EOF = "\r\n\r\n".getBytes();

   // Required Implementations

   /**
    * Reads in the client request and mirrors the content in the response
    * 
    * @param clientSocket
    * @throws RequestHandlingException
    */
   public void handleClientRequest(Socket clientSocket) throws RequestHandlingException
   {
      // Initialize Streams
      PrintStream outStream = null;
      InputStream inStream = null;

      // Obtain Streams
      try
      {
         outStream = new PrintStream(clientSocket.getOutputStream());
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
               outStream.print(new String(newBuffer));

               // Stop
               break;
            }
            // EOF not encountered
            else
            {
               // Print entire buffer
               outStream.print(new String(buffer));
            }
         }

         // Flush Outstream
         outStream.flush();
      }
      catch (IOException ioe)
      {
         throw new RuntimeException(ioe);
      }
   }

   /**
    * Returns the index of the specified pattern to find in the specified byte array, 
    * or -1 if not found 
    * 
    * @param arrayToSearch
    * @param patternToFind
    * @return
    */
   //TODO Should be centralized to I/O Utility
   private static int indexOf(byte[] arrayToSearch, byte[] patternToFind)
   {
      String toSearch = new String(arrayToSearch);
      String toFind = new String(patternToFind);
      return toSearch.indexOf(toFind);
   }
}