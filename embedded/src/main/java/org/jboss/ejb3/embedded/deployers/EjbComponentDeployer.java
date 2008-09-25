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

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.embedded.deployment.EmbeddedDescriptorHandler;
import org.jboss.ejb3.javaee.JavaEEComponentHelper;
import org.jboss.ejb3.javaee.JavaEEModule;
import org.jboss.ejb3.javaee.SimpleJavaEEModule;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EjbComponentDeployer extends AbstractSimpleRealDeployer<JBossEnterpriseBeanMetaData>
{
   public EjbComponentDeployer()
   {
      super(JBossEnterpriseBeanMetaData.class);
      
      setComponentsOnly(true);
      
      addOutput(BeanMetaData.class);
   }

   protected void addDependencies(BeanMetaDataBuilder builder, DeploymentUnit unit, JBossEnterpriseBeanMetaData metaData)
   {
      // TODO: ask something else for that name
      builder.addDependency("org.jboss.ejb3.deployment:" + unit.getParent().getSimpleName());
   }
   
   @Override
   public void deploy(DeploymentUnit unit, JBossEnterpriseBeanMetaData metaData) throws DeploymentException
   {
      log.info("Found " + metaData + " in " + unit);
      
      JavaEEModule module = new SimpleJavaEEModule(unit.getParent().getSimpleName());
      //unit.getAttachment(JavaEEModule.class);
      
      String ejbName = metaData.getEjbName();
      String componentName = JavaEEComponentHelper.createObjectName(module, ejbName);
      
      Ejb3Deployment deployment = unit.getAttachment(Ejb3Deployment.class);
      
      EJBContainer component;
      try
      {
         EmbeddedDescriptorHandler handler = new EmbeddedDescriptorHandler(deployment, metaData.getEjbJarMetaData());
         component = handler.createEJBContainer(metaData);
      }
      catch(Exception e)
      {
         throw new DeploymentException(e);
      }
      
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(componentName, component.getClass().getName());
      builder.setConstructorValue(component);
      addDependencies(builder, unit, metaData);
      
      unit.addAttachment(BeanMetaData.class, builder.getBeanMetaData());
   }
}
