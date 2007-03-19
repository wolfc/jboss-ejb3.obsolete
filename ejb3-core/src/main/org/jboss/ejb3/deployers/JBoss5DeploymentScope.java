/*
* JBoss, Home of Professional Open Source
* Copyright 2005, Red Hat Middleware LLC., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.deployers.spi.structure.DeploymentContext;
import org.jboss.deployers.spi.deployer.DeploymentUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstraction for an EAR/WAR or anything that scopes EJB deployments
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 55144 $
 */
public class JBoss5DeploymentScope implements DeploymentScope
{
   private ConcurrentHashMap<String, Ejb3Deployment> deployments;
   private String shortName;
   private String baseName;

   public JBoss5DeploymentScope(DeploymentContext parent)
   {
      // Use the root vfs path name
      this.shortName = parent.getRoot().getPathName();
      if( shortName.length() == 0 )
      {
         shortName = parent.getName();
         // this is a hack because VFS has gay URL name.
         if (shortName.endsWith("!/"))
         {
            this.shortName = shortName.substring(0, shortName.length() - 2);
         }
      }
      // Further reduce the path to the last component of the url name 
      int x = shortName.lastIndexOf('/');
      this.shortName = shortName.substring(x + 1);

      baseName = shortName;
      int idx = shortName.lastIndexOf('.');
      if( idx > 0 )
         baseName = shortName.substring(0, idx);
      deployments = (ConcurrentHashMap<String, Ejb3Deployment>)parent.getDeploymentUnit().getAttachment("EJB_DEPLOYMENTS");
      if (deployments == null)
      {
         deployments = new ConcurrentHashMap<String, Ejb3Deployment>();
         parent.getDeploymentUnit().addAttachment("EJB_DEPLOYMENTS", deployments);
      }
   }

   public Collection<Ejb3Deployment> getEjbDeployments()
   {
      return deployments.values();
   }

   public void register(Ejb3Deployment deployment)
   {
      deployments.put(deployment.getDeploymentUnit().getShortName(), deployment);
   }

   public void unregister(Ejb3Deployment deployment)
   {
      deployments.remove(deployment.getDeploymentUnit().getShortName());
   }

   public Ejb3Deployment findRelativeDeployment(String relativeName)
   {
      if (relativeName.startsWith("../"))
      {
         relativeName = relativeName.substring(3);
      }
      return deployments.get(relativeName);
   }

   public String getShortName()
   {
      return shortName;
   }

   public String getBaseName()
   {
      return baseName;
   }

}
