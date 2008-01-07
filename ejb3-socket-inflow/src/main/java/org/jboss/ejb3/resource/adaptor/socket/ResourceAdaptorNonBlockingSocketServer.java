package org.jboss.ejb3.resource.adaptor.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.resource.spi.work.WorkException;

import org.jboss.ejb3.resource.adaptor.socket.handler.http.ResourceAdaptorHttpRequestHandlerImpl;
import org.jboss.logging.Logger;

public class ResourceAdaptorNonBlockingSocketServer extends NonBlockingSocketServer
{
   // Class Members
   /*
    * Logger
    */
   private static final Logger logger = Logger.getLogger(ResourceAdaptorNonBlockingSocketServer.class);

   // Instance Members

   private SocketResourceAdaptor ra;

   // Constructors

   public ResourceAdaptorNonBlockingSocketServer(SocketResourceAdaptor ra, InetAddress bindHost, int bindPort)
   {
      super(bindHost, bindPort);
      this.ra = ra;
   }

   public ResourceAdaptorNonBlockingSocketServer(SocketResourceAdaptor ra)
   {
      this.ra = ra;
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

   // Required Implementations

   @Override
   protected void dispatchClientRequest(Socket clientSocket)
   {
      try
      {
         this.getRa().getCtx().getWorkManager().scheduleWork(
               new ResourceAdaptorHttpRequestHandlerImpl(clientSocket, this.getRa()));

         try
         {
            clientSocket.close();
         }
         // Ignore
         catch (IOException e)
         {
            logger.warn(e);
         }
      }
      catch (WorkException e)
      {
         throw new RuntimeException(e);
      }
   }
}
