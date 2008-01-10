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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;

/**
 * Starts any initial EJB deployment created and initialized
 * by the EJBRegistrationDeployer
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision: 57082 $
 */
public class EJBStage2Deployer extends AbstractSimpleRealDeployer<Ejb3Deployment>
{
   /**
    * Create a new EJBStage2Deployer.
    */
   public EJBStage2Deployer()
   {
      super(Ejb3Deployment.class);
   }
   
   @Override
   public void deploy(DeploymentUnit unit, Ejb3Deployment deployment) throws DeploymentException
   {
      try
      {
         log.debug("********* EJBStage2 Begin Unit: " + unit.getName() + " jar: " + unit.getSimpleName());
         deployment.start();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void undeploy(DeploymentUnit unit, Ejb3Deployment deployment)
   {
      try
      {
         deployment.stop();
      }
      catch(Exception e)
      {
         log.warn("Failed to stop deployment " + deployment, e);
      }
   }
}
