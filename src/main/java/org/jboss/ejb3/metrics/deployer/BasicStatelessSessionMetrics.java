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

import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;

/**
 * BasicStatelessSessionMetrics
 * 
 * Implementation of a SLSB metrics collector.  Additionally exposed as a 
 * management object.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@ManagementObject(isRuntime = true, properties = ManagementProperties.EXPLICIT, description = "Stateless Session Bean Metrics", componentType = @ManagementComponent(type = "EJB3", subtype = "SLSB"))
public class BasicStatelessSessionMetrics extends ManagedSessionMetricsWrapperBase
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Underlying Container through which we'll get the metrics
    */
   private StatelessContainer slsb;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param delegate Invocation stats delegate
    * @param slsb The underlying container
    * @throws IllegalArgumentException If either argument is not supplied
    */
   public BasicStatelessSessionMetrics(final InvocationStatistics delegate, final StatelessContainer slsb)
         throws IllegalArgumentException
   {
      // Invoke Super
      super(slsb, delegate);

      // Set
      this.setSlsb(slsb);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.metrics.deployer.ManagedSessionMetricsWrapperBase#getAvailableCount()
    */
   @Override
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The number of slots available in the instance pool")
   public int getAvailableCount()
   {
      return this.getPool().getAvailableCount();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.deployer.ManagedSessionMetricsWrapperBase#getCreateCount()
    */
   @Override
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The number of bean instances created")
   public int getCreateCount()
   {
      return this.getPool().getCreateCount();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.deployer.ManagedSessionMetricsWrapperBase#getCurrentSize()
    */
   @Override
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The current number of bean instances in the backing pool for this SLSB")
   public int getCurrentSize()
   {
      return this.getPool().getCurrentSize();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.deployer.ManagedSessionMetricsWrapperBase#getMaxSize()
    */
   @Override
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The maxmimum size of the backing instance pool")
   public int getMaxSize()
   {
      return this.getPool().getMaxSize();
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.deployer.ManagedSessionMetricsWrapperBase#getRemoveCount()
    */
   @Override
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The number of backing SLSB instances which have been removed")
   public int getRemoveCount()
   {
      return this.getPool().getRemoveCount();
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the underlying pool
    */
   private Pool getPool()
   {
      return this.getSlsb().getPool();
   }

   /**
    * @return the slsb
    */
   private StatelessContainer getSlsb()
   {
      return slsb;
   }

   /**
    * @param slsb the slsb to set
    */
   private void setSlsb(final StatelessContainer slsb)
   {
      this.slsb = slsb;
   }
}
