/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer.bean;

import org.jboss.ejb3.mdb.Consumer;

import javax.ejb.ActivationConfigProperty;
import java.util.Map;

@Consumer(activateConfig = {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
        @ActivationConfigProperty(propertyName="destination", propertyValue="queue/tutorial/example")
})
public class ExampleConsumerBean implements ExampleProducerRemote, ExampleProducerLocal, ExampleProducerXA
{
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
