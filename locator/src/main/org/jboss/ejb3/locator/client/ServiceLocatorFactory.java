/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.locator.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ServiceLocatorFactory
{

   // Class Members
   private static ServiceLocator serviceLocator = null;

   /*
    * Initialize Service Locator depending upon external configuration of JNDI
    * Hosts
    */
   static
   {
      // Obatin Metadata and parse
      // TODO Needs to follow rules, order of finding config file
      File jndiHostConfigFile = new File("IMPLEMENT.xml");
      try
      {
         ServiceLocatorFactory.serviceLocator = new JndiCachingServiceLocator(JndiHostMetadataParser.getInstance()
               .parse(new FileInputStream(jndiHostConfigFile)));
      }
      catch (FileNotFoundException e)
      {
         // TODO Add more elegant error message
         throw new ServiceLocatorException(e);
      }

   }

   /**
    * Obtains the Service Locator configured for this application
    * 
    * @return
    */
   public ServiceLocator getServiceLocator()
   {
      return ServiceLocatorFactory.serviceLocator;
   }
}
