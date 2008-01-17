package org.jboss.ejb3.resource.adaptor.socket;

import java.net.InetAddress;
import java.net.Socket;

import org.jboss.ejb3.resource.adaptor.socket.handler.RequestHandlingException;
import org.jboss.ejb3.resource.adaptor.socket.handler.http.CopyHttpRequestToResponseRequestHandler;

public class TestNonBlockingSocketServer extends NonBlockingSocketServer
{

   public TestNonBlockingSocketServer(InetAddress bindHost, int bindPort,
         CopyHttpRequestToResponseRequestHandler handler)
   {
      super(bindHost, bindPort);
   }

   public TestNonBlockingSocketServer()
   {
      super();
   }

   @Override
   protected void dispatchClientRequest(Socket clientSocket)
   {
      try
      {
         CopyHttpRequestToResponseRequestHandler handler = new CopyHttpRequestToResponseRequestHandler(clientSocket);
         handler.handleClientRequest();
      }
      catch (RequestHandlingException e)
      {
         throw new RuntimeException(e);
      };

   }
}
