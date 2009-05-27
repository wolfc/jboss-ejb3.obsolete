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

import org.jboss.ejb3.metrics.spi.SessionMetrics;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;

/**
 * BasicSessionMetrics
 * 
 * Threadsafe implementation of a session metrics collector.
 * Additionally exposed as a management object
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@ManagementObject(isRuntime = true, properties = ManagementProperties.EXPLICIT, description = "Some test", componentType = @ManagementComponent(type = "MCBean", subtype = "*"))
public class BasicSessionMetrics implements SessionMetrics
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Number of bean instances in the underlying pool/cache
    * available for service.  Synchronized on "this".  
    */
   private int availableCount;

   /**
    * Number of bean instances created for this EJB.
    * Synchronized on "this".
    */
   private int createCount;

   /**
    * Number of bean instances in the underlying 
    * pool/cache.  Synchronized on "this".
    */
   private int currentSize;

   /**
    * Size of the underlying instance pool/cache
    * at its highest.  Synchronized on "this".
    */
   private int maxSize;

   /**
    * Number of bean instances removed from the
    * underlying pool/cache.  Synchronized on "this".
    */
   private int removeCount;

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#getAvailableCount()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC)
   public synchronized int getAvailableCount()
   {
      return this.availableCount;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#getCreateCount()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC)
   public synchronized int getCreateCount()
   {
      return this.createCount;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#getCurrentSize()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC)
   public synchronized int getCurrentSize()
   {
      return this.currentSize;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#getMaxSize()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC)
   public synchronized int getMaxSize()
   {
      return this.maxSize;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#getRemoveCount()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC)
   public synchronized int getRemoveCount()
   {
      return this.removeCount;
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#setAvailableCount(int)
    */
   public synchronized void setAvailableCount(final int availableCount)
   {
      this.availableCount = availableCount;
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#setCreateCount(int)
    */
   public synchronized void setCreateCount(final int createCount)
   {
      this.createCount = createCount;
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#setCurrentSize(int)
    */
   public synchronized void setCurrentSize(final int currentSize)
   {
      this.currentSize = currentSize;
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#setMaxSize(int)
    */
   public synchronized void setMaxSize(final int maxSize)
   {
      this.maxSize = maxSize;
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.metrics.spi.SessionMetrics#setRemoveCount(int)
    */
   public synchronized void setRemoveCount(final int removeCount)
   {
      this.removeCount = removeCount;
   }
}
