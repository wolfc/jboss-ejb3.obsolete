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

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.DeploymentScope;

import javax.naming.NameNotFoundException;
import java.util.LinkedHashMap;

/**
 * Resolve EJBs.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class EjbModuleEjbResolver extends DeploymentEjbResolver
{
   protected LinkedHashMap ejbContainers;
   protected Ejb3Deployment deployment;

   public EjbModuleEjbResolver(DeploymentScope deploymentScope, String errorName, LinkedHashMap ejbContainers, Ejb3Deployment deployment)
   {
      super(deploymentScope, errorName);
      this.ejbContainers = ejbContainers;
      this.deployment = deployment;
   }

   @Override
   protected EJBContainer searchDeploymentInternally(String ejbLink, Class businessIntf)
   {
      for (Object obj : ejbContainers.values())
      {
         EJBContainer container = (EJBContainer) obj;
         if (container.getEjbName().equals(ejbLink))
         {
            return container;
         }
      }
      return null;
   }

   @Override
   protected EJBContainer searchForEjbContainerInternally(Class businessIntf) throws NameNotFoundException
   {
      EJBContainer rtnContainer;
      rtnContainer = getEjbContainer(deployment, businessIntf);
      return rtnContainer;
   }

}
