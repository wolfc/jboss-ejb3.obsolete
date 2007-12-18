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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class ServiceLocatorFactory
{

   // Class Members
   private static JndiServiceLocator serviceLocator = null;
   
   private static final String CONFIGURATION_FILE_USER_OVERRIDE_FILENAME_SYSTEM_PROPERTY_KEY = "jboss.servicelocator.location";
   
   private static final String CONFIGURATION_FILE_USER_OVERRIDE_JAR_SYSTEM_PROPERTY_KEY = "jboss.servicelocator.classloader";

   private static final String CONFIGURATION_FILE_DEFAULT_FILENAME = "jboss-servicelocator.xml";

   private static final String CONFIGURATION_FILE_DEFAULT_INCONTAINER_URI = System
         .getProperty("jboss.server.config.url")
         + ServiceLocatorFactory.CONFIGURATION_FILE_DEFAULT_FILENAME;

   private static final String CONFIGURATION_FILE_DEFAULT_OUTCONTAINER_LOCATION = "META-INF/"
         + ServiceLocatorFactory.CONFIGURATION_FILE_DEFAULT_FILENAME;

   /*
    * Initialize Service Locator depending upon external configuration of JNDI
    * Hosts
    */
   static
   {

      // Initialize
      InputStream configuration = null;

      // Attempt to obtain default in-container file
      try
      {
         configuration = new FileInputStream(new File(new URI(
               ServiceLocatorFactory.CONFIGURATION_FILE_DEFAULT_INCONTAINER_URI)));
      }
      catch (FileNotFoundException fnfe)
      {
         // Not defined as default in-container location
      }
      catch (URISyntaxException e)
      {
         throw new ServiceLocatorException(e);
      }

      // Not found as default in-container file, attempt for default in-JAR file
      configuration = Thread.currentThread().getContextClassLoader().getResourceAsStream(
            ServiceLocatorFactory.CONFIGURATION_FILE_DEFAULT_OUTCONTAINER_LOCATION);
      
      // If default in-JAR file is not found
      if(configuration==null)
      {
         //TODO
      }

      // Parse
      ServiceLocatorFactory.serviceLocator = new Ejb3ServiceLocatorImpl(JndiHostConfigurationParser.getInstance()
            .parse(configuration));
   }

   /**
    * Obtains the Service Locator configured for this application
    * 
    * @return
    */
   public JndiServiceLocator getServiceLocator()
   {
      return ServiceLocatorFactory.serviceLocator;
   }
}
