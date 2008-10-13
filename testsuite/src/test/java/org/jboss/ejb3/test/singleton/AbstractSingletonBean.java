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

/**
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class AbstractSingletonBean
{
   private static Object instanceLock = new Object();
   private static Thread lockerThread;
   
   protected int value;
   
   private String lastReturnedValueMethod;

   public AbstractSingletonBean()
   {
      super();
   }

   /**
    * This method demonstrates that once one thread is entered an instance method
    * no other thread can enter any method of the same instance in case of write concurrency.
    */
   public void writeLock(long pause)
   {
      Thread currentThread = Thread.currentThread();
      synchronized(instanceLock)
      {
         if(lockerThread == null)
            lockerThread = currentThread;
         else if(!lockerThread.equals(currentThread))
         {
            throw new IllegalStateException("Another thread is active in the instance: " + lockerThread + ", current thread: " + currentThread);
         }
      }
   
      try
      {
         Thread.sleep(pause);
      }
      catch (InterruptedException e)
      {
      }
      
      synchronized(instanceLock)
      {
         if(!currentThread.equals(lockerThread))
         {
            throw new IllegalStateException("Another thread is/was active in the instance: " + lockerThread + ", current thread: " + currentThread);
         }
         lockerThread = null;
      }
   }

   /**
    * This method demonstrates that two threads can be active in the same session bean instance in case of read concurrency.
    *  
    * 1. if current value is bigger than or equal the valueThreshold then return the current value.
    * 2. Increase the current value
    * 3. If the current value is less than valueThreshold then wait and let other threads to increase the value
    *    until it reaches the valueThreshold.
    * 4. Return the current value (which should be bigger than or equal to valueThreshold).
    *    
    * if waiting takes longer than timeout then throw an exception.
    */
   public int getValue(int valueThreshold, long timeout)
   {
      synchronized (instanceLock)
      {
         if (value >= valueThreshold)
         {
            lastReturnedValueMethod = "getValue";
            return value;
         }
   
         ++this.value;
   
         // wait until the other thread increases the current value
         long startTime = System.currentTimeMillis();
         while (this.value < valueThreshold)
         {
            long waitTime = System.currentTimeMillis() - startTime;
            if (waitTime > timeout)
               throw new IllegalStateException("The method took too long. Timeout=" + timeout + ", waitTime=" + waitTime + ", value=" + value);
   
            instanceLock.notify();
   
            try
            {
               instanceLock.wait(timeout);
            }
            catch (InterruptedException e)
            {
            }
         }
   
         lastReturnedValueMethod = "getValue";
         instanceLock.notify();
         return this.value;
      }
   }

   public int setValue(int newValue)
   {
      try
      {
         int prev = this.value;
         this.value = newValue;
         return prev;
      }
      finally
      {
         lastReturnedValueMethod = "setValue";
      }
   }

   public String getLastReturnedValueMethod()
   {
      return lastReturnedValueMethod;
   }
}