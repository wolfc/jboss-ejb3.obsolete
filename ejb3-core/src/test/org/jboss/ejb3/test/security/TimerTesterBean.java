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
package org.jboss.ejb3.test.security;

import java.util.Date;

import javax.naming.InitialContext;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

import org.jboss.security.SecurityAssociation;

import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(TimerTester.class)
@RemoteBinding(jndiBinding="TimerTester")
@SecurityDomain("timer-runas-test")
public class TimerTesterBean implements TimerTester
{
   private static final Logger log = Logger.getLogger(TimerTesterBean.class);
   
   public static boolean timerCalled = false;

   private @Resource TimerService timerService;

   private @Resource SessionContext ctx;
   
   private Timer timer;

   public void startTimer(long pPeriod)
   {
      timerCalled = false;
      System.out.println("************ startTimer");
      timer = timerService.createTimer(new Date(new Date().getTime() + pPeriod), "TimerSLSBean");
      
   }
   
   public boolean isTimerCalled()
   {
      return timerCalled;
   }

   @Timeout
   public void timeoutHandler(Timer timer)
   {
      log.info("EJB TIMEOUT!!!!");
      log.info("CallerPrincipal: "+ctx.getCallerPrincipal()+"."); 
      log.info("PrincipalFromSecurityAssociation: "+SecurityAssociation.getPrincipal()+"."); 
      log.info("CallerPricipalFromSecurityAssociation: "+SecurityAssociation.getCallerPrincipal()+"."); 

      try 
      {
         UncheckedStateless tester =
            (UncheckedStateless) new InitialContext().lookup("UncheckedStatelessBean/remote");
         tester.unchecked();
         timerCalled = true;
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
