/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer_deployment_descriptor.bean;

import org.jboss.annotation.ejb.MessageProperties;
import org.jboss.annotation.ejb.DeliveryMode;
import org.jboss.annotation.ejb.DeliveryMode;
import org.jboss.annotation.ejb.MessageProperties;

import java.util.Map;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public interface ExampleProducer
{
   void method1(String msg, int val);

   void method2(String msg, Map<String, String> map);
}
