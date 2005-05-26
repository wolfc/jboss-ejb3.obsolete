/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer.bean;

import org.jboss.ejb3.mdb.Producer;
import org.jboss.ejb3.mdb.Local;


/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Local @Producer
public interface ExampleProducerLocal extends ExampleProducer
{

}
