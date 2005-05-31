/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer.bean;

import java.util.Map;
import org.jboss.annotation.ejb.DeliveryMode;
import org.jboss.annotation.ejb.MessageProperties;
import org.jboss.annotation.ejb.Producer;
import org.jboss.annotation.ejb.Local;


/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Local @Producer(connectionFactory="java:/JmsXA")
public interface ExampleProducerXA extends ExampleProducer
{
   @MessageProperties(delivery=DeliveryMode.PERSISTENT, priority=4)
   void method2(String msg, Map<String, String> map);
}
