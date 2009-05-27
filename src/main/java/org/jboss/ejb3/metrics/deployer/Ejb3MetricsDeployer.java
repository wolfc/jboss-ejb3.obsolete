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

import org.jboss.beans.metadata.plugins.AbstractConstructorMetaData;
import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.metrics.spi.SessionMetrics;
import org.jboss.logging.Logger;

/**
 * Ejb3MetricsDeployer
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Ejb3MetricsDeployer extends AbstractSimpleRealDeployer<Ejb3Deployment>
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(Ejb3MetricsDeployer.class);

   /**
    * Name of our output, a flag upon which other deployers may rely
    */
   public static final String NAME_OUTPUT = Ejb3MetricsDeployer.class.getName();

   /**
    * Suffix to append to the Deployment name in order to create a unique bind name 
    * for the metrics POJO
    */
   private static final String BEAN_NAME_METRICS_SUFFIX = "-metrics";

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor; sets deployment stage and requisite input/output chain
    */
   public Ejb3MetricsDeployer()
   {
      // Invoke super
      super(Ejb3Deployment.class);

      // Output is a flag upon which other deployers may rely
      this.addOutput(NAME_OUTPUT);
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Creates a new Metrics collector for the EJB3 Deployment (typically created 
    * by the Ejb3Deployer) attached to this DeploymentUnit.  Installs the metrics 
    * into MC and sets upon the EJB3 Deployment.
    */
   @Override
   public void deploy(final DeploymentUnit du, final Ejb3Deployment deployment) throws DeploymentException
   {
      // Determine if we process this deployment
      if (deployment == null)
      {
         // Not an EJB3 Deployment
         if (log.isTraceEnabled())
         {
            log.trace("Skipping non-EJB3 Deployment: " + du);
         }
         return;
      }

      // Log
      if (log.isTraceEnabled())
      {
         log.trace("Deploying EJB3 Session metrics for : " + du);
      }

      // Make a new metrics definition
      final String metricsBeanName = deployment.getName() + BEAN_NAME_METRICS_SUFFIX;
      final SessionMetrics metrics = new BasicSessionMetrics();
      final BeanMetaDataBuilder bmdb = BeanMetaDataBuilder.createBuilder(metricsBeanName, metrics.getClass().getName());
      bmdb.setConstructorValue(new AlreadyInstantiated(metrics));
      final BeanMetaData bean = bmdb.getBeanMetaData();

      // Add the attachment
      du.addAttachment(BeanMetaData.class, bean);

      // Set metrics upon the deployment
      deployment.setMetrics(metrics);
      log.debug("Set EJB3 metrics upon " + du);

      // Set a flag showing we were here as the output
      du.addAttachment(NAME_OUTPUT, true, Boolean.class);
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor metadata which uses an underlying bean already 
    * instanciated.
    * 
    * @author Scott Stark
    */
   private static class AlreadyInstantiated extends AbstractConstructorMetaData
   {
      private static final long serialVersionUID = 1L;

      private Object bean;

      public class Factory
      {
         public Object create()
         {
            return bean;
         }
      }

      public AlreadyInstantiated(Object bean)
      {
         this.bean = bean;
         this.setFactory(new AbstractValueMetaData(new Factory()));
         this.setFactoryClass(Factory.class.getName());
         this.setFactoryMethod("create");
      }
   }

}
