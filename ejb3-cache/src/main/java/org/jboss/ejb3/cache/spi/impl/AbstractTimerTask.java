/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ejb3.cache.spi.impl;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Abstract superclass of various TimerTask implementations.
 * 
 * @author Brian Stansberry
 */
public abstract class AbstractTimerTask 
   extends TimerTask
{
   private boolean stopped = true;
   private Timer timer;
   private String timerName;
   private long interval;
   
   /**
    * Create a new PassivationExpirationRunner.
    * 
    */
   protected AbstractTimerTask(String timerName, long interval)
   {      
      setTimerName(timerName);
      setInterval(interval);
   }
   
   protected AbstractTimerTask() {}
   
   public boolean isStopped()
   {
      return stopped;
   }

   public Timer getTimer()
   {
      return timer;
   }

   public String getTimerName()
   {
      return timerName;
   }
   
   public void setTimerName(String name)
   {
      assert name != null : "name is null";
      this.timerName = name;
   }

   /**
    * Gets the interval, in milliseconds, with which this task should
    * be {@link Timer#schedule(TimerTask, long) scheduled with the timer}.
    * 
    * @return the interval
    */
   public long getInterval()
   {
      return interval;
   }

   /**
    * Sets the interval, in milliseconds, with which this task should
    * be {@link Timer#schedule(TimerTask, long) scheduled with the timer}.
    * 
    * @param interval the interval
    */
   public void setInterval(long interval)
   {
      assert interval > 0 : "interval is < 1";
      this.interval = interval;
   }

   public void start()
   {
      if (stopped)
      {
         timer = new Timer(getTimerName(), true);
         stopped = false;
         long period = getInterval() * 1000;
         timer.schedule(this, period, period);
      }
   }
   
   public void stop()
   {
      stopped = true;
      cancel();
      timer.cancel();
   }

}
