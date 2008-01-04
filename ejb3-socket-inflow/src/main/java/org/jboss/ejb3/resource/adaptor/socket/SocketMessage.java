package org.jboss.ejb3.resource.adaptor.socket;

import java.net.Socket;

/**
 * A SocketMessage is an incoming socket-based request from a
 * client, to be sent to the Listener for processing 
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SocketMessage
{
   // Instance Members

   Socket socket = new Socket();

   // Constructor

   public SocketMessage(Socket socket)
   {
      this.setSocket(socket);
   }

   // Accessors / Mutators

   public Socket getSocket()
   {
      return socket;
   }

   private void setSocket(Socket socket)
   {
      this.socket = socket;
   }

}
