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
package org.jboss.ejb3.test.singleton.unit;


import org.jboss.ejb3.test.singleton.SingletonRemote;
import org.jboss.test.JBossTestCase;

import junit.framework.Test;


/**
 * A SingletonUnitTestCase.
 * 
 * TODO:
 * - A Singleton session bean must not implement the javax.ejb.SessionSynchronizationinterface or use the session synchronization annotations.
 * - @Startup annotation
 * - lifecycle
 * - interceptor lifecycle and concurrency
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class SingletonUnitTestCase extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      return getDeploySetup(SingletonUnitTestCase.class, "singletonpool.jar");
   }

   public SingletonUnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      deploy("singleton-test.jar");
   }

   @Override
   public void tearDown() throws Exception
   {
      super.tearDown();
      undeploy("singleton-test.jar");
   }
   
   /**
    * This method demonstrates that once one thread is entered an instance method
    * no other thread can enter any method of the same instance in case of write concurrency.
    */
   public void testWriteConcurrency() throws Exception
   {
      final SingletonRemote remote = (SingletonRemote) getInitialContext().lookup("SingletonBean/remote");

      final Throwable[] sawFailedThread = new Throwable[1];
      final int[] finishedCount = new int[1];
      final Thread[] threads = new Thread[10];
      for(int i = 0; i < threads.length; ++i)
      {
         threads[i] = new Thread(new Runnable()
         {
            public void run()
            {
               try
               {
                  remote.writeLock((long) (1000*Math.random()));
               }
               catch(Throwable t)
               {
                  t.printStackTrace();
                  sawFailedThread[0] = t;
               }
               finally
               {
                  synchronized (finishedCount)
                  {
                     ++finishedCount[0];
                     if (finishedCount[0] == threads.length)
                        finishedCount.notify();
                  }
               }
            }
         });
      }
      
      for(Thread t : threads)
      {
         t.start();
      }
      
      synchronized(finishedCount)
      {
         while(finishedCount[0] < threads.length)
         {
            try
            {
               finishedCount.wait();
            }
            catch(InterruptedException e)
            {
            }
         }
      }

      assertNull("One of the threads failed: " + sawFailedThread[0], sawFailedThread[0]);
      assertEquals(1, remote.getInstanceCount());
   }

   /**
    * This method demonstrates that two threads can be active in the same session bean instance in case of read concurrency.
    */
   public void testReadConcurrency() throws Throwable
   {
      final SingletonRemote remote = (SingletonRemote) getInitialContext().lookup("SingletonBean/remote");

      final Thread[] threads = new Thread[5];
      final int[] results = new int[threads.length];
      final Throwable error[] = new Throwable[1];
      final int[] finishedThreads = new int[1];
      for(int i = 0; i < threads.length; ++i)
      {
         final int threadIndex = i;
         threads[threadIndex] = new Thread(new Runnable()
         {
            public void run()
            {
               try
               {
                  results[threadIndex] = remote.getReadLock(threads.length, 1000);
               }
               catch(Throwable t)
               {
                  log.error(t);
                  error[0] = t;
               }
               finally
               {
                  synchronized (finishedThreads)
                  {
                     ++finishedThreads[0];
                     finishedThreads.notify();
                  }
               }
            }
         });
      }

      for(int i = 0; i < threads.length; ++i)
         threads[i].start();

      synchronized(finishedThreads)
      {
         while(finishedThreads[0] < threads.length)
         {
            try
            {
               finishedThreads.wait();
            }
            catch(InterruptedException e)
            {
            }
         }
      }

      if(error[0] != null)
         throw error[0];
      
      for(int i = 0; i < threads.length; ++i)
         assertEquals(threads.length, results[i]);

      assertEquals(1, remote.getInstanceCount());
   }
}
