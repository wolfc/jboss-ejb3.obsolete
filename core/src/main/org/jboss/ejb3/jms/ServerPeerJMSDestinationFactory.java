/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.jms;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.management.ObjectName;

import org.jboss.ejb3.KernelAbstraction;
import org.jboss.ejb3.KernelAbstractionFactory;
import org.jboss.logging.Logger;

/**
 * Use ServerPeer for creation of destinations.
 * 
 * JBoss AS 5.0
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ServerPeerJMSDestinationFactory extends JMSDestinationFactory
{
   private static final Logger log = Logger.getLogger(ServerPeerJMSDestinationFactory.class);
   
   // Do not instantiate outside package
   ServerPeerJMSDestinationFactory()
   {
      
   }
   
   public void createDestination(Class<? extends Destination> type, String jndiSuffix) throws Exception
   {
      String methodName;
      String destinationContext;
      if (type == Topic.class)
      {
         destinationContext = "topic";
         methodName = "createTopic";
      }
      else if (type == Queue.class)
      {
         destinationContext = "queue";
         methodName = "createQueue";
      }
      else
      {
         // type was not a Topic or Queue, bad user
         throw new IllegalArgumentException
                 ("Expected javax.jms.Queue or javax.jms.Topic: " + type);
      }
      
      String name = jndiSuffix;
      String jndiName = destinationContext + "/" + jndiSuffix;
      
      ObjectName serverPeerName = new ObjectName("jboss.messaging:service=ServerPeer");
      
      KernelAbstraction kernel = KernelAbstractionFactory.getInstance();
      // invoke the server to create the destination
      Object result = kernel.invoke(serverPeerName,
              methodName,
              new Object[]{name, jndiName},
              new String[]{"java.lang.String", "java.lang.String"});
      
      log.debug("result = " + result);
   }
}
