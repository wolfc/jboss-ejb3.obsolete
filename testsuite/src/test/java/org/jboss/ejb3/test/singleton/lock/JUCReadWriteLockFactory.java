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

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A JUCReadWriteLockFactory.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class JUCReadWriteLockFactory implements LockFactory
{
   private final ReadWriteLock lock;
   
   public JUCReadWriteLockFactory()
   {
      lock = new ReentrantReadWriteLock(true);
   }
   
   public Lock createLock(boolean read)
   {
      return new JUCLockAdaptor(read ? lock.readLock() : lock.writeLock());
   }

   private static class JUCLockAdaptor implements Lock
   {
      private final java.util.concurrent.locks.Lock lock;
      
      public JUCLockAdaptor(java.util.concurrent.locks.Lock lock)
      {
         this.lock = lock;
      }
      
      public void lock()
      {
         lock.lock();
      }

      public void unlock()
      {
         lock.unlock();
      }
   }
}
