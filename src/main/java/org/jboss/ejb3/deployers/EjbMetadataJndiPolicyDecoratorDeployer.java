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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.common.metadata.MetadataUtil;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * EjbMetadataJndiPolicyDecoratorDeployer
 * 
 * Decorates merged EJB Metadata with the ability to resolve
 * JNDI targets as dictated by an optionally-specified
 * DefaultJndiBindingPolicy
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EjbMetadataJndiPolicyDecoratorDeployer extends AbstractDeployer
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(EjbMetadataJndiPolicyDecoratorDeployer.class);

   /**
    * Deployer Input, set to processed metadata
    */
   private static final String INPUT = Ejb3MetadataProcessingDeployer.OUTPUT;

   /**
    * Deployer Output flag signaling that this deployer has been run
    */
   public static final String EJB_DECORATED_FLAG = EjbMetadataJndiPolicyDecoratorDeployer.class.getSimpleName();

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor; sets deployment stage and requisite input/output chain
    */
   public EjbMetadataJndiPolicyDecoratorDeployer()
   {
      // Set the Stage to post-CL
      this.setStage(DeploymentStages.POST_CLASSLOADER);

      // Input is the JBossMetaData post-merge
      this.addInput(INPUT);

      // Output is a flag upon which other deployers may rely
      this.addOutput(EjbMetadataJndiPolicyDecoratorDeployer.EJB_DECORATED_FLAG);
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /* (non-Javadoc)
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

      // Decorate
      log.debug("Decorating EJB3 EJBs in " + md + " with JNDI Policy");
      MetadataUtil.decorateEjbsWithJndiPolicy(md, du.getClassLoader());

      // Output
      du.addAttachment(EjbMetadataJndiPolicyDecoratorDeployer.EJB_DECORATED_FLAG, true, Boolean.class);
   }

}
