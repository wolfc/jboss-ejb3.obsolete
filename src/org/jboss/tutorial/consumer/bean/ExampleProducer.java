/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer.bean;

import org.jboss.ejb3.mdb.MessageProperties;
import org.jboss.ejb3.mdb.DeliveryMode;

import java.util.Map;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public interface ExampleProducer
{
   void method1(String msg, int val);

   @MessageProperties(delivery=DeliveryMode.NON_PERSISTENT)
   void method2(String msg, Map<String, String> map);
}
