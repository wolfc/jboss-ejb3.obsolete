package org.jboss.ejb3.servicelocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ServiceLocatorFactory
{

   // Class Members
   private static ServiceLocator serviceLocator = null;

   /*
    * Initialize Service Locator depending upon 
    * external configuration of JNDI Hosts
    */
   static
   {
      // Obatin Metadata and parse
      //TODO Needs to follow rules, order of finding config file
      File jndiHostConfigFile = new File("IMPLEMENT.xml");
      try
      {
         ServiceLocatorFactory.serviceLocator = new JndiCachingServiceLocator(JndiHostMetadataParser.getInstance()
               .parse(new FileInputStream(jndiHostConfigFile)));
      }
      catch (FileNotFoundException e)
      {
         //TODO Add more elegant error message
         throw new ServiceLocatorException(e);
      }

   }

   /**
    * Obtains the Service Locator configured for this application
    * @return
    */
   public ServiceLocator getServiceLocator()
   {
      return ServiceLocatorFactory.serviceLocator;
   }
}
