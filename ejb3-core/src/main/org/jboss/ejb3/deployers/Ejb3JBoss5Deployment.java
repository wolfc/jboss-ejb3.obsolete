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

import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.JmxDependencyPolicy;
import org.jboss.ejb3.MCKernelAbstraction;
import org.jboss.ejb3.MCDependencyPolicy;
import org.jboss.ejb3.security.JaccHelper;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;

import javax.management.MBeanServer;
import javax.security.jacc.PolicyConfiguration;

/**
 * JBoss 4.0 Microkernel specific implementation
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 56592 $
 */
public class Ejb3JBoss5Deployment extends Ejb3Deployment
{
   private org.jboss.deployers.spi.deployer.DeploymentUnit deploymentInfo;

   private static final Logger log = Logger.getLogger(Ejb3JBoss5Deployment.class);

   public Ejb3JBoss5Deployment(DeploymentUnit du, Kernel kernel, MBeanServer mbeanServer, org.jboss.deployers.spi.deployer.DeploymentUnit di, DeploymentScope deploymentScope)
   {
      super(du, deploymentScope);
      this.deploymentInfo = di;
      kernelAbstraction = new MCKernelAbstraction(kernel, mbeanServer);

      // todo maybe mbeanServer should be injected?
      this.mbeanServer = mbeanServer;
   }

   protected PolicyConfiguration createPolicyConfiguration() throws Exception
   {
      return JaccHelper.initialiseJacc(getJaccContextId());

   }

   protected void putJaccInService(PolicyConfiguration pc, DeploymentUnit unit)
   {
      try
      {
         JaccHelper.putJaccInService(pc, deploymentInfo);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

   public DependencyPolicy createDependencyPolicy()
   {
      return new JBoss5DependencyPolicy();
   }


}
