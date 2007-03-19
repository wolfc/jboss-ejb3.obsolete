/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.tutorial.jca.inflow.swiftmq.bean;

import org.jboss.annotation.ejb.ResourceAdapter;

import javax.ejb.*;
import javax.jms.Message;
import javax.jms.MessageListener;


@MessageDriven(name="testmdb", activationConfig =
{
@ActivationConfigProperty(propertyName="messagingType", propertyValue="javax.jms.MessageListener"),
@ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
@ActivationConfigProperty(propertyName="Destination", propertyValue="testqueue"),
@ActivationConfigProperty(propertyName="ConnectionFactoryName", propertyValue="ConnectionFactory"),
@ActivationConfigProperty(propertyName="Transacted", propertyValue="true"),
@ActivationConfigProperty(propertyName="Xa", propertyValue="true"),
@ActivationConfigProperty(propertyName="DeliveryOption", propertyValue="B"),
@ActivationConfigProperty(propertyName="SubscriptionDurability", propertyValue="Durable"),
@ActivationConfigProperty(propertyName="MaxPoolSize", propertyValue="20"),
@ActivationConfigProperty(propertyName="MaxMessages", propertyValue="1"),
})
@ResourceAdapter("swiftmq.rar")
public class AnnotatedTestMDBBean
   implements MessageListener
{
   public void onMessage(Message message)
   {
      System.out.println(message);
   }
}
