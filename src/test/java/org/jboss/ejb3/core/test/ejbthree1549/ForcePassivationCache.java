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

import org.jboss.ejb3.cache.simple.SimpleStatefulCache;
import org.jboss.logging.Logger;

/**
 * ForcePassivationCache
 * 
 * An extension of the SimpleStatefulCache which provides for
 * forcing the Passivation Thread to run
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ForcePassivationCache extends SimpleStatefulCache
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ForcePassivationCache.class);

   private static final Object PASSIVATION_LOCK = new Object();

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Triggers passivation to run
    */
   public static void forcePassivation()
   {
      // Get a lock
      log.info("Awaiting lock to force passivation");
      synchronized (PASSIVATION_LOCK)
      {
         // Notify that passivation should run
         log.info("Notifying passivation via manual force...");
         PASSIVATION_LOCK.notify();
      }
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   public void start()
   {
      // Switch up the Timeout (Passivation) Task to a blocking implementation
      this.setTimeoutTask(new BlockingPassivationTask("EJBTHREE-1549 SFSB Passivation Thread"));

      // Call super implementation
      super.start();

   }

   /**
    * BlockingPassivationTask
    * 
    * An extension of the default timeout task which, instead of 
    * waiting for a timeout, will await (block until) notification that passivation
    * should run
    *
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private class BlockingPassivationTask extends SessionTimeoutTask
   {
      public BlockingPassivationTask(String name)
      {
         super(name);
      }

      public void block() throws InterruptedException
      {
         // Get a lock on our monitor
         synchronized (PASSIVATION_LOCK)
         {
            // Wait until we're signaled
            log.info("Waiting to be notified to run passivation...");
            PASSIVATION_LOCK.wait();
         }
         
         // Log that we've been notified
         log.info("Notified to run passivation");
      }
   }

}