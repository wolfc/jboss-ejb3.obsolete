/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer.bean;

import org.jboss.ejb3.mdb.Producer;

import javax.ejb.Local;


/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Producer
@Local
public interface ExampleProducerLocal extends ExampleProducer
{

}
