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

import org.jboss.ejb3.cache.StatefulCache;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;

/**
 * BasicStatefulSessionInstanceMetrics
 * 
 * Implementation of a SFSB instance
 * metrics collector.  Additionally exposed as a 
 * management object.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@ManagementObject(isRuntime = true, properties = ManagementProperties.EXPLICIT, description = "Stateful Session Bean Metrics", componentType = @ManagementComponent(type = "EJB3", subtype = "StatefulSession"))
public class BasicStatefulSessionMetrics extends ManagedSessionMetricsWrapperBase
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Underlying Container through which we'll get the metrics
    */
   private StatefulContainer sfsb;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param invocationStats Invocation stats delegate
    * @param slsb The underlying container
    * @throws IllegalArgumentException If either argument is not supplied
    */
   public BasicStatefulSessionMetrics(final InvocationStatistics invocationStats, final StatefulContainer sfsb)
         throws IllegalArgumentException
   {
      // Invoke super
      super(sfsb, invocationStats);

      // Set
      this.setSlsb(sfsb);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||   

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.StatefulSessionInstanceMetrics#getCacheSize()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The size of the SFSB instance cache for currently active sessions")
   public int getCacheSize()
   {
      return this.getCache().getCacheSize();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.StatefulSessionInstanceMetrics#getTotalSize()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The total size of the SFSB instance cache, including passivated sessions")
   public int getTotalSize()
   {
      return this.getCache().getTotalSize();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.StatefulSessionInstanceMetrics#getPassivatedCount()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The number of sessions currently passivated")
   public int getPassivatedCount()
   {
      return this.getCache().getPassivatedCount();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionInstanceMetrics#getCreateCount()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The number of sessions created")
   public int getCreateCount()
   {
      return this.getCache().getCreateCount();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionInstanceMetrics#getRemoveCount()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The number of sessions removed")
   public int getRemoveCount()
   {
      return this.getCache().getRemoveCount();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionInstanceMetrics#getAvailableCount()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The number of sessions that may be added to the current cache")
   public int getAvailableCount()
   {
      return this.getCache().getAvailableCount();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionInstanceMetrics#getMaxSize()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The maximum size of the SFSB instance cache")
   public int getMaxSize()
   {
      return this.getCache().getMaxSize();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionInstanceMetrics#getCurrentSize()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The number of sessions currently active")
   public int getCurrentSize()
   {
      return this.getCache().getCurrentSize();
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the underlying cache
    */
   private StatefulCache getCache()
   {
      return this.getSfsb().getCache();
   }

   /**
    * @return the slsb
    */
   private StatefulContainer getSfsb()
   {
      return sfsb;
   }

   /**
    * @param sfsb the slsb to set
    */
   private void setSlsb(final StatefulContainer sfsb)
   {
      this.sfsb = sfsb;
   }
}
