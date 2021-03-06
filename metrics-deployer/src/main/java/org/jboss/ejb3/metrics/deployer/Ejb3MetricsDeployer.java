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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.plugins.AbstractConstructorMetaData;
import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.mdb.MessagingContainer;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.spi.MetaData;

/**
 * Ejb3MetricsDeployer
 * 
 * Creates ManagedObject attachments to the current EJB3 deployment
 * for Session Bean metrics.
 * 
 * EJBTHREE-1839
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Ejb3MetricsDeployer extends AbstractSimpleRealDeployer<Ejb3Deployment> implements ManagedObjectCreator
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
    * The managed object factory.
    */
   private ManagedObjectFactory managedObjectFactory = ManagedObjectFactory.getInstance();

   /**
    * Delimiter in constructing bind / component names
    */
   private static final char DELIMITER = '/';

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

      // Get out all EJB3 Containers
      final Collection<Container> containers = deployment.getEjbContainers().values();
      if (containers != null)
      {
         final AbstractKernelDeployment kernelDeployment = new AbstractKernelDeployment();
         List<BeanMetaDataFactory> beanFactories = new ArrayList<BeanMetaDataFactory>();
         kernelDeployment.setBeanFactories(beanFactories);

         // For each EJB Container
         for (final Container container : containers)
         {
            // Get the EJB Name
            final String deploymentUnitName = du.getSimpleName();
            final StringBuilder bindNameBuilder = new StringBuilder();

            // If we're in an EAR
            final DeploymentUnit topLevelDu = du.getTopLevel();
            boolean isEar = du != topLevelDu || topLevelDu.isAttachmentPresent(JBossAppMetaData.class);
            if (isEar)
            {
               // Prepend the EAR name
               bindNameBuilder.append(topLevelDu.getSimpleName());
               bindNameBuilder.append(DELIMITER);
            }
            // JAR name
            if (deploymentUnitName == null)
            {
               bindNameBuilder.append("*");
            }
            else
            {
               bindNameBuilder.append(deploymentUnitName);
               bindNameBuilder.append(DELIMITER);
            }
            // EJB Name
            final String ejbName = container.getEjbName();
            bindNameBuilder.append(ejbName);
            final String bindName = bindNameBuilder.toString();

            // Session Containers
            if (container instanceof SessionContainer)
            {
               // Cast
               final SessionContainer sessionContainer = (SessionContainer) container;

               // Get the invocation stats
               final InvocationStatistics stats = sessionContainer.getInvokeStats();
               if (stats == null)
               {
                  throw new IllegalStateException("Invocation statistics was null");
               }

               // SLSB
               if (sessionContainer instanceof StatelessContainer)
               {
                  // Cast
                  final StatelessContainer slsb = (StatelessContainer) sessionContainer;

                  // Make new metrics
                  final BasicStatelessSessionMetrics metrics = new BasicStatelessSessionMetrics(stats, slsb);

                  // Add to beanFactories
                  this.attach(metrics, bindName, beanFactories);
                  log.debug("Attached metrics for: " + bindName);
               }

               // SFSB
               else if (sessionContainer instanceof StatefulContainer)
               {
                  // Cast
                  final StatefulContainer sfsb = (StatefulContainer) sessionContainer;

                  // Make new metrics
                  final BasicStatefulSessionMetrics metrics = new BasicStatefulSessionMetrics(stats, sfsb);

                  // Add to beanFactories
                  this.attach(metrics, bindName, beanFactories);
                  log.debug("Attached metrics for: " + bindName);
               }

            }
            // MDB
            else if (container instanceof MessagingContainer)
            {
               // Cast
               final MessagingContainer mdb = (MessagingContainer) container;

               // Make new metrics
               final BasicMessageDrivenMetrics metrics = new BasicMessageDrivenMetrics(mdb);

               // Add to beanFactories
               this.attach(metrics, bindName, beanFactories);
               log.debug("Attached stats for: " + bindName);
            }
         }

         // Add the Kernel Attachment 
         du.addAttachment(NAME_OUTPUT, kernelDeployment, KernelDeployment.class);
      }

   }

   /**
    * Build the managed object for the ejb3 metrics.
    */
   public void build(DeploymentUnit unit, Set<String> attachmentNames, Map<String, ManagedObject> managedObjects)
         throws DeploymentException
   {
      KernelDeployment deployment = unit.getAttachment(NAME_OUTPUT, KernelDeployment.class);
      if (deployment != null)
      {
         for (BeanMetaData bmd : deployment.getBeans())
         {
            MetaData metaData = null;
            DeploymentUnit compUnit = unit.getComponent(bmd.getName());
            if (compUnit != null)
               metaData = compUnit.getMetaData();

            ManagedObject mo = managedObjectFactory.initManagedObject(bmd, null, metaData, bmd.getName(), null);
            if (mo != null)
               managedObjects.put(bmd.getName(), mo);
         }
      }
   }

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Attaches the specified object to the specified beanFactories
    * 
    * @param attachment
    * @param beanName
    * @param beanFactories
    * @throws IllegalArgumentException If any argument is not specified
    */
   private void attach(final Object attachment, final String beanName, final List<BeanMetaDataFactory> beanFactories)
         throws IllegalArgumentException
   {
      // Precondition Checks
      if (attachment == null)
      {
         throw new IllegalArgumentException("metrics is null");
      }

      // Create the BeanMetaData manually, as
      // BeanMetaDataBuilder.setConstructor is doing some nonsense
      AbstractBeanMetaData bmd = new AbstractBeanMetaData(beanName, attachment.getClass().getName());
      AbstractConstructorMetaData cmd = new AbstractConstructorMetaData();
      cmd.setValue(new AbstractValueMetaData(attachment));
      bmd.setConstructor(cmd);

      // Add to beanFactories
      if (log.isTraceEnabled())
      {
         log.debug("Attaching MC Bean with name \"" + beanName + "\": " + bmd);
      }
      beanFactories.add(bmd);

   }

}
