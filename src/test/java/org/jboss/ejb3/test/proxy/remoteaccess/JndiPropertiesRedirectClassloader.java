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
package org.jboss.ejb3.test.proxy.remoteaccess;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.jboss.logging.Logger;

/**
 * JndiPropertiesRedirectClassloader
 * 
 * Hacky classloader to use to prevent the JNP Server from
 * loading jndi.properties (which should be used by clients 
 * only) and instead swapping for jnpserver.properties
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class JndiPropertiesRedirectClassloader extends ClassLoader
{

   private static final String TO_REPLACE = "jndi.properties";

   private static final String REPLACE_WITH = "jnpserver.properties";

   private static final Logger log = Logger.getLogger(JndiPropertiesRedirectClassloader.class);

   /**
    * Replaces a request to load "jndi.properties" with "jnpserver.properties"
    */
   @Override
   public Enumeration<URL> getResources(String name) throws IOException
   {
      if (name.equals(JndiPropertiesRedirectClassloader.TO_REPLACE))
      {
         log.info("Replacing request for " + JndiPropertiesRedirectClassloader.TO_REPLACE + " with "
               + JndiPropertiesRedirectClassloader.REPLACE_WITH);
         name = JndiPropertiesRedirectClassloader.REPLACE_WITH;
      }
      return super.getResources(name);
   }

}
