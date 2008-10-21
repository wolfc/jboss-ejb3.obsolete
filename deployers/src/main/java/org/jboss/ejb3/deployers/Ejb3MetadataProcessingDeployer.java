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
package org.jboss.ejb3.deployers;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.process.chain.ProcessorChain;
import org.jboss.metadata.process.chain.ejb.jboss.JBossMetaDataProcessorChain;
import org.jboss.metadata.process.processor.JBossMetaDataProcessor;
import org.jboss.metadata.process.processor.ejb.jboss.JBossMetaDataValidatorChainProcessor;
import org.jboss.metadata.process.processor.ejb.jboss.SetDefaultLocalBusinessInterfaceProcessor;

/**
 * Ejb3MetadataProcessingDeployer
 * 
 * Runs post-merge processing on EJB3 Metadata
 * to apply rules, perform validation, etc
 * 
 * JBMETA-132
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Ejb3MetadataProcessingDeployer extends AbstractDeployer
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(Ejb3MetadataProcessingDeployer.class);

   //TODO
   /*
    * This is reflected in the public static field:
    * public static final String EJB_MERGED_ATTACHMENT_NAME = "merged."+JBossMetaData.class.getName();
    * ...of the MergedJBossMetaDataDeployer, which is not currently visible from here
    */
   /**
    * Deployer Input, set to merged metadata
    */
   private static final String INPUT = "merged." + JBossMetaData.class.getName();

   /**
    * Deployer Output, the Processed metadata
    */
   public static final String OUTPUT = "processed." + JBossMetaData.class.getName();

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor; sets deployment stage and requisite input/output chain
    */
   public Ejb3MetadataProcessingDeployer()
   {
      // Set the Stage to post-CL
      this.setStage(DeploymentStages.POST_CLASSLOADER);

      // Input is the JBossMetaData post-merge
      this.addInput(INPUT);

      // Output is a flag upon which other deployers may rely
      this.addOutput(OUTPUT);
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * For DeploymentUnits with a merged metadata attachment representing
    * EJB3 beans, will run a standard ProcessorChain and output the 
    * processed result
    * 
    * @see org.jboss.deployers.spi.deployer.Deployer#deploy(org.jboss.deployers.structure.spi.DeploymentUnit)
    */
   public void deploy(DeploymentUnit du) throws DeploymentException
   {
      // Obtain the Merged Metadata
      JBossMetaData md = du.getAttachment(INPUT, JBossMetaData.class);

      // If metadata's not present as an attachment, return
      if (md == null)
      {
         return;
      }

      // If this is not an EJB3 Deployment, return
      if (!md.isEJB3x())
      {
         return;
      }

      // Get a Processor Chain
      ProcessorChain<JBossMetaData> chain = this.getProcessorChain(du);

      // Create new processed metadata
      JBossMetaData processedMetadata = chain.process(md);

      // Set the processed metadata as the output
      du.addAttachment(OUTPUT, processedMetadata, JBossMetaData.class);
   }

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Obtains the ProcessorChain to be run on the merged metadata
    * 
    * @param du
    */
   protected ProcessorChain<JBossMetaData> getProcessorChain(DeploymentUnit du)
   {
      // Initialize
      ProcessorChain<JBossMetaData> chain = new JBossMetaDataProcessorChain<JBossMetaData>();
      StringBuffer logMessage = new StringBuffer("Creating ");
      logMessage.append(ProcessorChain.class.getSimpleName());
      logMessage.append(" with the following Processors:");

      // Obtain processors to put in the chain
      Collection<JBossMetaDataProcessor<JBossMetaData>> processors = this.getProcessors(du);

      // For each of the processors
      if (processors != null)
      {
         for (JBossMetaDataProcessor<JBossMetaData> processor : processors)
         {
            // Add to the chain
            chain.addProcessor(processor);
            logMessage.append(" ");
            logMessage.append(processor);
         }
      }

      // Log
      log.debug(logMessage.toString());

      // Return
      return chain;

   }

   /**
    * Obtains the processors to use in the ProcessorChain to be run 
    * on the merged metadata attachment of the DeploymentUnit
    * 
    * @param du
    * @return
    */
   @SuppressWarnings("unchecked")
   protected Collection<JBossMetaDataProcessor<JBossMetaData>> getProcessors(DeploymentUnit du)
   {
      // Initialize
      Collection<JBossMetaDataProcessor<JBossMetaData>> processors = new ArrayList<JBossMetaDataProcessor<JBossMetaData>>();

      /*
       * Add processors
       * 
       * Maintainer's note: The order here is preserved
       */

      // JBMETA-122 Implicit Local Business Interface
      ClassLoader deploymentCl = du.getClassLoader();
      processors.add(new SetDefaultLocalBusinessInterfaceProcessor<JBossMetaData>(deploymentCl));

      // JBMETA-118 Validation
      processors.add(JBossMetaDataValidatorChainProcessor.INSTANCE);

      /*
       * End Processor Adding
       */

      // Return
      return processors;
   }

}
