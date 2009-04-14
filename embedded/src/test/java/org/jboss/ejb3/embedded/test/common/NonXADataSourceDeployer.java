/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.embedded.test.common;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.jpa.resolvers.DataSourceDependencyResolver;
import org.jboss.metadata.rar.jboss.mcf.NonXADataSourceDeploymentMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class NonXADataSourceDeployer extends AbstractSimpleRealDeployer<NonXADataSourceDeploymentMetaData>
{
   private DataSourceDependencyResolver resolver;
   
   public NonXADataSourceDeployer()
   {
      super(NonXADataSourceDeploymentMetaData.class);
      
      addOutput(BeanMetaData.class);
   }

   @Override
   public void deploy(DeploymentUnit unit, NonXADataSourceDeploymentMetaData deployment) throws DeploymentException
   {
      String jndiName = deployment.getJndiName();
      String beanName = resolver.resolveDataSourceSupplier(jndiName);
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, deployment.getDriverClass())
         .addPropertyMetaData("jndiName", jndiName)
         .addPropertyMetaData("URL", deployment.getConnectionUrl())
         .addPropertyMetaData("user", deployment.getUserName())
         .addPropertyMetaData("password", deployment.getPassWord().toCharArray());
      unit.addAttachment(BeanMetaData.class, builder.getBeanMetaData());
   }
   
   @Inject
   public void setDataSourceDependencyResolver(DataSourceDependencyResolver resolver)
   {
      this.resolver = resolver;
   }
}
