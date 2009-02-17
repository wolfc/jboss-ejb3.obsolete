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

package org.jboss.ejb3.cache.spi.impl;

import org.jboss.ejb3.cache.spi.PassivationExpirationProcessor;
import org.jboss.logging.Logger;

/**
 * TimerTask that will periodically invoke 
 * {@link PassivationExpirationProcessor#processPassivationExpiration()}
 * on a single processor.
 * 
 * @author Brian Stansberry
 */
public class PassivationExpirationRunner 
   extends AbstractTimerTask
{
   private static final Logger log = Logger.getLogger(PassivationExpirationRunner.class);
   
   private PassivationExpirationProcessor processor;
   
   /**
    * Create a new PassivationExpirationRunner.
    * 
    */
   public PassivationExpirationRunner(PassivationExpirationProcessor processor, 
                                      String timerName, long interval)
   {
      super(timerName, interval);
      
      assert processor != null : "processor is null";
      this.processor = processor;
   }

   public void run()
   {
      try
      {
         if (!isStopped())
         {
            processor.processPassivationExpiration();
         }
      }
      catch (Exception e)
      {
         // Don't let it kill the Timer thread
         log.error("Caught exception processing passivation/expiration", e);
      }
   }

}
