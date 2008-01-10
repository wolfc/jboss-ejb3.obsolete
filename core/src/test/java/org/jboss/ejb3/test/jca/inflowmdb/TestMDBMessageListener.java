/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.test.jca.inflowmdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.ejb3.test.jca.inflow.TestMessage;
import org.jboss.ejb3.test.jca.inflow.TestMessageListener;
import org.jboss.logging.Logger;

/**
 * 
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@MessageDriven(name = "TestMDB", activationConfig =
{
@ActivationConfigProperty(propertyName="name", propertyValue="testInflow"),
@ActivationConfigProperty(propertyName="anInt", propertyValue="5"),
@ActivationConfigProperty(propertyName="anInteger", propertyValue="55"),
@ActivationConfigProperty(propertyName="localhost", propertyValue="127.0.0.1"),
@ActivationConfigProperty(propertyName="props", propertyValue="key1=value1,key2=value2,key3=value3")
})
@ResourceAdapter("jcainflow.rar")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TestMDBMessageListener implements TestMessageListener
{
   private static final Logger log = Logger.getLogger(TestMDBMessageListener.class);

   public void deliverMessage(TestMessage message)
   {
      message.acknowledge();
      log.info(message.toString());
   }
}
