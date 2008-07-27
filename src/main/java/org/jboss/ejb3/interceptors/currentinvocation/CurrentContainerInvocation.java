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
package org.jboss.ejb3.interceptors.currentinvocation;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aspects.currentinvocation.CurrentInvocation;
import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;
import org.jboss.logging.Logger;

/**
 * CurrentContainerInvocation
 * 
 * A simple extension to CurrentInvocation which handles casting to
 * EJB3 ContainerMethodInvocation as a convenience
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class CurrentContainerInvocation extends CurrentInvocation
{
   private static final Logger log = Logger.getLogger(CurrentContainerInvocation.class);

   // -------------------------------------------------------------------------------------------------||
   // Functional Methods ------------------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------------------||

   /**
    * Retrieves the ContainerMethodInvocation associated with the current
    * Thread / request if one is associated.  Returns null
    * otherwise
    * 
    *  @return
    */
   public static ContainerMethodInvocation getCurrentInvocation()
   {
      // Obtain current invocation
      Invocation currentInvocation = CurrentInvocation.getCurrentInvocation();

      // Cast and return
      try
      {
         return ContainerMethodInvocation.class.cast(currentInvocation);
      }
      catch (ClassCastException cce)
      {
         throw new RuntimeException("Current invocation is not of type " + ContainerMethodInvocation.class.getName()
               + " but instead is " + currentInvocation.getClass().getName(), cce);
      }
   }
}
