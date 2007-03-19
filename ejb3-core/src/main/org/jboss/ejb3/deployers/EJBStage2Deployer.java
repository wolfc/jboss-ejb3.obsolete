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

import org.jboss.deployers.plugins.deployer.AbstractSimpleDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.logging.Logger;

/**
 * Starts any initial EJB deployment created and initialized
 * by the EJBRegistrationDeployer
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57082 $
 */
public class EJBStage2Deployer extends AbstractSimpleDeployer
{
   private static final Logger log = Logger.getLogger(EJBStage2Deployer.class);

   public EJBStage2Deployer()
   {
      // make sure we run at right moment (before EJB3 client deployer)
      setRelativeOrder(COMPONENT_DEPLOYER);
   }
   
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      Ejb3Deployment deployment = unit.getAttachment(Ejb3Deployment.class);
      if (deployment == null) return;
      try
      {
         log.debug("********* EJBStage2 Begin Unit: " + unit.getName() + " jar: " + unit.getDeploymentContext().getRoot().getName());
         deployment.start();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
