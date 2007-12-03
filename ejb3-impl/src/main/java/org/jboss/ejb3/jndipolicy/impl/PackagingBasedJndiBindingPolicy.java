/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.jndipolicy;

import org.jboss.ejb3.Ejb3DeploymentSummary;

/**
 * The JBoss Default JNDI Binding Policy
 * 
 * Determines JNDI name of EJBs based on packaging structure, 
 * EJB name, and local/remote designation
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class PackagingBasedJndiBindingPolicy implements DefaultJndiBindingPolicy
{
   // Class Members

   private static final String JNDI_SUFFIX_REMOTE = "/remote";

   private static final String JNDI_SUFFIX_LOCAL = "/local";

   private static final String JNDI_SUFFIX_REMOTE_HOME = "/home";

   private static final String JNDI_SUFFIX_LOCAL_HOME = "/localHome";

   // Required Implementations

   public String getJndiName(Ejb3DeploymentSummary summary)
   {
      // If Remote / Remote Business
      if (!summary.isLocal())
      {
         // If Home
         if (summary.isHome())
         {
            return this.getDefaultRemoteHomeJndiName(summary);
         }
         // Business / Remote
         else
         {
            return this.getDefaultRemoteJndiName(summary);
         }

      }
      else
      {
         if (summary.isHome())
         {
            return this.getDefaultLocalHomeJndiName(summary);
         }
         else
         {
            return this.getDefaultLocalJndiName(summary);
         }
      }
   }

   public String getDefaultRemoteJndiName(Ejb3DeploymentSummary summary)
   {
      
      String name = summary.getEjbName() + PackagingBasedJndiBindingPolicy.JNDI_SUFFIX_REMOTE;
      if (summary.getDeploymentScopeBaseName() != null)
         name = summary.getDeploymentScopeBaseName() + "/" + name;
      return name;
   }

   public String getDefaultRemoteHomeJndiName(Ejb3DeploymentSummary summary)
   {
      return summary.getEjbName() + PackagingBasedJndiBindingPolicy.JNDI_SUFFIX_REMOTE_HOME;
   }

   public String getDefaultLocalHomeJndiName(Ejb3DeploymentSummary summary)
   {
      return summary.getEjbName() + PackagingBasedJndiBindingPolicy.JNDI_SUFFIX_LOCAL_HOME;
   }

   public String getDefaultLocalJndiName(Ejb3DeploymentSummary summary)
   {
      String name = summary.getEjbName() + PackagingBasedJndiBindingPolicy.JNDI_SUFFIX_LOCAL;
      if (summary.getDeploymentScopeBaseName() != null)
         name = summary.getDeploymentScopeBaseName() + "/" + name;
      return name;
   }

}
