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
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.logging.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * Implements the EJB3 Timer Service specification (EJB3 chapter 18).
 * 
 * Each bean container has its own job and trigger group.
 *
 * @author <a href="mailto:carlo@nerdnet.nl">Carlo de Wolf</a>
 * @version $Revision$
 */
public class TimerServiceImpl implements TimerService
{
   private static final Logger log = Logger.getLogger(TimerServiceImpl.class);
   
   private Scheduler scheduler;
   private TimedObjectInvoker invoker;
   private String groupName;
   private long jobNum = 0;
   private long triggerNum = 0;
   
   protected TimerServiceImpl(Scheduler scheduler, TimedObjectInvoker invoker) {
      assert scheduler != null;
      assert invoker != null;
      
      this.scheduler = scheduler;
      this.invoker = invoker;
      this.groupName = "ejb3";
   }
   
   protected Timer createTimer(Trigger trigger, Serializable info)
   {
      try {
         String name = "myJob" + jobNum;
         jobNum++;
         
         Class jobClass = QuartzTimerJob.class;
         
         Timer timer = new TimerImpl(scheduler, trigger, info);
         
         PersistentTimer persistentTimer = new PersistentTimer(trigger, invoker.getTimedObjectId(), info);
         
         JobDetail jobDetail = new JobDetail(name, groupName, jobClass);
         jobDetail.getJobDataMap().put("timer", persistentTimer);
         
         scheduler.scheduleJob(jobDetail, trigger);
         
         return timer;
      }
      catch(SchedulerException e) {
         // translate the exception, because the client might not have quartz
         log.error("createTimer failed", e);
         throw new EJBException(e.getMessage());
      }
      
   }
   
   /**
    * Create a single-action timer that expires after a specified duration.
    * 
    * @param    duration    The number of milliseconds that must elapse before the timer expires.
    * @param    info        Application information to be delivered along with the timer expiration notification. This can be null.
    * @return   The newly created Timer.
    * @throws   IllegalArgumentException    If duration is negative.
    * @throws   IllegalStateException       If this method is invoked while the instance is in a state that does not allow access to this method.
    * @throws   EJBException                If this method fails due to a system-level failure.
    */
   public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException,
         EJBException
   {
      if(duration < 0) throw new IllegalArgumentException("duration must not be negative");
      // TODO: check state
      
      Date expiration = new Date(System.currentTimeMillis() + duration);
      return createTimer(expiration, info);
   }

   /**
    * Create an interval timer whose first expiration occurs after a specified duration, 
    * and whose subsequent expirations occur after a specified interval.
    * 
    * @param    initialDuration     The number of milliseconds that must elapse before the first timer expiration notification.
    * @param    intervalDuration    The number of milliseconds that must elapse between timer expiration notifications. Expiration notifications are scheduled relative to the time of the first expiration. If expiration is delayed(e.g. due to the interleaving of other method calls on the bean) two or more expiration notifications may occur in close succession to "catch up".
    * @param    info                Application information to be delivered along with the timer expiration. This can be null.
    * @return   The newly created Timer.
    * @throws   IllegalArgumentException    If initialDuration is negative, or intervalDuration is negative. 
    * @throws   IllegalStateException       If this method is invoked while the instance is in a state that does not allow access to this method. 
    * @throws   EJBException                If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(long initialDuration, long intervalDuration, Serializable info)
         throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if(initialDuration < 0) throw new IllegalArgumentException("initialDuration must not be negative");
      if(intervalDuration < 0) throw new IllegalArgumentException("intervalDuration must not be negative");
      // TODO: check state
      
      Date initialExpiration = new Date(System.currentTimeMillis() + initialDuration);
      
      return createTimer(initialExpiration, intervalDuration, info);
   }

   /**
    * Create a single-action timer that expires at a given point in time.
    * 
    * @param    expiration  The point in time at which the timer must expire.
    * @param    info        Application information to be delivered along with the timer expiration notification. This can be null. 
    * @return   The newly created Timer. 
    * @throws   IllegalArgumentException    If expiration is null, or expiration.getTime() is negative. 
    * @throws   IllegalStateException       If this method is invoked while the instance is in a state that does not allow access to this method. 
    * @throws   EJBException                If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException,
         EJBException
   {
      if(expiration == null) throw new IllegalArgumentException("expiration must not be null");
      if(expiration.getTime() < 0) throw new IllegalArgumentException("expiration.time must not be negative");
      // TODO: check state
      
      String triggerName = "myTrigger" + triggerNum;
      triggerNum++;
      
      Trigger trigger = new SimpleTrigger(triggerName, groupName, expiration);
      
      return createTimer(trigger, info);
   }

   /**
    * Create an interval timer whose first expiration occurs at a given point in time and whose subsequent expirations occur after a specified interval.
    * 
    * @param    initialExpiration   The point in time at which the first timer expiration must occur.
    * @param    intervalDuration    The number of milliseconds that must elapse between timer expiration notifications. Expiration notifications are scheduled relative to the time of the first expiration. If expiration is delayed(e.g. due to the interleaving of other method calls on the bean) two or more expiration notifications may occur in close succession to "catch up".
    * @param    info                Application information to be delivered along with the timer expiration notification. This can be null. 
    * @return   The newly created Timer. 
    * @throws   IllegalArgumentException    If initialExpiration is null, or initialExpiration.getTime() is negative, or intervalDuration is negative. 
    * @throws   IllegalStateException       If this method is invoked while the instance is in a state that does not allow access to this method. 
    * @throws   EJBException                If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info)
         throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if(initialExpiration == null) throw new IllegalArgumentException("initialExpiration must not be null");
      if(initialExpiration.getTime() < 0) throw new IllegalArgumentException("initialExpiration.time must not be negative");
      if(intervalDuration < 0) throw new IllegalArgumentException("intervalDuration must not be negative");
      // TODO: check state
      
      String triggerName = "myTrigger" + triggerNum;
      triggerNum++;
      Date endTime = null;
      
      Trigger trigger = new SimpleTrigger(triggerName, groupName, initialExpiration, endTime, SimpleTrigger.REPEAT_INDEFINITELY, intervalDuration);
      
      return createTimer(trigger, info);
   }

   protected Scheduler getScheduler()
   {
      return scheduler;
   }

   protected TimedObjectInvoker getTimedObjectInvoker()
   {
      return invoker;
   }
   
   /**
    * Get all the active timers associated with this bean.
    * 
    * @return   A collection of javax.ejb.Timer objects.
    * @throws   IllegalStateException   If this method is invoked while the instance is in a state that does not allow access to this method.
    * @throws   EJBException            If this method could not complete due to a system-level failure.
    */
   public Collection getTimers() throws IllegalStateException, EJBException
   {
      throw new RuntimeException("NYI");
   }

   protected void shutdown()
   {
      log.debug("shutting down " + this);
      try
      {
         String triggerNames[] = scheduler.getTriggerNames(groupName);
         for(String triggerName : triggerNames)
            scheduler.unscheduleJob(triggerName, groupName);
         String jobNames[] = scheduler.getJobNames(groupName);
         for(String jobName : jobNames)
            scheduler.deleteJob(jobName, groupName);
      }
      catch(SchedulerException e)
      {
         log.error("shutdown failed", e);
         // TODO: ignore?
      }
   }
   
   public String toString()
   {
      return "Timer Service " + invoker;
   }
}
