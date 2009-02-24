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
package org.jboss.ejb3.nointerface.test.deployers;

import javax.security.jacc.PolicyConfiguration;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactoryRegistry;
import org.jboss.ejb3.deployers.JBoss5DependencyPolicy;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.pool.PoolFactoryRegistry;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EjbDeployment extends Ejb3Deployment
{
   /**
    * @param deploymentUnit
    * @param unit
    * @param deploymentScope
    * @param metaData
    */
   public EjbDeployment(org.jboss.deployers.structure.spi.DeploymentUnit deploymentUnit, DeploymentUnit unit,
         DeploymentScope deploymentScope, JBossMetaData metaData)
   {
      super(deploymentUnit, unit, deploymentScope, metaData);
   }

   @Override
   public void create() throws Exception
   {
      // make sure we don't invoke legacy stuff
      System.err.println("******************");
   }

   public DependencyPolicy createDependencyPolicy(JavaEEComponent component)
   {
      return new JBoss5DependencyPolicy(component);
   }

   protected PolicyConfiguration createPolicyConfiguration() throws Exception
   {
      throw new RuntimeException("NYI");
   }

   @Override
   public void destroy()
   {
      // make sure we don't invoke legacy stuff
   }

   protected void putJaccInService(PolicyConfiguration pc, DeploymentUnit unit)
   {
      throw new RuntimeException("NYI");
   }

   @Inject
   @Override
   public void setCacheFactoryRegistry(CacheFactoryRegistry registry)
   {
      super.setCacheFactoryRegistry(registry);
   }

   @Inject
   @Override
   public void setPoolFactoryRegistry(PoolFactoryRegistry poolFactoryRegistry)
   {
      super.setPoolFactoryRegistry(poolFactoryRegistry);
      System.out.println("Set pool factory");
   }

   @Inject
   @Override
   public void setPersistenceManagerFactoryRegistry(PersistenceManagerFactoryRegistry registry)
   {
      super.setPersistenceManagerFactoryRegistry(registry);
   }

   @Override
   public void start() throws Exception
   {
      // make sure we don't invoke legacy stuff
   }

   @Override
   public void stop()
   {
      // make sure we don't invoke legacy stuff
   }
}
