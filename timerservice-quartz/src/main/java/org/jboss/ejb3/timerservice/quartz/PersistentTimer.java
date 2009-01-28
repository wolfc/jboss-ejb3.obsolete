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

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * This class contains all the info for find a persistent timer.
 *
 * @author <a href="mailto:carlo@nerdnet.nl">Carlo de Wolf</a>
 * @version $Revision$
 */
public class PersistentTimer implements Serializable, TimerHandle
{
   private static final Logger log = Logger.getLogger(PersistentTimer.class);
   
   private static final long serialVersionUID = 1L;

   //private String schedulerName;
   //private String jobName;
   //private String jobGroup;
   private String triggerName;
   private String triggerGroup;
   private String containerGuid;
   
   private Serializable info;
   
   protected PersistentTimer(Trigger trigger, String containerGuid, Serializable info)
   {
      assert trigger != null;
      assert containerGuid != null;
      
      this.triggerName = trigger.getName();
      this.triggerGroup = trigger.getGroup();
      this.info = info;
      this.containerGuid = containerGuid;
   }
   
   protected TimedObjectInvoker getTimedObjectInvoker()
   {
      // TODO: a hack to get back the container. This needs thinking.
      TimedObjectInvoker invoker = TimedObjectInvokerRegistry.getTimedObjectInvoker(containerGuid);
      assert invoker != null;
      return invoker;
   }
   
   public Timer getTimer() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {
      // TODO: check state
      try
      {
         Scheduler scheduler = QuartzTimerServiceFactory.getScheduler();
         Trigger trigger = scheduler.getTrigger(triggerName, triggerGroup);
         if(trigger == null)
            throw new NoSuchObjectLocalException("can't find trigger '" + triggerName + "' in group '" + triggerGroup + "'");
         return new TimerImpl(scheduler, trigger, info);
      }
      catch(SchedulerException e)
      {
         log.error("getTimer failed", e);
         throw new EJBException(e);
      }
   }
}
