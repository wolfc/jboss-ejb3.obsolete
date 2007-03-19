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
package org.jboss.ejb3.timerservice.jboss;

import javax.ejb.TimerService;
import javax.management.ObjectName;

import org.jboss.ejb.txtimer.EJBTimerService;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.timerservice.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.TimerServiceFactory;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Factory to create timer services which use the JBoss EJB Timer Service.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class JBossTimerServiceFactory extends TimerServiceFactory
{
   private static Logger log = Logger.getLogger(JBossTimerServiceFactory.class);
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.timerservice.TimerServiceFactory#createTimerService(javax.management.ObjectName, org.jboss.ejb3.timerservice.TimedObjectInvoker)
    */
   @Override
   public TimerService createTimerService(ObjectName containerId, TimedObjectInvoker invoker)
   {
      TimerService timerService = null;
      try
      {
         EJBTimerService service = getEJBTimerService();
         TimerService delegate = service.createTimerService(containerId, null, invoker);
         timerService = new TimerServiceFacade(containerId, delegate);
      }
      catch (Exception e)
      {
         //throw new EJBException("Could not create timer service", e);
         if (log.isTraceEnabled())
         {
            log.trace("Unable to initialize timer service", e);
         }
         else
         {
            log.trace("Unable to initialize timer service");
         }
      }
      return timerService;
   }

   protected EJBTimerService getEJBTimerService()
   {
      return (EJBTimerService) MBeanProxyExt.create(EJBTimerService.class, EJBTimerService.OBJECT_NAME, MBeanServerLocator.locateJBoss());
   }
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.timerservice.TimerServiceFactory#removeTimerService(javax.ejb.TimerService)
    */
   @Override
   public void removeTimerService(TimerService timerService)
   {
      removeTimerService(((TimerServiceFacade) timerService).getContainerId());
   }

   protected void removeTimerService(ObjectName containerId)
   {
      try
      {
         EJBTimerService service = getEJBTimerService();
         service.removeTimerService(containerId, true);
      }
      catch (Exception e)
      {
         //throw new EJBException("Could not remove timer service", e);
         if (log.isTraceEnabled())
         {
            log.trace("Unable to initialize timer service", e);
         }
         else
         {
            log.trace("Unable to initialize timer service");
         }
      }
   }
   
   public void restoreTimerService(TimerService aTimerService)
   {
      if (aTimerService == null)
      {
         log.warn("TIMER SERVICE IS NOT INSTALLED");
         return;
      }
      TimerServiceFacade timerService = (TimerServiceFacade) aTimerService;
      EJBContainer container = timerService.getContainer();
      // FIXME: do not assume that a TimedObjectInvoker is an EJBContainer
      ClassLoader loader = container.getClassloader();
      
      getEJBTimerService().restoreTimers(timerService.getContainerId(), loader);
   }
}
