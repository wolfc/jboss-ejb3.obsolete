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
package org.jboss.ejb3.test.consumer;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.Message;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.ejb3.Container;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class DeploymentDescriptorQueueTestConsumer implements DeploymentDescriptorQueueTestRemote, DeploymentDescriptorQueueTestXA, DeploymentDescriptorQueueTestLocal
{
   private static final Logger log = Logger.getLogger(DeploymentDescriptorQueueTestConsumer.class);
   
   Message currentMessage;

   private Message setterMessage;
   
   StatelessRemote stateless;
   StatelessLocal statelessLocal;
   DataSource testDatasource;
   
   public void setStatelessLocal(StatelessLocal statelessLocal)
   {
      this.statelessLocal = statelessLocal;
   }

   void setMessage(Message msg)
   {
      setterMessage = msg;
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
   }
   
   public void method1(String msg, int num) throws Exception
   {
      testInjections();
      
      TestStatusBean.queueRan = "method1";
      TestStatusBean.fieldMessage = currentMessage != null;
      TestStatusBean.setterMessage = setterMessage != null;

      System.out.println("method1(" + msg + ", " + num + ")");
   }

   public void method2(String msg, float num)
   {
      TestStatusBean.queueRan = "method2";

      TestStatusBean.fieldMessage = currentMessage != null;
      TestStatusBean.setterMessage = setterMessage != null;

      System.out.println("method2(" + msg + ", " + num + ")");
   }

   @AroundInvoke
   public Object intercept(InvocationContext ctx) throws Exception
   {
      System.out.println("**** intercepted ****" + ctx.getMethod().getName());
      TestStatusBean.interceptedQueue = ctx.getMethod().getName();
      return ctx.proceed();
   }

   @PostConstruct
   public void postConstruct()
   {
      TestStatusBean.postConstruct = true;
   }

   @PreDestroy
   public void preDestroy()
   {
      TestStatusBean.preDestroy = true;
   }
}
