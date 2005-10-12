/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer_deployment_descriptor.bean;

import java.util.Map;
import javax.ejb.ActivationConfigProperty;
import javax.jms.Message;
import org.jboss.annotation.ejb.Consumer;
import org.jboss.annotation.ejb.CurrentMessage;

public class ExampleConsumerBean implements ExampleProducerRemote, ExampleProducerLocal, ExampleProducerXA
{
   // you can have container inject the current JMS message so that you can manipulate it, like for instance
   // to get a reply-to destination.
   private Message currentMessage;

   public void method1(String msg, int val)
   {
      System.out.println("method1(" + msg + ", " + val + ")");
   }
   public void method2(String msg, Map<String, String> map)
   {
      System.out.println("method2: " + msg);
      for (String key : map.keySet())
      {
         System.out.println("method2 key/val: " + key + ":" + map.get(key));
      }
   }

}
