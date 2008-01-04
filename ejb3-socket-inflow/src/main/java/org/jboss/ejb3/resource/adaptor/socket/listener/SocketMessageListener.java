package org.jboss.ejb3.resource.adaptor.socket.listener;

import org.jboss.ejb3.resource.adaptor.socket.SocketMessage;

/**
 * Listener for new Socket Messages
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface SocketMessageListener
{
   /**
    * Event fired when a new message is received
    * 
    * @param message
    */
   public void onMessage(SocketMessage message);
}
