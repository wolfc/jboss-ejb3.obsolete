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
package org.jboss.ejb3.timerservice.quartz;

import java.io.Serializable;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

import org.jboss.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * A view on an actual (persistent) timer.
 * 
 * This object must never be serializable (EJB3 18.4.1)
 *
 * @author <a href="mailto:carlo@nerdnet.nl">Carlo de Wolf</a>
 * @version $Revision$
 */
public class TimerImpl implements Timer
{
   private static final Logger log = Logger.getLogger(TimerImpl.class);
   
   private Scheduler scheduler;
   private Trigger trigger;
   private Serializable info;
   
   protected TimerImpl(Scheduler scheduler, Trigger trigger, Serializable info) {
      assert scheduler != null;
      assert trigger != null;
      
      this.scheduler = scheduler;
      this.trigger = trigger;
      this.info = info;
   }
   
   protected void checkState()
   {
      // TODO: implement bean state checking to see if a call is allowed
      
      if(trigger.getNextFireTime() == null)
         throw new NoSuchObjectLocalException("timer has expired");
   }
   
   /**
    * Cause the timer and all its associated expiration notifications to be cancelled.
    * 
    * @throws   IllegalStateException       If this method is invoked while the instance is in a state that does not allow access to this method. 
    * @throws   NoSuchObjectLocalException  If invoked on a timer that has expired or has been cancelled. 
    * @throws   EJBException                If this method could not complete due to a system-level failure.
    */
   public void cancel() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      checkState();
      
      try {
         // TODO: call TimerServiceImpl.cancelTimer instead
         scheduler.unscheduleJob(trigger.getName(), trigger.getGroup());
      }
      catch(SchedulerException e) {
         log.error("cancel failed", e);
         throw new EJBException(e.getMessage());
      }
   }

   /**
    * Get the number of milliseconds that will elapse before the next scheduled timer expiration.
    * 
    * @return   The number of milliseconds that will elapse before the next scheduled timer expiration.
    * @throws   IllegalStateException       If this method is invoked while the instance is in a state that does not allow access to this method. 
    * @throws   NoSuchObjectLocalException  If invoked on a timer that has expired or has been cancelled. 
    * @throws   EJBException                If this method could not complete due to a system-level failure.
    */
   public long getTimeRemaining() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      // leave all checks to getNextTimeout
      return getNextTimeout().getTime() - System.currentTimeMillis();
   }

   /**
    * Get the point in time at which the next timer expiration is scheduled to occur.
    * 
    * @return   The point in time at which the next timer expiration is scheduled to occur.
    * @throws   IllegalStateException       If this method is invoked while the instance is in a state that does not allow access to this method. 
    * @throws   NoSuchObjectLocalException  If invoked on a timer that has expired or has been cancelled. 
    * @throws   EJBException                If this method could not complete due to a system-level failure.
    */
   public Date getNextTimeout() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      checkState();
      
      Date nextTimeout = trigger.getNextFireTime();
      if(nextTimeout == null)
         throw new IllegalStateException("trigger does not have a next fire time"); // TODO: proper EJB3 state check & exception
      return nextTimeout;
   }

   /**
    * Get the information associated with the timer at the time of creation.
    * 
    * @return   The Serializable object that was passed in at timer creation, or null if the info argument passed in at timer creation was null.
    * @throws   IllegalStateException       If this method is invoked while the instance is in a state that does not allow access to this method. 
    * @throws   NoSuchObjectLocalException  If invoked on a timer that has expired or has been cancelled. 
    * @throws   EJBException                If this method could not complete due to a system-level failure.
    */
   public Serializable getInfo() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      checkState();
      
      return info;
   }

   /**
    * Get a serializable handle to the timer. This handle can be used at a later time to re-obtain the timer reference.
    * 
    * @return   A serializable handle to the timer.
    * @throws   IllegalStateException       If this method is invoked while the instance is in a state that does not allow access to this method. 
    * @throws   NoSuchObjectLocalException  If invoked on a timer that has expired or has been cancelled. 
    * @throws   EJBException                If this method could not complete due to a system-level failure.
    */
   public TimerHandle getHandle() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      checkState();
      
      return null; // FIXME: implement getHandle
   }
}
