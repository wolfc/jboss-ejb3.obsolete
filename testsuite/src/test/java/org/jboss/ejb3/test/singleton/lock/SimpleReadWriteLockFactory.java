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
package org.jboss.ejb3.test.singleton.lock;


/**
 * A SimpleReadWriteLockFactory.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class SimpleReadWriteLockFactory implements LockFactory
{
   private final Sync sync = new Sync();
   
   public Lock createLock(boolean read)
   {
      return new SimpleReadWriteLock(sync, read);
   }
   
   private class SimpleReadWriteLock implements Lock
   {
      private final Sync sync;
      private final boolean read;
      
      public SimpleReadWriteLock(Sync sync, boolean read)
      {
         this.sync = sync;
         this.read = read;
      }
      
      public void lock()
      {
         try
         {
            boolean wait;
            sync.sync();      
            if (read)
               wait = sync.isWriteInProgress();
            else
               wait = sync.hasActiveThreads();

            while (wait)
            {
               sync.releaseSync();

               //log.info(methodName + " is waiting; active threads=" + lock.activeThreads);
               synchronized (sync)
               {
                  try
                  {
                     sync.wait();
                  }
                  catch (InterruptedException e)
                  {
                  }
                  
                  sync.sync();
                  if (read)
                     wait = sync.isWriteInProgress();
                  else
                     wait = sync.hasActiveThreads();
               }
            }

            sync.increaseActiveThreads();
            if (!read)
               sync.setWriteInProgress(true);
         }
         finally
         {
            sync.releaseSync();
         }
      }

      public void unlock()
      {
         try
         {
            sync.sync();
            sync.decreaseActiveThreads();
            if (!read)
               sync.setWriteInProgress(false);
         }
         finally
         {
            sync.releaseSync();
         }
      }
   }
   
   private static class Sync
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
