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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.ejb3.entity.PersistenceUnitDeployment;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
@Deprecated
public class PersistenceUnitRegistry
{
   private static final Logger log = Logger.getLogger(PersistenceUnitRegistry.class);

   private static ConcurrentHashMap<String, PersistenceUnitDeployment> persistenceUnits = new ConcurrentHashMap<String, PersistenceUnitDeployment>();

   public static void register(PersistenceUnitDeployment container)
   {
      if (persistenceUnits.contains(container.getKernelName())) throw new RuntimeException("Persistence Unit is already registered: " + container.getKernelName());
      persistenceUnits.put(container.getKernelName(), container);
   }

   public static void unregister(PersistenceUnitDeployment container)
   {
      persistenceUnits.remove(container.getKernelName());
   }

   public static PersistenceUnitDeployment getPersistenceUnit(String kernelName)
   {
      return persistenceUnits.get(kernelName);
   }

   public static Collection<PersistenceUnitDeployment> getPersistenceUnits()
   {
      return persistenceUnits.values();
   }

}
