/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.endpoint.deployers.test.common;

import javax.ejb.TimerService;

import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.spi.TimerServiceFactory;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MockTimerServiceFactory implements TimerServiceFactory
{
   private static final Logger log = Logger.getLogger(MockTimerServiceFactory.class);
   
   public TimerService createTimerService(TimedObjectInvoker invoker)
   {
      log.info("not creating a timer service");
      return null;
   }

   public void restoreTimerService(TimerService timerService)
   {
      log.info("ignoring timer service restore");
   }

   public void suspendTimerService(TimerService timerService)
   {
      log.info("ignoring timer service suspend");
   }
}
