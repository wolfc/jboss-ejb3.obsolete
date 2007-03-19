/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.enc;

import org.jboss.ejb3.entity.PersistenceUnitDeployment;
import org.jboss.ejb3.PersistenceUnitRegistry;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.Ejb3Deployment;

import javax.naming.NameNotFoundException;
import java.util.List;
import java.util.LinkedHashMap;

/**
 * Resolves persistence units for @PersistenceContext and @PersistenceUnit
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class DeploymentPersistenceUnitResolver
{
   protected List<PersistenceUnitDeployment> persistenceUnitDeployments;
   protected DeploymentScope deploymentScope;
   protected LinkedHashMap ejbContainers;

   public DeploymentPersistenceUnitResolver(List<PersistenceUnitDeployment> persistenceUnitDeployments, DeploymentScope deploymentScope, LinkedHashMap ejbContainers)
   {
      this.persistenceUnitDeployments = persistenceUnitDeployments;
      this.deploymentScope = deploymentScope;
      this.ejbContainers = ejbContainers;
   }

   public PersistenceUnitDeployment getPersistenceUnitDeployment(String unitName) throws NameNotFoundException
   {
      if ("".equals(unitName))
      {
         if (persistenceUnitDeployments == null)
         {
            throw new NameNotFoundException("EMPTY STRING unitName but there is no deployments in scope");
         }
         if (persistenceUnitDeployments.size() == 1 && ejbContainers.size() > 0)
         {
            return persistenceUnitDeployments.get(0);
         }
         else if (persistenceUnitDeployments.size() > 1)
         {
            throw new NameNotFoundException("EMPTY STRING unitName and there is more than one scoped persistence unit");
         }
         throw new NameNotFoundException("There is no default persistence unit in this deployment.");
      }
      int hashIndex = unitName.indexOf('#');
      if (hashIndex != -1)
      {
         String relativePath = unitName.substring(0, hashIndex);
         String name = unitName.substring(hashIndex + 1);
         if (deploymentScope == null)
         {
            String relativeJarName = relativePath.substring(3);
            // look in global EJB jars.
            for (PersistenceUnitDeployment pud : PersistenceUnitRegistry.getPersistenceUnits())
            {
               String jarName = pud.getDeployment().getDeploymentUnit().getShortName() + ".jar";
               if (pud.getDeployment().getEar() == null
                       && jarName.equals(relativeJarName)
                       && pud.getEntityManagerName().equals(name)
                       && pud.isScoped())
               {
                  return pud;
               }
            }
            return null;
         }
         Ejb3Deployment dep = deploymentScope.findRelativeDeployment(relativePath);
         if (dep == null)
         {
            return null;
         }
         PersistenceUnitDeployment rtn = dep.getPersistenceUnitDeploymentInternal(name);
         return rtn;
      }
      PersistenceUnitDeployment rtn = getPersistenceUnitDeploymentInternal(unitName);
      if (rtn != null) return rtn;

      for (PersistenceUnitDeployment deployment : PersistenceUnitRegistry.getPersistenceUnits())
      {
         if (deployment.isScoped()) continue;
         if (deployment.getEntityManagerName().equals(unitName)) return deployment;
      }
      return rtn;
   }

   public PersistenceUnitDeployment getPersistenceUnitDeploymentInternal(String unitName)
   {
      if (persistenceUnitDeployments != null)
      {
         for (PersistenceUnitDeployment deployment : persistenceUnitDeployments)
         {
            if (deployment.getEntityManagerName().equals(unitName))
            {
               return deployment;
            }
         }
      }
      return null;
   }
}
