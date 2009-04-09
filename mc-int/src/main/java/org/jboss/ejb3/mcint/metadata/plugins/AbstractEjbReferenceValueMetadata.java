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

import java.util.Collection;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.AbstractDependencyValueMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.common.deployers.spi.AttachmentNames;
import org.jboss.ejb3.common.deployers.spi.Ejb3DeployerUtils;
import org.jboss.ejb3.common.resolvers.spi.EjbReference;
import org.jboss.ejb3.common.resolvers.spi.EjbReferenceResolver;
import org.jboss.ejb3.common.resolvers.spi.UnresolvableReferenceException;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.reflect.spi.TypeInfo;

/**
 * AbstractEjbReferenceValueMetadata
 *
 * Describes both the requisite JNDI dependency and target Proxy for injection
 * based upon a supplied resolver, reference, and naming context.  Will
 * search through all eligible EJB3 deployments available from the
 * MainDeployer
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AbstractEjbReferenceValueMetadata extends AbstractDependencyValueMetaData
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final String DEPENDS_JNDI_PREFIX = "jndi:";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private EjbReferenceResolver resolver;

   private EjbReference reference;

   private Context namingContext;

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
      this.setNamingContext(context);
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

   /**
    * Used in defining the dependency
    */
   @Override
   public Object getUnderlyingValue()
   {
      return DEPENDS_JNDI_PREFIX + this.getTargetJndiName();
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the target JNDI name, whose value is to be injected
    *
    * @return The target JNDI Name
    */
   protected String getTargetJndiName()
   {
      // Initialize
      String jndiName = null;

      /*
       * Look through all EJB3 DeploymentUnits
       */

      // Get all EJB3 DUs
      Collection<DeploymentUnit> dus = Ejb3DeployerUtils.getAllEjb3DeploymentUnitsInMainDeployer();

      // Loop through each DeploymentUnit
      if (dus != null)
      {
         for (DeploymentUnit du : dus)
         {
            // Ensure it's an EJB3 DU (by looking for the processed metadata)
            if (du.getAttachment(AttachmentNames.PROCESSED_METADATA, JBossMetaData.class) == null)
            {
               continue;
            }

            try
            {
               // Try to resolve
               jndiName = resolver.resolveEjb(du, reference);

               // If we've resolved here, we're done
               if (jndiName != null)
               {
                  break;
               }

            }
            catch (UnresolvableReferenceException urre)
            {
               // we could not resolve in this unit, let's try the next unit
               if (log.isTraceEnabled())
               {
                  log.trace("EJB reference " + reference + " could not be resolved in unit " + du + " - trying next unit");
               }
               continue;
            }
         }
      }

      // Ensure we've got a resolved JNDI name
      if (jndiName == null)
      {
         throw new UnresolvableReferenceException("Could not resolve in current deployments reference: " + reference);
      }

      // Return the JNDI Name
      return jndiName;
   }

   /**
    * Obtains the EJB Proxy from JNDI based upon the resolved JNDI name
    *
    * @return The Proxy to inject
    */
   protected Object resolveEjb()
   {
      // Initialize
      Object obj = null;
      String jndiName = this.getTargetJndiName();

      // Lookup
      try
      {
         obj = getNamingContext().lookup(jndiName);
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Could not obtain " + jndiName + "from JNDI", e);
      }

      this.addDependencyItem();

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

   protected Context getNamingContext()
   {
      return namingContext;
   }

   protected void setNamingContext(Context context)
   {
      this.namingContext = context;
   }

}
