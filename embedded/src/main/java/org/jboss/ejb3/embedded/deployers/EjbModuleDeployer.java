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

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployerWithInput;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.embedded.deployment.EjbDeployment;
import org.jboss.ejb3.embedded.deployment.EmbeddedEjb3DeploymentUnit;
import org.jboss.jpa.resolvers.PersistenceUnitDependencyResolver;
import org.jboss.kernel.Kernel;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EjbModuleDeployer extends AbstractRealDeployerWithInput<JBossMetaData>
{
   @Deprecated
   private Kernel kernel;
   
   private PersistenceUnitDependencyResolver persistenceUnitDependencyResolver;
   
   public EjbModuleDeployer()
   {
      //addInput(EjbMetadataJndiPolicyDecoratorDeployer.EJB_DECORATED_FLAG);
      
      setDeploymentVisitor(new EjbDeploymentVisitor());
      
      addOutput(JBossEnterpriseBeanMetaData.class);
      addOutput(BeanMetaData.class);
   }
   
   private static Ejb3Deployment createModule(DeploymentUnit unit, JBossMetaData metaData)
   {
      org.jboss.ejb3.DeploymentUnit ejb3Unit = new EmbeddedEjb3DeploymentUnit(unit);
      DeploymentScope scope = null;
      return new EjbDeployment(unit, ejb3Unit, scope, metaData);
   }
   
   private class EjbDeploymentVisitor implements DeploymentVisitor<JBossMetaData>
   {
      public void deploy(DeploymentUnit unit, JBossMetaData metaData) throws DeploymentException
      {
         log.info("Found " + metaData + " in " + unit);
         JBossMetaData realMetaData = unit.getTransientManagedObjects().getAttachment(JBossMetaData.class);
         if(realMetaData != null && realMetaData != metaData)
         {
            metaData = realMetaData;
            log.info("but it's really " + metaData);
         }
         
         // FIXME
         if(metaData.getEnterpriseBeans() == null)
         {
            log.warn(unit + " contains no beans");
            return;
         }
         
         Ejb3Deployment module = createModule(unit, metaData);
         
         // ejb3-core builds its dependencies with runtime components. Since deployment
         // isn't ready yet, we'll inject it.
         module.setPersistenceUnitDependencyResolver(persistenceUnitDependencyResolver);
         
         unit.addAttachment(Ejb3Deployment.class, module);
         
         String name = "org.jboss.ejb3.deployment:" + unit.getSimpleName();
         BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(name, module.getClass().getName());
         builder.setConstructorValue(module);
         unit.addAttachment(BeanMetaData.class + ":" + name, builder.getBeanMetaData());
         
         for(JBossEnterpriseBeanMetaData bean : metaData.getEnterpriseBeans())
         {
            DeploymentUnit component = unit.addComponent(bean.getEjbName());
            component.addAttachment(JBossEnterpriseBeanMetaData.class, bean);
         }
      }

      public Class<JBossMetaData> getVisitorType()
      {
         return JBossMetaData.class;
      }

      public void undeploy(DeploymentUnit unit, JBossMetaData metaData)
      {
         // FIXME
         if(metaData.getEnterpriseBeans() == null)
         {
            return;
         }
         
         for(JBossEnterpriseBeanMetaData bean : metaData.getEnterpriseBeans())
         {
            unit.removeComponent(bean.getEjbName());
         }
         unit.removeAttachment(Ejb3Deployment.class);
      }
   }
   
   @Inject(bean="jboss.kernel:service=Kernel")
   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }
   
   @Inject
   public void setPersistenceUnitDependencyResolver(PersistenceUnitDependencyResolver resolver)
   {
      this.persistenceUnitDependencyResolver = resolver;
   }
}
