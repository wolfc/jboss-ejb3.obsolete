/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.metrics.deployer;

import java.util.Map;

import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;

/**
 * ManagedSessionMetricsWrapperBase
 * 
 * Base class to to delegate to the underlying invocation stats and
 * instance metrics, exposing management properties and operations.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
abstract class ManagedSessionMetricsWrapperBase
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The underlying session container
    */
   private SessionContainer container;

   /**
    * The delegate
    */
   private InvocationStatistics invocationStats;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param container
    * @param invocationStats
    * @throws IllegalArgumentException If the stats or EJB Name were not supplied
    */
   ManagedSessionMetricsWrapperBase(final SessionContainer container, final InvocationStatistics invocationStats)
         throws IllegalArgumentException
   {
      // Precondition check
      if (invocationStats == null)
      {
         throw new IllegalArgumentException("Supplied invocation stats was null");
      }
      if (container == null)
      {
         throw new IllegalArgumentException("Container was null");
      }

      // Set properties
      this.setInvocationStats(invocationStats);
      this.setContainer(container);
   }

   // --------------------------------------------------------------------------------||
   // Delegate Methods ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * @return
    * @see org.jboss.ejb3.statistics.InvocationStatistics#getStats()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC)
   //@MetaMapping(value = InvocationStatisticMetaMapper.class)
   public Map getInvocationStats()
   {
      return invocationStats.getStats();
   }

   /**
    * 
    * @see org.jboss.ejb3.statistics.InvocationStatistics#resetStats()
    */
   @ManagementOperation
   public void resetInvocationStats()
   {
      invocationStats.resetStats();
   }

   /**
    * Exposes the time, represented in milliseconds since the epoch, 
    * that the stats were last reset
    * @return
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC)
   public long getInvocationStatsLastResetTime()
   {
      return invocationStats.lastResetTime;
   }

   /**
    * Returns the EJB Name
    * @return
    */
   @ManagementProperty(readOnly = true, use = ViewUse.CONFIGURATION)
   public String getName()
   {
      return this.container.getEjbName();
   }

   // --------------------------------------------------------------------------------||
   // Required Session Instance Metrics ----------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * The following properties are to be exposed by 
    * the backing instance pool/cache
    */

   /**
    * The number of instances available for service
    */
   abstract int getAvailableCount();

   /**
    * The number of instances created
    * @return
    */
   abstract int getCreateCount();

   /**
    * The current size of the backing pool/cache
    * @return
    */
   abstract int getCurrentSize();

   /**
    * The maximum size of the backing pool/cache
    * @return
    */
   abstract int getMaxSize();

   /**
    * The number of instances removed 
    * @return
    */
   abstract int getRemoveCount();

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * @param invocationStats the stats to set
    */
   private void setInvocationStats(final InvocationStatistics invocationStats)
   {
      this.invocationStats = invocationStats;
   }

   /**
    * @param container The underlying Session Container
    */
   private void setContainer(final SessionContainer container)
   {
      this.container = container;
   }

}
