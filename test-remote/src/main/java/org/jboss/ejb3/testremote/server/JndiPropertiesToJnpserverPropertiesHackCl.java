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
package org.jboss.ejb3.testremote.server;

/**
 * JndiPropertiesToJnpserverPropertiesHackCl
 * 
 * Hacky classloader to use to prevent the JNP Server from
 * loading jndi.properties (which should be used by clients 
 * only) and instead swapping for jnpserver.properties
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class JndiPropertiesToJnpserverPropertiesHackCl extends RedirectClassloader
{

   private static final String TO_REPLACE = "jndi.properties";

   private static final String REPLACE_WITH = "jnpserver.properties";

   @Override
   protected String getFrom()
   {
      return JndiPropertiesToJnpserverPropertiesHackCl.TO_REPLACE;
   }

   @Override
   protected String getTo()
   {
      return JndiPropertiesToJnpserverPropertiesHackCl.REPLACE_WITH;
   }

}
