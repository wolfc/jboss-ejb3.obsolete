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
package org.jboss.ejb3.profile3_1;

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.logging.Logger;

/**
 * Ejb3ComponentRegistry
 *
 * Temporary workaround to an issue where MC does not allow
 * static methods (of {@link Ejb3Registry}) to act as callbacks.
 * Ejb3ComponentRegistry acts as an indirection to {@link Ejb3Registry}
 * which maintains a registry of deployed EJB containers.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class Ejb3ComponentRegistry
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(Ejb3ComponentRegistry.class);

   /**
    * Adds the <code>container</code> to the {@link Ejb3Registry}
    *
    * @param container
    */
   public void addContainer(EJBContainer container)
   {
      // We need the Ejb3Registry to hold these references, since that's where
      // the IsLocalInterceptor looks for.
      Ejb3Registry.register(container);
      logger.debug("Container " + container + " added to registry");
   }

   /**
    * Unregisters (an registered) <code>container</code> from {@link Ejb3Registry}
    *
    * @param container
    */
   public void removeContainer(EJBContainer container)
   {
      // unregsiter the container
      // Bug in hasContainer implementation - The implementation uses the objectName of the container
      // instead of guid to check the internal map of containers. Effectively always returns false.
      // So let's skip this hasContainer check for now (risky, might throw exception if the container
      // wasn't registered)
      //         if (Ejb3Registry.hasContainer(container))
      //         {
      Ejb3Registry.unregister(container);
      logger.debug("Container " + container + " removed from registry");
   }
}
