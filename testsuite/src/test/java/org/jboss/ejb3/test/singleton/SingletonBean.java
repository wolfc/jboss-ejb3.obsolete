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

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.AspectDomain;
import org.jboss.ejb3.annotation.Pool;

/**
 * A SingletonBean.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
@Stateless
@Remote(SingletonRemote.class)
@Pool (value="SingletonPool")
@AspectDomain(value = "Singleton Stateless Bean")
public class SingletonBean implements SingletonRemote
{
   // counter for created instances
   private static Integer instanceCount = 0;
   
   // these are static to catch not only threads in the same instance but also in other instances
   // (if the singleton appears to be not a singleton)
   private static Object instanceLock = new Object();
   private static Thread lockerThread;
   
   // some instance variable
   private int value;
   
   // instance initialization
   {
      synchronized(instanceCount)
      {
         ++instanceCount;
      }
   }
   
   public int getInstanceCount()
   {
      return instanceCount;
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
    * 1. check that the value (instance variable) is equal to the expectedCurrentValue.
    *    If it's not then wait for the other thread to set it to the expectedCurrentValue.
    * 2. increase the value
    * 3. if the expectedCurrentValue != 0 then just return the current value.
    *    Otherwise wait for the other thread to change the value and return the current value.
    *    
    * if waiting takes longer than timeout then throw an exception.
    */
   public int getReadLock(int expectedCurrentValue, long timeout)
   {
      long startTime = System.currentTimeMillis();
      synchronized(instanceLock)
      {
         // make sure value has the expected value
         while(expectedCurrentValue != this.value)
         {
            if (System.currentTimeMillis() - startTime > timeout)
               throw new IllegalStateException("The method took too long.");
            try
            {
               instanceLock.wait(timeout);
            }
            catch (InterruptedException e)
            {
            }            
         }

         // at this point value == expectedCurrentValue
         if(expectedCurrentValue != this.value)
            throw new IllegalStateException("Unexpected instance variable value. Expected " + expectedCurrentValue + " but was " + this.value);

         // increase the value
         ++this.value;
         instanceLock.notify();
         
         if(expectedCurrentValue == 0)
         {
            // wait until the other thread increases the current value
            startTime = System.currentTimeMillis();
            while (this.value == expectedCurrentValue + 1)
            {
               if (System.currentTimeMillis() - startTime > timeout)
                  throw new IllegalStateException("The method took too long.");

               try
               {
                  instanceLock.wait(timeout);
               }
               catch (InterruptedException e)
               {
               }
            }
         }
      }
      
      return this.value;
   }
}
