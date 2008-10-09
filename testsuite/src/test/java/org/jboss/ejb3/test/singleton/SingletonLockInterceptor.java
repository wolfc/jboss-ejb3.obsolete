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
package org.jboss.ejb3.test.singleton;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.aop.AbstractInterceptor;
import org.jboss.logging.Logger;

/**
 * A SingletonLockInterceptor.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class SingletonLockInterceptor extends AbstractInterceptor
{
   private static final Logger log = Logger.getLogger(SingletonLockInterceptor.class);

   // container/instance lock
   private final Lock lock = new Lock();
   
   public String getName()
   {
      return "SingletonLockInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      /* way to get to the metadata and determine whether the method has READ or WRITE lock
      EJBContainer container = getEJBContainer(invocation);
      JBossEnterpriseBeanMetaData xml = container.getXml();
      if(xml != null)
         log.info("metadata is available");
         */
      
      // for now consider methods starting with "get" as having READ lock
      boolean isReadMethod = false;
      MethodInvocation mi = (MethodInvocation) invocation;
      if(mi.getMethod() != null)
         isReadMethod = mi.getMethod().getName().startsWith("get");
      
      lock.sync();
      try
      {
         boolean wait;
         if(isReadMethod)
            wait = lock.isWriteInProgress();
         else
            wait = lock.hasActiveThreads();
         
         while(wait)
         {
            synchronized (lock)
            {
               lock.releaseSync();

               try
               {
                  lock.wait();
               }
               catch (InterruptedException e)
               {
               }
            }
            
            lock.sync();
            
            if(isReadMethod)
               wait = lock.isWriteInProgress();
            else
               wait = lock.hasActiveThreads();
         }
         
         lock.increaseActiveThreads();
         if(!isReadMethod)
            lock.setWriteInProgress(true);
      }
      finally
      {
         lock.releaseSync();
      }
      
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         lock.sync();
         try
         {
            lock.decreaseActiveThreads();
            if(!isReadMethod)
               lock.setWriteInProgress(false);
         }
         finally
         {
            lock.releaseSync();
         }
      }
   }
   
   private static class Lock
   {
      private int activeThreads;

      private Thread synched;
      private int synchedDepth;
   
      private boolean writeInProgress;
      
      public void increaseActiveThreads()
      {
         ++activeThreads;
      }
      
      public void decreaseActiveThreads()
      {
         --activeThreads;
      }
      
      public boolean hasActiveThreads()
      {
         return activeThreads > 0;
      }
      
      public boolean isWriteInProgress()
      {
         return writeInProgress;
      }
      
      public void setWriteInProgress(boolean writeInProgress)
      {
         this.writeInProgress = writeInProgress;
      }
      
      /**
       * A method that checks if the calling thread has the lock, and if it
       * does not blocks until the lock is available. If there is no current owner
       * of the lock, or the calling thread already owns the lock then the
       * calling thread will immeadiately acquire the lock.
       */ 
      public void sync()
      {
         synchronized (this)
         {
            Thread thread = Thread.currentThread();
            while (synched != null && !synched.equals(thread))
            {
               try
               {
                  this.wait();
               }
               catch (InterruptedException ex)
               { /* ignore */
               }
            }

            synched = thread;

            if(synchedDepth > 0)
               throw new IllegalStateException("At the moment synchedDepth shouldn't be more than 1");
            ++synchedDepth;
         }
      }
    
      public void releaseSync()
      {
         synchronized(this)
         {
            if (--synchedDepth == 0)
               synched = null;
            this.notify();
         }
      }
   }
}
