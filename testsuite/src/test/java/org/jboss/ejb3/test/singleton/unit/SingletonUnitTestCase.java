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
                  results[threadIndex] = remote.getValue(threads.length, 1000);
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

   /**
    * This method demonstrates that an invocation of a method with write concurrency
    * can't proceed until all the currently in progress methods with read concurrency are done.
    */
   public void testWriteThreadWaitingOnReadThreadsToComplete() throws Throwable
   {
      final SingletonRemote singleton = (SingletonRemote) getInitialContext().lookup("SingletonBean/remote");

      // set the initial value to 0 
      SetValueThread setter = new SetValueThread(singleton, 0);
      setter.start();
      setter.waitOnTheResult();      
      assertEquals("setValue", singleton.getLastReturnedValueMethod());

      // assert current value is 0
      GetValueThread getter = new GetValueThread(singleton);
      getter.start();
      getter.waitOnTheResult();
      getter.assertResult(0);
      assertEquals("getValue", singleton.getLastReturnedValueMethod());

      // this getter will block on the server until the value reaches 6
      GetValueThread getter6 = new GetValueThread(singleton, 6);
      getter6.start();

      // this getter will block on the server until the value reaches 5
      GetValueThread getter5 = new GetValueThread(singleton, 5);
      getter5.start();

      // this getter will block on the server until the value reaches 4
      GetValueThread getter4 = new GetValueThread(singleton, 4);
      getter4.start();

      // current value should now be 3
      // this getter will make it 4 and resume getter4
      getter = new GetValueThread(singleton, 4);
      assertFalse(getter6.isDone());
      assertFalse(getter5.isDone());
      assertFalse(getter4.isDone());
      getter.start();
      
      getter.waitOnTheResult();
      assertTrue(getter.isDone());
      getter.assertResult(4);
      
      getter4.waitOnTheResult();
      assertTrue(getter4.isDone());
      getter4.assertResult(4);
      
      // at this point getter5 and getter6 are blocked
      // invoke setter (which would block until the getters are done) and set the value to 1 again
      setter = new SetValueThread(singleton, 1);
      assertFalse(getter6.isDone());
      assertFalse(getter5.isDone());
      setter.start();
      
      // invoke getter again which would unblock getter5
      getter = new GetValueThread(singleton, 5);
      assertFalse(getter6.isDone());
      assertFalse(getter5.isDone());
      assertFalse(setter.isDone());
      getter.start();

      getter.waitOnTheResult();
      getter.assertResult(5);
      getter5.waitOnTheResult();
      getter5.assertResult(5);
      
      // at this point getter5 is blocked and blocking the setter
      // invoke getter again and unblock getter5
      getter = new GetValueThread(singleton, 6);
      assertFalse(getter6.isDone());
      assertFalse(setter.isDone());
      getter.start();
      
      // wait for the results on all threads
      // the setter must end last on the server
      setter.waitOnTheResult();
      setter.assertResult(6);
      // although the setter must end last on the server the response might not arrive last on the client
      getter6.waitOnTheResult();
      getter6.assertResult(6);
      getter.waitOnTheResult();
      getter.assertResult(6);
      // make sure setValue ended last on the server
      assertEquals("setValue", singleton.getLastReturnedValueMethod());
      
      // assert the current value is 1
      getter = new GetValueThread(singleton);
      getter.start();
      getter.waitOnTheResult();
      getter.assertResult(1);
      
      assertEquals(1, singleton.getInstanceCount());
   }
   
   private static abstract class AbstractValueThread extends Thread
   {
      final private SingletonRemote singleton;
      private int result;
      private boolean[] done = new boolean[1];
      private Throwable error;
      
      public AbstractValueThread(SingletonRemote singleton)
      {
         super();
         this.singleton = singleton;
      }
      
      protected abstract int execute(SingletonRemote singleton);

      public void run()
      {
         try
         {
            result = execute(singleton);
         }
         catch(Throwable t)
         {
            error = t;
         }
         finally
         {
            synchronized(done)
            {
               done[0] = true;
               done.notify();
            }
         }
      }
      
      public int getResult()
      {
         return result;
      }

      public boolean isDone()
      {
         return done[0];
      }
      
      public void waitOnTheResult()
      {
         synchronized(done)
         {
            while (!done[0])
            {
               try
               {
                  done.wait();
               }
               catch (InterruptedException e)
               {
               }
            }
         }
      }
      
      public Throwable getError()
      {
         return error;
      }
      
      public void assertResult(int expected)
      {
         if(error != null)
            fail(error.getMessage());
         assertEquals(expected, result);
      }
   }
   
   private static class SetValueThread extends AbstractValueThread
   {
      final int newValue;
      
      public SetValueThread(SingletonRemote singleton, int newValue)
      {
         super(singleton);
         this.newValue = newValue;
      }

      @Override
      protected int execute(SingletonRemote singleton)
      {
         return singleton.setValue(newValue);
      }
   }

   private static class GetValueThread extends AbstractValueThread
   {
      final int valueThreshold;
      final int timeout;

      public GetValueThread(SingletonRemote singleton)
      {
         this(singleton, 0, 1000);
      }

      public GetValueThread(SingletonRemote singleton, int valueThreshold)
      {
         this(singleton, valueThreshold, 2000);
      }
      
      public GetValueThread(SingletonRemote singleton, int valueThreshold, int timeout)
      {
         super(singleton);
         this.valueThreshold = valueThreshold;
         this.timeout = timeout;
      }

      @Override
      protected int execute(SingletonRemote singleton)
      {
         return singleton.getValue(valueThreshold, timeout);
      }
   }
}
