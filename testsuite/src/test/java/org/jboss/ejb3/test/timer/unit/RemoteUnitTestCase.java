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
package org.jboss.ejb3.test.timer.unit;

import java.util.Date;

import junit.framework.Test;

import org.jboss.ejb3.test.timer.TimerTester;
import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class RemoteUnitTestCase
extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public RemoteUnitTestCase(String name)
   {

      super(name);

   }

   public void testNewTransaction() throws Exception
   {
      TimerTester test = (TimerTester) getInitialContext().lookup("TransactionalTimerTesterBean/remote");
      test.startTimer(5000);
      test.accessTimer();
      Thread.sleep(6000);
      assertTrue(test.isTimerCalled());
   }
   
   // EJBTHREE-630
   public void testPersistence() throws Exception
   {
      TimerTester test = (TimerTester) getInitialContext().lookup("TimerTesterService/remote");
      long when = System.currentTimeMillis() + 5000;
      test.setTimer(new Date(when));
      
      redeploy("timer-test.jar");
      
      test = (TimerTester) getInitialContext().lookup("TimerTesterService/remote");
      long wait = 1000 + (when - System.currentTimeMillis());
      if(wait > 0)
         Thread.sleep(wait);
      assertTrue(test.isTimerCalled());
   }
   
   public void testService() throws Exception
   {
      TimerTester test = (TimerTester) getInitialContext().lookup("TimerTesterService/remote");
      test.startTimer(5000);
      test.accessTimer();
      Thread.sleep(6000);
      assertTrue(test.isTimerCalled());
      test.startTimerViaEJBContext(5000);
      test.accessTimer();
      Thread.sleep(6000);
      assertTrue(test.isTimerCalled());
   }
   
   public void testSimple() throws Exception
   {
      TimerTester test = (TimerTester) this.getInitialContext().lookup("TimerTesterBean/remote");
      test.startTimer(5000);
      test.accessTimer();
      Thread.sleep(6000);
      assertTrue(test.isTimerCalled());
      test.startTimerViaEJBContext(5000);
      test.accessTimer();
      Thread.sleep(6000);
      assertTrue(test.isTimerCalled());
   }
   
   public void testSimple21() throws Exception
   {
      TimerTester test = (TimerTester) this.getInitialContext().lookup("TimerTesterBean21/remote");
      test.startTimer(5000);
      test.accessTimer();
      Thread.sleep(6000);
      assertTrue(test.isTimerCalled());
      test.startTimerViaEJBContext(5000);
      test.accessTimer();
      Thread.sleep(6000);
      assertTrue(test.isTimerCalled());
   }

   public void testRollback() throws Exception
   {
      TimerTester test = (TimerTester) this.getInitialContext().lookup("TimerTesterBean/remote");
      test.startTimerAndRollback(5000);
      Thread.sleep(6000);
      assertFalse(test.isTimerCalled());
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(RemoteUnitTestCase.class, "timer-test.jar");
   }

}
