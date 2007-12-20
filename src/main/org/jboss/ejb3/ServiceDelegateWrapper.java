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
package org.jboss.ejb3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.TimerService;

import org.jboss.ejb3.statistics.InvocationStatistics;

import org.jboss.system.ServiceMBeanSupport;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 64731 $
 * @deprecated service mbeans are no longer in use
 */
@Deprecated
public class ServiceDelegateWrapper extends ServiceMBeanSupport implements ServiceDelegateWrapperMBean
{
   private Object delegate;
   private Method createMethod;
   private Method startMethod;
   private Method stopMethod;
   private Method destroyMethod;


   public ServiceDelegateWrapper(Object delegate)
   {
      this.delegate = delegate;
      try
      {
         createMethod = delegate.getClass().getMethod("create");
      }
      catch (NoSuchMethodException ignored)
      {
      }
      try
      {
         startMethod = delegate.getClass().getMethod("start");
      }
      catch (NoSuchMethodException ignored)
      {
      }
      try
      {
         stopMethod = delegate.getClass().getMethod("stop");
      }
      catch (NoSuchMethodException ignored)
      {
      }
      try
      {
         destroyMethod = delegate.getClass().getMethod("destroy");
      }
      catch (NoSuchMethodException ignored)
      {
      }

   }

   @Override
   protected void createService() throws Exception
   {
      super.createService();
      try
      {
         if (createMethod != null) createMethod.invoke(delegate);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getCause();
         if (t instanceof Exception) throw (Exception)t;
         else throw new RuntimeException(t);
      }
   }

   @Override
   protected void startService() throws Exception
   {
      super.startService();
      try
      {
         if (startMethod != null) startMethod.invoke(delegate);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getCause();
         if (t instanceof Exception) throw (Exception)t;
         else throw new RuntimeException(t);
      }
   }

   @Override
   protected void stopService() throws Exception
   {
      super.stopService();
      try
      {
         if (stopMethod != null) stopMethod.invoke(delegate);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getCause();
         if (t instanceof Exception) throw (Exception)t;
         else throw new RuntimeException(t);
      }

   }

   @Override
   protected void destroyService() throws Exception
   {
      super.destroyService();
      try
      {
         if (destroyMethod != null) destroyMethod.invoke(delegate);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getCause();
         if (t instanceof Exception) throw (Exception)t;
         else throw new RuntimeException(t);
      }
   }
   
   // FIXME: this is here for EJBTHREE-630, re-establishing timers
   public TimerService getTimerService(Object pKey)
   {
      return ((Container) delegate).getTimerService(pKey);
   }
   
   public InvocationStatistics getInvokeStats()
   {
      return ((Container) delegate).getInvokeStats();
   }
}
