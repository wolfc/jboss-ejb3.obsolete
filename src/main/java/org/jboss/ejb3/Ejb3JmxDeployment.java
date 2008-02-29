/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3;

import javax.security.jacc.PolicyConfiguration;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb3.javaee.JavaEEComponent; 
import org.jboss.logging.Logger;

/**
 * JBoss 4.0 Microkernel specific implementation
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Deprecated
public class Ejb3JmxDeployment extends Ejb3Deployment
{
   private DeploymentInfo deploymentInfo;

   private static final Logger log = Logger.getLogger(Ejb3JmxDeployment.class);

   public Ejb3JmxDeployment(DeploymentInfo di, DeploymentScope deploymentScope)
   {
      super(new JmxDeploymentUnit(di), deploymentScope, null, null);
      this.deploymentInfo = di;
      kernelAbstraction = new JmxKernelAbstraction(di);

      // todo maybe mbeanServer should be injected?
      mbeanServer = di.getServer();
   }

   protected PolicyConfiguration createPolicyConfiguration() throws Exception
   {
	   throw new IllegalStateException("This method should not be called"); 	   
   }

   protected void putJaccInService(PolicyConfiguration pc, DeploymentUnit unit)
   {
	   throw new IllegalStateException("This method should not be called"); 
   }

   public DependencyPolicy createDependencyPolicy(JavaEEComponent component)
   {
      return new JmxDependencyPolicy();
   }


}
