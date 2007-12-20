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
package org.jboss.injection;

import org.jboss.naming.Util;

import javax.naming.NameNotFoundException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class PuEncInjector implements EncInjector
{
   private String encName;
   private Class injectionType;
   private String unitName;
   private String error;

   public PuEncInjector(String encName, Class injectionType, String unitName, String error)
   {
      this.encName = encName;
      this.injectionType = injectionType;
      this.error = error;
      this.unitName = unitName;
   }

   public void inject(InjectionContainer container)
   {
      Object factory = null;
      try
      {
         factory = PersistenceUnitHandler.getFactory(injectionType, unitName, container);
      }
      catch (NameNotFoundException e)
      {
         throw new RuntimeException(e);
      }
      if (factory == null)
      {
         throw new RuntimeException("Failed to locate " + error + " of unit name: " + unitName + " for " + container.getIdentifier());
      }

      try
      {
         Util.rebind(container.getEnc(), encName, factory);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to bind " + error + " of unit name: " + unitName + " ref-name" + encName + " for container " + container.getIdentifier(), e);
      }
   }
}
