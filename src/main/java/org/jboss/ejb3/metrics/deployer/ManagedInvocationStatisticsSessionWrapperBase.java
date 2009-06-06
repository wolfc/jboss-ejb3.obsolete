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

import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.annotations.MetaMapping;

/**
 * ManagedInvocationStatisticsSessionWrapperBase
 * 
 * Base class to to delegate to the underlying invocation stats, 
 * exposing management properties and operations.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
class ManagedInvocationStatisticsSessionWrapperBase
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The delegate
    */
   private InvocationStatistics delegate;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param delegate
    * @throws IllegalArgumentException If the delegate was not supplied
    */
   ManagedInvocationStatisticsSessionWrapperBase(final InvocationStatistics delegate) throws IllegalArgumentException
   {
      // Precondition check
      if (delegate == null)
      {
         throw new IllegalArgumentException("Supplied delegate was null");
      }

      this.setDelegate(delegate);
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
   public Map getStats()
   {
      return delegate.getStats();
   }

   /**
    * 
    * @see org.jboss.ejb3.statistics.InvocationStatistics#resetStats()
    */
   @ManagementOperation
   public void resetStats()
   {
      delegate.resetStats();
   }

   /**
    * Exposes the time, represented in milliseconds since the epoch, 
    * that the stats were last reset
    * @return
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC)
   public long getLastResetTime()
   {
      return delegate.lastResetTime;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * @param stats the stats to set
    */
   private void setDelegate(final InvocationStatistics stats)
   {
      this.delegate = stats;
   }

}
