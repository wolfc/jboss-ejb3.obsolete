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

import org.jboss.logging.Logger;
import org.jboss.naming.Util;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class EnvEntryEncInjector implements EncInjector
{
   private static final Logger log = Logger.getLogger(EnvEntryEncInjector.class);

   private String name;
   private String entryType;
   private String value;


   public EnvEntryEncInjector(String encName, String entryType, String value)
   {
      this.name = encName;
      this.entryType = entryType;
      this.value = value;
   }

   public void inject(InjectionContainer container)
   {
      try
      {
         Util.rebind(container.getEnc(),
                 name,
                 getEnvEntryValue());
      }
      catch (Exception e)
      {
         throw new RuntimeException("Invalid <env-entry> name: " + name, e);
      }
   }


   protected Object getEnvEntryValue() throws ClassNotFoundException
   {
      Class type = Thread.currentThread().getContextClassLoader().loadClass(entryType);
      if (type == String.class)
      {
         return value;
      }
      else if (type == Integer.class)
      {
         return new Integer(value);
      }
      else if (type == Long.class)
      {
         return new Long(value);
      }
      else if (type == Double.class)
      {
         return new Double(value);
      }
      else if (type == Float.class)
      {
         return new Float(value);
      }
      else if (type == Byte.class)
      {
         return new Byte(value);
      }
      else if (type == Character.class)
      {
         String input = value;
         if (input == null || input.length() == 0)
         {
            return new Character((char) 0);
         }
         else
         {
            if (input.length() > 1)
               // TODO: Add deployment context
               log.warn("Warning character env-entry is too long: binding="
                       + name + " value=" + input);
            return new Character(input.charAt(0));
         }
      }
      else if (type == Short.class)
      {
         return new Short(value);
      }
      else if (type == Boolean.class)
      {
         return new Boolean(value);
      }
      else
      {
         return value;
      }
   }
}
