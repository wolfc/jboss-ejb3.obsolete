/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer.bean;

import org.jboss.ejb3.mdb.DeliveryMode;
import org.jboss.ejb3.mdb.MessageProperties;
import org.jboss.ejb3.mdb.Producer;

import javax.ejb.Local;
import java.util.Map;


/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Local
@Producer(connectionFactory="java:/JmsXA")
public interface ExampleProducerXA extends ExampleProducer
{
   @MessageProperties(delivery=DeliveryMode.PERSISTENT, priority=4)
   void method2(String msg, Map<String, String> map);
}
