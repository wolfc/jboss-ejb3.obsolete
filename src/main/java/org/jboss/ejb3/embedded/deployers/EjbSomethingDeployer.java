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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * I still don't get AbstractComponentDeployer.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Deprecated
public class EjbSomethingDeployer extends AbstractComponentDeployer<JBossMetaData, JBossEnterpriseBeanMetaData>
{
   public EjbSomethingDeployer()
   {
      setComponentVisitor(new EjbComponentVisitor());
      setDeploymentVisitor(new EjbDeploymentVisitor());
   }
   
   // TODO: what is this thing?
   private class EjbComponentVisitor implements DeploymentVisitor<JBossEnterpriseBeanMetaData>
   {
      public void deploy(DeploymentUnit unit, JBossEnterpriseBeanMetaData deployment) throws DeploymentException
      {
         System.err.println("Found " + deployment);
      }

      public Class<JBossEnterpriseBeanMetaData> getVisitorType()
      {
         return JBossEnterpriseBeanMetaData.class;
      }

      public void undeploy(DeploymentUnit unit, JBossEnterpriseBeanMetaData deployment)
      {
      }
   }
   
   private static class EjbDeploymentVisitor implements DeploymentVisitor<JBossMetaData>
   {
      public void deploy(DeploymentUnit unit, JBossMetaData deployment) throws DeploymentException
      {
         for(JBossEnterpriseBeanMetaData bean : deployment.getEnterpriseBeans())
         {
            DeploymentUnit component = unit.addComponent(bean.getEjbName());
            component.addAttachment(JBossEnterpriseBeanMetaData.class, bean);
         }
      }

      public Class<JBossMetaData> getVisitorType()
      {
         return JBossMetaData.class;
      }

      public void undeploy(DeploymentUnit unit, JBossMetaData deployment)
      {
         for(JBossEnterpriseBeanMetaData bean : deployment.getEnterpriseBeans())
         {
            unit.removeComponent(bean.getEjbName());
         }
      }
   }
}
