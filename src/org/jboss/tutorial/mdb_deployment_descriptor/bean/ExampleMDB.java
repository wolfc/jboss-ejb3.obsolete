/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.mdb_deployment_descriptor.bean;

import javax.ejb.MessageDriven;
import javax.ejb.ActivationConfigProperty;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ExampleMDB implements MessageListener
{
   public void onMessage(Message recvMsg)
   {
      System.out.println("----------------");
      System.out.println("Received message");
      System.out.println("----------------");
   }

}
