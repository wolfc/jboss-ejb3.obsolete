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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.ObjectName;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class TimerServiceFacade implements TimerService
{
   private TimerService delegate;
   
   private Container container;
   
   protected TimerServiceFacade(Container container, TimerService delegate)
   {
      this.container = container;
      this.delegate = delegate;
   }

   public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      return delegate.createTimer(initialExpiration, intervalDuration, info);
   }

   public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      return delegate.createTimer(expiration, info);
   }

   public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      return delegate.createTimer(initialDuration, intervalDuration, info);
   }

   public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      return delegate.createTimer(duration, info);
   }

   protected EJBContainer getContainer()
   {
      return (EJBContainer) container;
   }
   
   protected ObjectName getContainerId()
   {
      return container.getObjectName();
   }
   
   public Collection getTimers() throws IllegalStateException, EJBException
   {
      return delegate.getTimers();
   }
}
