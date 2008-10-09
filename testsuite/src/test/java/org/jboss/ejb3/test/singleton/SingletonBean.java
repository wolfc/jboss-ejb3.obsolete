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
   
   public void testWriteLock(long pause)
   {
      Thread currentThread = Thread.currentThread();
      synchronized(instanceLock)
      {
         if(lockerThread == null)
            lockerThread = currentThread;
         else if(!lockerThread.equals(currentThread))
         {
            //System.out.println("Another thread is active in the instance: " + lockerThread + ", current thread: " + currentThread);
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
            //System.out.println("Another thread is active in the instance: " + lockerThread + ", current thread: " + currentThread);
            throw new IllegalStateException("Another thread is/was active in the instance: " + lockerThread + ", current thread: " + currentThread);
         }
         lockerThread = null;
      }
   }
}
