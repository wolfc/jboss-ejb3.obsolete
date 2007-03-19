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
package org.jboss.ejb3.test.dd.mdb;

import javax.annotation.Resource;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.EJBException;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.test.dd.mdb.StatelessLocal;
import org.jboss.ejb3.test.dd.mdb.StatelessRemote;

/**
 * MessageBeanImpl.java
 *
 *
 * Created: Sat Nov 25 18:07:50 2000
 *
 * @author 
 * @version
 */

public class QueueBean implements MessageDrivenBean, MessageListener
{
   private static final Logger log = Logger.getLogger(QueueBean.class);

   private MessageDrivenContext ctx = null;
   
   StatelessRemote stateless;
   StatelessLocal statelessLocal;
   DataSource testDatasource;

   public QueueBean()
   {

   }

   @Resource
   public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException
   {
      this.ctx = ctx;
   }

   public void ejbCreate()
   {
   }

   public void ejbRemove()
   {
      ctx = null;
   }

   public void onMessage(Message message)
   {
      log.debug("DEBUG: QueueBean got message" + message.toString());
      try
      {
         testInjections();
         TestStatusBean.addDestination(message.getJMSDestination());
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void setStatelessLocal(StatelessLocal statelessLocal)
   {
      this.statelessLocal = statelessLocal;
   }
   
   private void testInjections() throws Exception
   {
      stateless.test();
      statelessLocal.testLocal();
      testDatasource.getConnection();
      
      Context initCtx = new InitialContext();
      Context myEnv = (Context) initCtx.lookup(Container.ENC_CTX_NAME + "/env");
      Object obj = myEnv.lookup("res/aQueue");
      if ((obj instanceof javax.jms.Queue) == false)
         throw new NamingException("res/aQueue is not a javax.jms.Queue");
      
      obj = myEnv.lookup("testDatasource");
      if ((obj instanceof javax.sql.DataSource) == false)
         throw new NamingException("testDatasource is not a javax.sql.DataSource");
   }
} // MessageBeanImpl

