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

import javax.naming.LinkRef;
import javax.naming.NamingException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class LinkRefEncInjector implements EncInjector
{
   private String jndiName;
   private String error;
   private String encName;

   public LinkRefEncInjector(String name, String mappedName, String error)
   {
      this.jndiName = mappedName;
      this.error = error;
      this.encName = name;
   }

   public void inject(InjectionContainer container)
   {
      try
      {
         Util.rebind(container.getEnc(), encName, new LinkRef(jndiName));
      }
      catch (NamingException e)
      {
         throw new RuntimeException(new StringBuilder().append("could not bind enc name '").append(encName).append("' for ").append(error).append(" for container ").append(container.getIdentifier()).append(e.getMessage()).toString());
      }
   }
}
