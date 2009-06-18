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

import javax.ejb.TimerService;

import org.jboss.ejb3.mdb.MessagingContainer;
import org.jboss.ejb3.mdb.MessagingDelegateWrapperMBean;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.annotations.MetaMapping;

/**
 * BasicMessageDrivenMetrics
 * 
 * Implementation of a MDB 
 * metrics collector.  Additionally exposed as a 
 * management object.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@ManagementObject(isRuntime = true, properties = ManagementProperties.EXPLICIT, description = "Message-Driven Instance Metrics", componentType = @ManagementComponent(type = "EJB3", subtype = "MessageDriven"))
public class BasicMessageDrivenMetrics implements MessagingDelegateWrapperMBean
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Underlying Container through which we'll get the metrics
    */
   private MessagingContainer mdb;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param slsb The underlying container
    * @throws IllegalArgumentException If the underlying container is not supplied
    */
   public BasicMessageDrivenMetrics(final MessagingContainer mdb) throws IllegalArgumentException
   {
      // Precondition check
      if (mdb == null)
      {
         throw new IllegalArgumentException("Underlying container was null");
      }

      // Set
      this.setMessagingContainer(mdb);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC)
   public String getName()
   {
      return this.getMessagingContainer().getEjbName();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.mdb.MessagingDelegateWrapperMBean#getKeepAliveMillis()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The number of milliseconds the instance will keep-alive")
   public int getKeepAliveMillis()
   {
      return this.getMBean().getKeepAliveMillis();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.mdb.MessagingDelegateWrapperMBean#getMaxMessages()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The maximum number of messages")
   public int getMaxMessages()
   {
      return this.getMBean().getMaxMessages();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.mdb.MessagingDelegateWrapperMBean#getMaxPoolSize()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The maximum number of backing objects allowed in the instance pool")
   public int getMaxPoolSize()
   {
      return this.getMBean().getMaxPoolSize();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.mdb.MessagingDelegateWrapperMBean#getMinPoolSize()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "The minimum number of backing objects allowed in the instance pool")
   public int getMinPoolSize()
   {
      return this.getMBean().getMinPoolSize();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.mdb.MessagingDelegateWrapperMBean#isDeliveryActive()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "If active delivery is enabled")
   public boolean isDeliveryActive()
   {
      return this.getMBean().isDeliveryActive();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.mdb.MessagingDelegateWrapperMBean#startDelivery()
    */
   @ManagementOperation(description = "Starts delivery to the MDB")
   public void startDelivery()
   {
      this.getMBean().startDelivery();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.mdb.MessagingDelegateWrapperMBean#stopDelivery()
    */
   @ManagementOperation(description = "Stops delivery to the MDB")
   public void stopDelivery()
   {
      this.getMBean().stopDelivery();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.ContainerDelegateWrapperMBean#getInvokeStats()
    */
   @ManagementProperty(readOnly = true, use = ViewUse.STATISTIC, description = "Obtains the invocation statistics for this MDB", name = "invocationStats")
   @MetaMapping(InvocationStatisticMetaMapper.class)
   public InvocationStatistics getInvokeStats()
   {
      return this.getMBean().getInvokeStats();
   }

   /** 
    * @see org.jboss.ejb3.statistics.InvocationStatistics#resetStats()
    */
   @ManagementOperation
   public void resetInvocationStats()
   {
      this.getMBean().getInvokeStats().resetStats();
   }

   /*
    * Not a managed operation, breaks the contract of
    * MessagingDelegateWrapperMBean.  Here just to appease the compiler.
    */
   public TimerService getTimerService(Object key)
   {
      throw new UnsupportedOperationException("Not supported via Managed Object; invoke over JMX Bus");
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the underlying pool
    */
   private MessagingDelegateWrapperMBean getMBean()
   {
      return (MessagingDelegateWrapperMBean) this.getMessagingContainer().getMBean();
   }

   /**
    * @return the slsb
    */
   private MessagingContainer getMessagingContainer()
   {
      return mdb;
   }

   /**
    * @param slsb the slsb to set
    */
   private void setMessagingContainer(final MessagingContainer mdb)
   {
      this.mdb = mdb;
   }
}
