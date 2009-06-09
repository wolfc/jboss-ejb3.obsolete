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
package org.jboss.ejb3.metrics.deployer;

import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperties;

/**
 * ManagedStatefulInvocationStatisticsWrapper
 * 
 * @ManagementObject wrapper to expose SFSB invocation statistics
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@ManagementObject(isRuntime = true, properties = ManagementProperties.EXPLICIT, description = "Stateful Session Bean Invocation Metrics", componentType = @ManagementComponent(type = "EJB3", subtype = "SFSB"))
public class ManagedStatefulInvocationStatisticsWrapper extends ManagedInvocationStatisticsSessionWrapperBase
{

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param delegate
    * @throws IllegalArgumentException If the delegate was not supplied
    */
   public ManagedStatefulInvocationStatisticsWrapper(final InvocationStatistics delegate)
         throws IllegalArgumentException
   {
      super(delegate);
   }

}
