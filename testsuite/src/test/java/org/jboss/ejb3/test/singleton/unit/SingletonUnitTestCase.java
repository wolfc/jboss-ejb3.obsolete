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
   
   public void testWriteLock() throws Exception
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
                  remote.testWriteLock((long) (1000*Math.random()));
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
}
