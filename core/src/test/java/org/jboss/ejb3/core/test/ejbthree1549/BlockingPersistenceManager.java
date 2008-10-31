/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.core.test.ejbthree1549;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.ejb3.cache.simple.StatefulSessionFilePersistenceManager;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.logging.Logger;

/**
 * BlockingPersistenceManager
 * 
 * An implementation of a PersistenceManager which, instead of
 * persisting directly, exposes a blocking mechanism allowing 
 * tests to control when passivation occurs 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class BlockingPersistenceManager extends StatefulSessionFilePersistenceManager
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(BlockingPersistenceManager.class);

   /**
    * Publicly-accessible lock to allow tests to block passivation
    */
   public static final Lock LOCK = new ReentrantLock();

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   public void passivateSession(StatefulBeanContext ctx)
   {
      // Block until the lock may be acquired, 
      // may currently be held by the test Thread
      LOCK.lock();

      try
      {
         // Mock Passivate
         log.info("Mock Passivation on " + ctx);
      }
      finally
      {
         // Release the lock
         LOCK.unlock();
      }

   }

}
