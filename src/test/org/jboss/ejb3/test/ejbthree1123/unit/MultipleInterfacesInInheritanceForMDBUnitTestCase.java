/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1123.unit;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1123.TestMDB;
import org.jboss.ejb3.test.ejbthree1123.TestStatelessRemoteBusiness;
import org.jboss.test.JBossTestCase;

/**
 * Tests to ensure that an MDB with the many interfaces
 * defined (via inheritance only) will successfully deploy
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class MultipleInterfacesInInheritanceForMDBUnitTestCase extends JBossTestCase
{
   // Constructor
   public MultipleInterfacesInInheritanceForMDBUnitTestCase(String name)
   {
      super(name);
   }

   // Suite
   public static Test suite() throws Exception
   {
      return getDeploySetup(MultipleInterfacesInInheritanceForMDBUnitTestCase.class,
            "ejbthree1123-service.xml, ejbthree1123.jar");
   }

   // Tests 

   /**
    * Test that the MDB with multiple business interfaces (defined through inheritance)
    * successfully deploys and receives a test message
    */
   public void testMultipleInterfacesViaInheritanceForMDB() throws Exception
   {
      // Send a Message to the Queue
      Queue queue = (Queue) this.getInitialContext().lookup(TestMDB.QUEUE_NAME);
      QueueConnectionFactory factory = (QueueConnectionFactory) this.getInitialContext().lookup("ConnectionFactory");
      QueueConnection qConnection = factory.createQueueConnection();
      QueueSession session = qConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
      session.createSender(queue).send(
            session.createTextMessage("\n\nRustic Overtones of Portland, ME.  Listen to them.  \n\nL,\nALR\n"));
      session.close();
      qConnection.close();

      // Wait for processing; async task
      this.sleep(5000);

      // Ensure the message was received; denotes that the MDB deployed OK
      TestStatelessRemoteBusiness ejb = (TestStatelessRemoteBusiness) this.getInitialContext().lookup(
            TestStatelessRemoteBusiness.JNDI_NAME);
      assertEquals(true, ejb.isMessageReceived());
      
      // Clear status for next run
      ejb.clearStatus();
   }
}