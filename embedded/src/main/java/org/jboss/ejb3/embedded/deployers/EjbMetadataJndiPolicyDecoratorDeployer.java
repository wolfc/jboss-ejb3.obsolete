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
package org.jboss.ejb3.embedded.deployers;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.deployers.MergedJBossMetaDataDeployer;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossEntityBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.BasicJndiBindingPolicy;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.JBossSessionPolicyDecorator;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.JbossEntityPolicyDecorator;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.DefaultJndiBindingPolicy;

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
      this.addInput(MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME);

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
      JBossMetaData md = du.getAttachment(MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME, JBossMetaData.class);

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

      // Initialize Map of beans to replace
      Map<JBossEnterpriseBeanMetaData, JBossEnterpriseBeanMetaData> beansToReplace = new HashMap<JBossEnterpriseBeanMetaData, JBossEnterpriseBeanMetaData>();

      // Obtain defined JNDI Binding Policy
      String mdJndiPolicyName = md.getJndiBindingPolicy();
      if (mdJndiPolicyName != null && mdJndiPolicyName.trim().length() == 0)
      {
         mdJndiPolicyName = null;
      }
      if (mdJndiPolicyName != null)
      {
         log.debug(JBossMetaData.class.getSimpleName() + " " + md + " has defined "
               + DefaultJndiBindingPolicy.class.getSimpleName() + " \"" + mdJndiPolicyName + "\"");
      }

      // For each of the Enterprise Beans
      JBossEnterpriseBeansMetaData beans = md.getEnterpriseBeans();
      for (JBossEnterpriseBeanMetaData bean : beans)
      {
         // Initialize a decorated instance
         JBossEnterpriseBeanMetaData decoratedBean = null;

         // Obtain a Policy
         DefaultJndiBindingPolicy policy = this.getJndiBindingPolicy(bean, mdJndiPolicyName, du.getClassLoader());

         // If this is a Session or Service Bean
         if (bean.isSession() || bean.isService())
         {
            // Cast
            assert bean instanceof JBossSessionBeanMetaData : JBossEnterpriseBeanMetaData.class.getSimpleName()
                  + " representing as Session Bean is not castable to " + JBossSessionBeanMetaData.class.getName();
            JBossSessionBeanMetaData sessionBean = (JBossSessionBeanMetaData) bean;

            // Create a Session JNDI Policy Decorated Bean
            decoratedBean = new JBossSessionPolicyDecorator(sessionBean, policy);
         }

         // If this is an Entity Bean
         if (bean.isEntity())
         {
            // Cast
            assert bean instanceof JBossEntityBeanMetaData : JBossEnterpriseBeanMetaData.class.getSimpleName()
                  + " representing as Entity Bean is not castable to " + JBossEntityBeanMetaData.class.getName();
            JBossEntityBeanMetaData entityBean = (JBossEntityBeanMetaData) bean;

            // Create a Entity JNDI Policy Decorated Bean
            decoratedBean = new JbossEntityPolicyDecorator(entityBean, policy);
         }

         // If we've decorated this bean, add to the map of beans to replace
         if (decoratedBean != null)
         {
            beansToReplace.put(bean, decoratedBean);
         }
      }

      // Replace with decorated beans
      for (JBossEnterpriseBeanMetaData beanToReplace : beansToReplace.keySet())
      {
         JBossEnterpriseBeanMetaData beanToReplaceWith = beansToReplace.get(beanToReplace);
         boolean removed = beans.remove(beanToReplace);
         assert removed : "Remove operation of " + beanToReplace + " from " + beans + " resulted in no action";
         beans.add(beanToReplaceWith);
         log.debug("Replaced " + beanToReplace.getEjbName() + " with decorated instance fit with "
               + DefaultJndiBindingPolicy.class.getSimpleName());
      }

      // Output
      du.addAttachment(EjbMetadataJndiPolicyDecoratorDeployer.EJB_DECORATED_FLAG, true, Boolean.class);
   }

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Obtains the JNDI Binding Policy instance to use for the specified metadata,
    * defaulting to a BasicJndiBindingPolicy if none is explicitly specified either in
    * the metadata itself or in its parent deployable unit
    *
    * @param md The Bean Metadata
    * @param deployableUnitDefaultJndiPolicyClassName The (optional) JNDI Policy declared
    *       by the deployable unit (JBossMetaData)
    * @param cl The Deployable Unit's ClassLoader
    */
   protected DefaultJndiBindingPolicy getJndiBindingPolicy(JBossEnterpriseBeanMetaData md,
         String deployableUnitDefaultJndiPolicyClassName, ClassLoader cl)
   {
      // Initialize a JNDI Binding Policy
      DefaultJndiBindingPolicy policy = null;

      // Obtain JNDI Policy Name defined at the EJB level
      String beanJndiPolicyName = md.getJndiBindingPolicy();
      if (beanJndiPolicyName != null && beanJndiPolicyName.trim().length() == 0)
      {
         beanJndiPolicyName = null;
      }
      if (beanJndiPolicyName != null)
      {
         log.debug("Session EJB " + md.getEjbName() + " has defined " + DefaultJndiBindingPolicy.class.getSimpleName()
               + " of \"" + beanJndiPolicyName);
      }

      // Use JNDI Policy defined by MD, then override at bean level
      String jndiPolicyName = deployableUnitDefaultJndiPolicyClassName != null
            ? deployableUnitDefaultJndiPolicyClassName
            : beanJndiPolicyName;

      // If JNDI Policy is defined
      if (jndiPolicyName != null)
      {
         // Load the configured JNDI Binding Policy
         Class<?> policyClass = null;
         try
         {
            policyClass = Class.forName(jndiPolicyName, true, cl);
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new RuntimeException("Could not find defined JNDI Binding Policy Class: " + jndiPolicyName, cnfe);
         }

         // Instanciate the configured JNDI Binding Policy
         try
         {
            policy = (DefaultJndiBindingPolicy) policyClass.newInstance();
         }
         catch (Throwable t)
         {
            throw new RuntimeException("Error in instanciating defined JNDI Binding Policy Class: " + jndiPolicyName, t);
         }

         // Log
         log.debug("Using " + DefaultJndiBindingPolicy.class.getSimpleName() + " \"" + policy.getClass().getName()
               + "\" for Session Bean " + md.getEjbName());
      }

      // If no JNDI Binding Policy was defined
      if (policy == null)
      {
         // Default to BasicJndiBindingPolicy
         policy = new BasicJndiBindingPolicy();
         log.debug("Defaulting to " + DefaultJndiBindingPolicy.class.getSimpleName() + " of \""
               + BasicJndiBindingPolicy.class.getName() + "\" for Session Bean " + md.getEjbName());
      }

      // Return
      return policy;
   }

}
