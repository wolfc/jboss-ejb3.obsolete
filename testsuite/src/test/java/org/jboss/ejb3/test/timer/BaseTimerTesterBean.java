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
package org.jboss.ejb3.test.timer;

import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

/**
 * This class forms the base of both an EJB3 timer tester bean and
 * an EJB 2.1 timer tester bean.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:carlo@nerdnet.nl">Carlo de Wolf</a>
 * @version $Revision$
 */
public abstract class BaseTimerTesterBean
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(BaseTimerTesterBean.class);
   
   protected static boolean timerCalled = false;

   private @Resource TimerService timerService;

   protected @Resource SessionContext ctx;
   
   // TODO: fix this state in the stateless bean
   private static Timer timer;

   @TransactionAttribute(TransactionAttributeType.MANDATORY)
   public void checkMandatoryTransaction()
   {
      
   }
   
   private void reset()
   {
      timerCalled = false;
      timer = null;
   }
   
   public void setTimer(Date expiration)
   {
      reset();
      System.out.println("************ set timer " + expiration);
      timer = timerService.createTimer(expiration, "TimerSLSBean");
   }
   
   public void startTimer(long pPeriod)
   {
      reset();
      System.out.println("************ startTimer");
      timer = timerService.createTimer(new Date(new Date().getTime() + pPeriod), "TimerSLSBean");
   }

   public void startTimerAndRollback(long pPeriod)
   {
      reset();
      System.out.println("************ startTimerAndRollback");
      timer = ctx.getTimerService().createTimer(pPeriod, "TimerSLSBean");
      ctx.setRollbackOnly();
   }

   public void startTimerViaEJBContext(long pPeriod)
   {
      reset();
      System.out.println("************ startTimerViaEJBContext");
      timer = ctx.getTimerService().createTimer(new Date(new Date().getTime() + pPeriod), "TimerSLSBean");
   }

   public void accessTimer()
   {
      //Access timer to make sure we have pushed the AllowedOperationsAssociation
      timer.getTimeRemaining();
      timer.getHandle();
      timer.getInfo();
   }
   
   public boolean isTimerCalled()
   {
      return timerCalled;
   }
}
