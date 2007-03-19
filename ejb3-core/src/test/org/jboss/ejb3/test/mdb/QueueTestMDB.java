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
package org.jboss.ejb3.test.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.EJB;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.InitialContext;

import javax.annotation.security.RunAs;

import org.jboss.annotation.security.SecurityDomain;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@MessageDriven(activationConfig =
        {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
        @ActivationConfigProperty(propertyName="destination", propertyValue="queue/mdbtest")
        })
@RunAs("TestRole")
@SecurityDomain("other")
public class QueueTestMDB implements MessageListener
{
   private static final Logger log = Logger.getLogger(QueueTestMDB.class);
   
   @EJB(beanName="LocalStateless")
   Stateless localStateless;
   
   public int count = 0;

   @Resource MessageDrivenContext ctx;
   
   public void onMessage(Message recvMsg)
   {
      if (ctx == null) throw new RuntimeException("FAILED ON CTX LOOKUP");
      ++count;
      ++TestStatusBean.queueRan;
      TestStatusBean.messageCount = count;
      
      System.out.println("+++ QueueTestMDB onMessage " + TestStatusBean.queueRan + " " + count + " " + this);
      
      testInjections();

      try {
         InitialContext jndiContext = new InitialContext();
         Stateless stateless = (Stateless)jndiContext.lookup("Stateless");
         stateless.setState("Set");
      } catch (Exception e)
      {
         e.printStackTrace();
      }
      System.out.println("--- QueueTestMDB onMessage " + TestStatusBean.queueRan + " " + count + " " + this);

   }
   
   protected void testInjections()
   {
      localStateless.setState("testing");
   }

   @AroundInvoke
   public Object intercept(InvocationContext ctx) throws Exception
   {
      TestStatusBean.interceptedQueue = true;
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
