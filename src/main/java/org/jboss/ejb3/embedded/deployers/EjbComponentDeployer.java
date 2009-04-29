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

import java.util.Collections;
import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.DemandMetaData;
import org.jboss.beans.metadata.spi.DependencyMetaData;
import org.jboss.beans.metadata.spi.SupplyMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.MCDependencyPolicy;
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
   private Set<String> additionalContainerDependencies;
   
   public EjbComponentDeployer()
   {
      super(JBossEnterpriseBeanMetaData.class);
      
      setComponentsOnly(true);
      
      addOutput(BeanMetaData.class);
   }

   protected void addDependencies(BeanMetaDataBuilder builder, DeploymentUnit unit, EJBContainer component)
   {
      for(String dependency : additionalContainerDependencies)
      {
         builder.addDependency(dependency);
      }
      
      // TODO: ask something else for that name
      builder.addDependency("org.jboss.ejb3.deployment:" + unit.getParent().getSimpleName());
      
      // Hmm, should not cast, EjbDeployment knows the type
      MCDependencyPolicy dependencyPolicy = (MCDependencyPolicy) component.getDependencyPolicy();
      
      // Translating back and forth, could be done in 1 step.
      for(DemandMetaData demand : dependencyPolicy.getDemands())
         builder.addDemand(demand.getDemand());
      for(DependencyMetaData dependency : dependencyPolicy.getDependencies())
         builder.addDependency(dependency.getDependency());
      for(SupplyMetaData supply : dependencyPolicy.getSupplies())
         builder.addSupply(supply.getSupply());
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
      addDependencies(builder, unit, component);
      
      DeploymentUnit parent = unit.getParent();
      assert parent != null : "parent should not be null of component " + unit;
      
      BeanMetaData bmd = builder.getBeanMetaData();
      
      log.debug("deploying bean: " + bmd.getName());
      log.debug("  with dependencies:");
      for (DependencyMetaData dependency : in(bmd.getDepends()))
      {
         log.debug("\t" + dependency.getDependency());
      }
      log.debug("  and demands:");
      for(DemandMetaData dmd : in(bmd.getDemands()))
      {
         log.debug("\t" + dmd.getDemand());
      }
      log.debug("  and supplies:");
      for(SupplyMetaData smd : in(bmd.getSupplies()))
      {
         log.debug("\t" + smd.getSupply());
      }
      
      // add the bean meta data to the parent, because else scope merging won't occur (whatever that is)
      // (e.g. the bean won't get injected)
      parent.addAttachment(BeanMetaData.class + ":" + componentName, bmd);
   }
   
   private static <T> Set<T> in(Set<T> c)
   {
      if(c != null) return c;
      return Collections.emptySet();
   }
   
   public void setAdditionalContainerDependencies(Set<String> s)
   {
      this.additionalContainerDependencies = s;
   }
}
