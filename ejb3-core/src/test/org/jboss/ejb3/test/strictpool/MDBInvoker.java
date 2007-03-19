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
package org.jboss.ejb3.test.strictpool;

import javax.jms.QueueSession;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.TextMessage;
import javax.jms.QueueReceiver;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import EDU.oswego.cs.dl.util.concurrent.CountDown;
import org.jboss.logging.Logger;

/**
 * Adapted from the EJB 2.1 tests (org.jboss.test.cts.test.SessionInvoker)
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class MDBInvoker extends Thread
{
   QueueSession session;
   Queue queueA;
   Queue queueB;
   int id;
   CountDown done;
   public Exception runEx;

   public MDBInvoker(QueueSession session, Queue queueA, Queue queueB, int id, CountDown done)
   {
      super("MDBInvoker#"+id);
      this.session = session;
      this.queueA = queueA;
      this.queueB = queueB;
      this.id = id;
      this.done = done;
   }
   public void run()
   {
      System.out.println("Begin run, this="+this);
      try
      {
         QueueSender sender = session.createSender(queueA);
         TextMessage message = session.createTextMessage();
         message.setText(this.toString());
         sender.send(message);
         QueueReceiver receiver = session.createReceiver(queueB);
         Message reply = receiver.receive(20000);
         if( reply == null )
         {
            runEx = new IllegalStateException("Message receive timeout");
         }
         else
         {
            Message tm = (Message) reply;
            System.out.println(tm);
         }
         sender.close();
         receiver.close();
      }
      catch(Exception e)
      {
         runEx = e;
      }
      done.release();
      System.out.println("End run, this="+this);
   }

}
