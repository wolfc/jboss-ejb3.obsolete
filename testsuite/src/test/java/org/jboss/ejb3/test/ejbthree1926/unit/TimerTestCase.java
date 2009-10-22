/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree1926.unit;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1926.SimpleTimer;
import org.jboss.ejb3.test.ejbthree1926.ResultTracker.Result;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * TimerTestCase
 * 
 * Testcase for EJBTHREE-1926 https://jira.jboss.org/jira/browse/EJBTHREE-1926
 * The bug was caused by restoring the timers too early during the EJB3 container
 * startup (in the stateless/service container lockedStart()).
 * 
 * The fix involved restoring the timers after the containers have started
 * and are ready to accept invocations.
 * 
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class TimerTestCase extends JBossTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(TimerTestCase.class);

   private static final String JAR_NAME = "ejbthree1926.jar";

   /**
    * @param name
    */
   public TimerTestCase(String name)
   {
      super(name);
   }

   /**
    * 
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(TimerTestCase.class, JAR_NAME);
   }

   /**
    * Tests that the timer, for a SLSB, scheduled for future, is invoked after
    * the StatelessContainer is started completely.
    *  
    * @throws Exception
    */
   public void testTimerRestoreForSLSB() throws Exception
   {
      logger.debug("Testing timer restore on SLSB");
      doTest("TimerSLSBean/remote");
   }

   /**
    * Tests that the timer scheduled, for Service Bean, for future, is invoked after
    * the ServiceContainer is started completely.
    *  
    * @throws Exception
    */
   public void testTimerRestoreForServiceBean() throws Exception
   {
      logger.debug("Testing timer restore on @Service Bean");
      doTest("TimerServiceBean/remote");
   }

   /**
    * Utility method for testing the timers
    * 
    * @param jndiName The jndi name of the bean to lookup
    * @throws Exception
    */
   private void doTest(String jndiName) throws Exception
   {
      // first get hold of the bean and create a timer
      Context ctx = new InitialContext();
      SimpleTimer timerBean = (SimpleTimer) ctx.lookup(jndiName);
      // schedule a (future) timer to fire after 1 sec 
      // and then immidiately undeploy the EJB application, so that
      // the timer is triggered next time the EJB application is deployed
      timerBean.scheduleAfter(1000);
      logger.info("Undeploying " + JAR_NAME);
      this.undeploy(JAR_NAME);
      // wait for a few seconds so that the timer is triggered
      // during container start
      Thread.sleep(3000);
      // now deploy back the same EJB application
      logger.info("deploying again " + JAR_NAME);
      this.deploy("ejbthree1926.jar");

      // lookup the bean and check the result 
      timerBean = (SimpleTimer) ctx.lookup(jndiName);
      assertEquals("Timer failed to invoke the TimeOut method on bean with jndi-name: " + jndiName, Result.SUCCESS,
            timerBean.getResult());
   }
}
