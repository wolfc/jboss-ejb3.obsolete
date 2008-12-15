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
package org.jboss.ejb3.mcint.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.util.graph.Graph;

/**
 * MockDeployerImpl
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MockDeployerImpl implements DeployerClient, MainDeployerStructure
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   private static final String MSG_MOCK = "Used in test mocking only";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /**
    * Holds all deployments
    */
   private Map<String, Deployment> deployments = new ConcurrentHashMap<String, Deployment>();

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#addDeployment(org.jboss.deployers.client.spi.Deployment)
    */
   public void addDeployment(Deployment deployment) throws DeploymentException
   {
      this.deployments.put(deployment.getName(), deployment);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#getDeployment(java.lang.String)
    */
   public Deployment getDeployment(String name)
   {
      return this.deployments.get(name);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#getTopLevel()
    */
   public Collection<Deployment> getTopLevel()
   {
      return Collections.synchronizedCollection(this.deployments.values());
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.main.MainDeployerStructure#getDeploymentUnit(java.lang.String)
    */
   public DeploymentUnit getDeploymentUnit(String name)
   {
      DeploymentContext context = this.getDeploymentContext(name);
      if (context == null)
      {
         return null;
      }
      return context.getDeploymentUnit();
   }
   
   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.main.MainDeployerStructure#getDeploymentContext(java.lang.String)
    */
   public DeploymentContext getDeploymentContext(String name)
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   // --------------------------------------------------------------------------------->>>>>>
   // --------------------------------------------------------------------------------->>>>>>
   // --------------------------------------------------------------------------------->>>>>>

   /*
    * Everything below this marker is not supported
    */

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#change(java.lang.String, org.jboss.deployers.spi.deployer.DeploymentStage)
    */
   public void change(String deploymentName, DeploymentStage stage) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#checkComplete()
    */
   public void checkComplete() throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#checkComplete(org.jboss.deployers.client.spi.Deployment[])
    */
   public void checkComplete(Deployment... deployment) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#checkComplete(java.lang.String[])
    */
   public void checkComplete(String... names) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#checkStructureComplete(org.jboss.deployers.client.spi.Deployment[])
    */
   public void checkStructureComplete(Deployment... deployments) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#checkStructureComplete(java.lang.String[])
    */
   public void checkStructureComplete(String... names) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#deploy(org.jboss.deployers.client.spi.Deployment[])
    */
   public void deploy(Deployment... deployments) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#getDeepManagedObjects(java.lang.String)
    */
   public Graph<Map<String, ManagedObject>> getDeepManagedObjects(String name) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#getDeploymentStage(java.lang.String)
    */
   public DeploymentStage getDeploymentStage(String deploymentName) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#getDeploymentState(java.lang.String)
    */
   public DeploymentState getDeploymentState(String name)
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#getManagedDeployment(java.lang.String)
    */
   public ManagedDeployment getManagedDeployment(String name) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#getManagedObjects(java.lang.String)
    */
   public Map<String, ManagedObject> getManagedObjects(String name) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#process()
    */
   public void process()
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#removeDeployment(org.jboss.deployers.client.spi.Deployment)
    */
   public boolean removeDeployment(Deployment deployment) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#removeDeployment(java.lang.String)
    */
   public boolean removeDeployment(String name) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#undeploy(org.jboss.deployers.client.spi.Deployment[])
    */
   public void undeploy(Deployment... deployments) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.DeployerClient#undeploy(java.lang.String[])
    */
   public void undeploy(String... names) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.main.MainDeployerStructure#getDeploymentContext(java.lang.String, boolean)
    */
   public DeploymentContext getDeploymentContext(String name, boolean errorNotFound) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.main.MainDeployerStructure#getDeploymentUnit(java.lang.String, boolean)
    */
   public DeploymentUnit getDeploymentUnit(String name, boolean errorNotFound) throws DeploymentException
   {
      throw new UnsupportedOperationException(MSG_MOCK);
   }

}
