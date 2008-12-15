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
package org.jboss.ejb3.mcint.metadata.plugins;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.ejb3.common.deployers.spi.AttachmentNames;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.resolvers.spi.EjbReference;
import org.jboss.ejb3.common.resolvers.spi.EjbReferenceResolver;
import org.jboss.ejb3.common.resolvers.spi.UnresolvableReferenceException;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.reflect.spi.TypeInfo;

/**
 * AbstractEjbReferenceValueMetadata
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AbstractEjbReferenceValueMetadata extends AbstractValueMetaData
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   private static final long serialVersionUID = 1L;

   private static final String MC_BEAN_NAME_MAIN_DEPLOYER = "MainDeployer";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   private EjbReferenceResolver resolver;

   private EjbReference reference;

   private Context context;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Sole Constructor
    */
   public AbstractEjbReferenceValueMetadata(EjbReferenceResolver resolver, EjbReference reference, Context context)
   {
      // Precondition check
      assert resolver != null : "Resolver is required, but was not specified";
      assert reference != null : "EJB Reference is required, but was not specified";
      assert context != null : "Naming Context is required, but was not specified";

      // Set properties
      this.setResolver(resolver);
      this.setReference(reference);
      this.setContext(context);
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   @Override
   public Object getValue(TypeInfo info, ClassLoader cl) throws Throwable
   {
      return this.resolveEjb();
   }

   @Override
   public Object getValue()
   {
      return this.resolveEjb();
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the EJB from JNDI based upon the resolved JNDI name
    */
   protected Object resolveEjb()
   {
      // Initialize
      Object obj = null;
      String jndiName = null;
      
      /*
       * Look through all EJB3 DeploymentUnits
       */

      // Get at the MainDeployer
      Object mainDeployer = Ejb3RegistrarLocator.locateRegistrar().lookup(MC_BEAN_NAME_MAIN_DEPLOYER);
      assert mainDeployer instanceof DeployerClient && mainDeployer instanceof MainDeployerStructure : "Obtained Main Deployer is not of expected type";
      DeployerClient dc = (DeployerClient) mainDeployer;
      MainDeployerStructure mds = (MainDeployerStructure) mainDeployer;

      // Loop through each Deployment
      for (Deployment d : dc.getTopLevel())
      {
         // Get the associated DU
         DeploymentUnit du = mds.getDeploymentUnit(d.getName());

         // Ensure it's an EJB3 DU (by looking for the processed metadata)
         if (du.getAttachment(AttachmentNames.PROCESSED_METADATA, JBossMetaData.class) == null)
         {
            continue;
         }

         // Try to resolve
         jndiName = resolver.resolveEjb(du, reference);

         // If we've resolved here, we're done
         if (jndiName != null)
         {
            break;
         }
      }

      // Ensure we've got a resolved JNDI name
      if (jndiName == null)
      {
         throw new UnresolvableReferenceException("Could not resolve in current deployments reference: " + reference);
      }

      // Lookup 
      try
      {
         obj = getContext().lookup(jndiName);
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Could not obtain " + jndiName + "from JNDI", e);
      }

      // Return
      return obj;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   public EjbReference getReference()
   {
      return reference;
   }

   protected void setReference(EjbReference reference)
   {
      this.reference = reference;
   }

   protected EjbReferenceResolver getResolver()
   {
      return resolver;
   }

   protected void setResolver(EjbReferenceResolver resolver)
   {
      this.resolver = resolver;
   }

   protected Context getContext()
   {
      return context;
   }

   protected void setContext(Context context)
   {
      this.context = context;
   }
}
