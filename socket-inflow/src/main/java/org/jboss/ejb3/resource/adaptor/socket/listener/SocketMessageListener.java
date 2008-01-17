package org.jboss.ejb3.resource.adaptor.socket.listener;

import org.jboss.ejb3.resource.adaptor.socket.HttpRequestMessage;

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
   public void onMessage(HttpRequestMessage message);
}
