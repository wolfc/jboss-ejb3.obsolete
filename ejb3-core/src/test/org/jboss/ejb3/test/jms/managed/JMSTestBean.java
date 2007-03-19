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
package org.jboss.ejb3.test.jms.managed;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import javax.ejb.Remove;
import javax.ejb.Init;

import org.jboss.ejb3.Container;
import org.jboss.logging.Logger;

public class JMSTestBean
   implements JMSTest
{
   
   private static Logger log = Logger.getLogger(JMSTestBean.class);
   
   private Connection connection;
   
   private Queue queue;
   
   private SessionContext ctx;
   
   public void setSessionContext(SessionContext sc)
   {
      ctx = sc;
   }
   
   @Init
   public void ejbCreate()
   {
      try
      {
         InitialContext iniCtx = new InitialContext();
 
         ConnectionFactory cf =
            (ConnectionFactory) iniCtx.lookup(Container.ENC_CTX_NAME + "/env/jms/MyConnectionFactory"); 
         
         connection = cf.createConnection();
         connection.start();
         queue = (Queue)iniCtx.lookup(Container.ENC_CTX_NAME + "/env/jms/MyQueue");
      }
      catch (Throwable t)
      {           
         log.error(t);
      }
   }
   
   
   public void test1() throws EJBException
   {
      Session session = null;
      
      try
      {
         session = 
            connection.createSession(true, 0);
         
         MessageProducer producer = session.createProducer(queue);
         
         TextMessage message = session.createTextMessage();
         
         message.setText("Testing 123");
         
         producer.send(message);
      }
      catch (JMSException e)
      {           
         log.error(e);
         ctx.setRollbackOnly();
         throw new EJBException(e.toString());
      }
      finally
      {
         try
         {
            if (session != null) session.close();
         }
         catch (JMSException e)
         {            
         }         
      }
   }
   
   @Remove
   public void remove()
   {
      
      try
      {
         if (connection != null)
            connection.close();
      } 
      catch (Exception e)
      {
         e.printStackTrace();
      }
      
   }
}
