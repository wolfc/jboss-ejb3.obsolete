/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.mdb.bean;

import org.jboss.ejb3.mdb.ConnectionConfig;

import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven
@ConnectionConfig(destinationType = javax.jms.Queue.class, destinationJndiName = "queue/tutorial/example", durable = true, subscriptionId = "queueExample")
public class ExampleMDB implements MessageListener
{
   public void onMessage(Message recvMsg)
   {
      System.out.println("----------------");
      System.out.println("Received message");
      System.out.println("----------------");
   }

}
