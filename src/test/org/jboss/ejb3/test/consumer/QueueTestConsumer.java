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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ActivationConfigProperty;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.Message;

import org.jboss.ejb3.annotation.Consumer;
import org.jboss.ejb3.annotation.CurrentMessage;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 67628 $
 */
@Consumer(activationConfig =
        {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
        @ActivationConfigProperty(propertyName="destination", propertyValue="queue/mdbtest"),
        @ActivationConfigProperty(propertyName="maxSession", propertyValue="1")
        })
public class QueueTestConsumer implements QueueTestRemote, QueueTestXA, QueueTestLocal
{
   @CurrentMessage Message currentMessage;

   private Message setterMessage;

   @CurrentMessage void setMessage(Message msg)
   {
      setterMessage = msg;
   }

   public void method1(String msg, int num)
   {
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
