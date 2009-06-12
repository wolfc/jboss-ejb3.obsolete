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
package org.jboss.ejb3.endpoint.deployers;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.endpoint.deployers.impl.EndpointImpl;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EJB3EndpointDeployer extends AbstractSimpleRealDeployer<JBossMetaData>
{
   private EJBIdentifier identifier;
   private EndpointResolver resolver;

   public EJB3EndpointDeployer()
   {
      super(JBossMetaData.class);
      setOutput(BeanMetaData.class);
   }

   @Override
   public void deploy(DeploymentUnit unit, JBossMetaData metaData) throws DeploymentException
   {
      if (!metaData.isEJB3x())
      {
         return;
      }
      JBossEnterpriseBeansMetaData beans = metaData.getEnterpriseBeans();
      for (JBossEnterpriseBeanMetaData bean : beans)
      {
         if (bean.isSession())
         {
            log.debug("found bean " + bean);
            // Create view for each bean
            deploy(unit, (JBossSessionBeanMetaData) bean);
         }
      }
   }

   protected void deploy(DeploymentUnit unit, JBossSessionBeanMetaData beanMetaData) throws DeploymentException
   {
      String ejbName = beanMetaData.getEjbName();
      String ejbBeanName = identifier.identifyEJB(unit, ejbName);
      String name = resolver.resolve(unit, ejbName);
      BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(name, EndpointImpl.class.getName());
      builder.addPropertyMetaData("container", builder.createInject(ejbBeanName));
      BeanMetaData bmd = builder.getBeanMetaData();
      log.debug("Deploy " + bmd);
      unit.addAttachment(BeanMetaData.class + ":" + name, bmd);
   }
   
   @Inject
   public void setEJBIdentifier(EJBIdentifier identifier)
   {
      this.identifier = identifier;
   }
   
   @Inject
   public void setEndpointResolver(EndpointResolver resolver)
   {
      this.resolver = resolver;
   }
   
   @Override
   public void undeploy(DeploymentUnit unit, JBossMetaData metaData)
   {
      JBossEnterpriseBeansMetaData beans = metaData.getEnterpriseBeans();
      for (JBossEnterpriseBeanMetaData bean : beans)
      {
         if (bean.isSession())
         {
            log.debug("found bean " + bean);
            // Create view for each bean
            undeploy(unit, (JBossSessionBeanMetaData) bean);
         }
      }
   }

   protected void undeploy(DeploymentUnit unit, JBossSessionBeanMetaData bean)
   {
   }
}
