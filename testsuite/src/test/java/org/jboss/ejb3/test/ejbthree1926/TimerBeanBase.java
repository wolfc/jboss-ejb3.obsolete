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
package org.jboss.ejb3.test.ejbthree1926;

import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.ejb3.test.ejbthree1926.ResultTracker.Result;
import org.jboss.logging.Logger;

/**
 * TimerBeanBase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class TimerBeanBase implements SimpleTimer
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(TimerBeanBase.class);

   @Resource
   private TimerService timerService;

   /**
    * @see org.jboss.ejb3.test.ejbthree1926.SimpleTimer#scheduleAfter(long)
    */
   public void scheduleAfter(long delay)
   {
      Date scheduledTime = new Date(new Date().getTime() + delay);
      Timer timer = this.timerService.createTimer(scheduledTime, "This is a timer which was scheduled to fire at "
            + scheduledTime);
      logger.info("Timer  " + timer + " scheduled to fire once at " + timer.getNextTimeout());

   }

   @Timeout
   public void timeout(Timer timer)
   {
      logger.info("Received timeout at " + new Date(System.currentTimeMillis()) + " from timer " + timer
            + " with info: " + timer.getInfo());
      // track this successful invocation
      ResultTracker.getInstance().setSuccess();
   }

   /**
    * @see org.jboss.ejb3.test.ejbthree1926.SimpleTimer#getResult()
    */
   public Result getResult()
   {
      return ResultTracker.getInstance().getResult();
   }

}
