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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.GenericValidator;
import org.xml.sax.SAXException;

public class JndiHostConfigurationParser
{

   // Class Members
   private static final Log logger = LogFactory.getLog(JndiHostConfigurationParser.class);

   private static JndiHostConfigurationParser instance = null;

   public static final String DEFAULT_JNDI_HOST_ID_PREFIX = "JNDIHOST-";

   // Internal Constructor
   private JndiHostConfigurationParser()
   {

   }

   // Singleton Accessor
   public static synchronized JndiHostConfigurationParser getInstance()
   {
      // Ensure instanciated
      if (JndiHostConfigurationParser.instance == null)
      {
         JndiHostConfigurationParser.instance = new JndiHostConfigurationParser();
      }

      // Return
      return JndiHostConfigurationParser.instance;
   }

   // Functional Methods

   @SuppressWarnings(value = "unchecked")
   public Map<String,JndiHost> parse(InputStream inStream)
   {
      // Initialize
      Digester jndiHostDefinitionsDigester = new Digester();
      // Add Rules for parsing configuration
      this.addJndiHostDefinitionsParsingRules(jndiHostDefinitionsDigester);

      // Parse
      List<JndiHost> jndiHosts = null;
      try
      {
         // Parse
         jndiHosts = (List<JndiHost>) jndiHostDefinitionsDigester.parse(inStream);
      }
      catch (IOException e)
      {
         throw new ServiceLocatorException(e);
      }
      catch (SAXException e)
      {
         throw new ServiceLocatorException(e);
      }

      // Assign each JNDI Host a unique ID, if not assigned
      int id = 0;
      List<String> ids = new ArrayList<String>();
      for (JndiHost jndiHost : jndiHosts)
      {
         // No ID specified for this host, assign one
         if (GenericValidator.isBlankOrNull(jndiHost.getId()))
         {
            jndiHost.setId(JndiHostConfigurationParser.DEFAULT_JNDI_HOST_ID_PREFIX + Integer.toString((id++)));
         }

         // Check for multiple IDs
         if (ids.contains(jndiHost.getId()))
         {
            throw new ServiceLocatorException("JNDI Host with address " + jndiHost.getAddress()
                  + " has conflicting/duplicate ID of " + jndiHost.getId());
         }
         else
         {
            // Add to list of IDs
            ids.add(jndiHost.getId());
         }
      }
      
      // Add to Map, indexed by ID
      Map<String,JndiHost> hosts = new HashMap<String, JndiHost>();
      for(JndiHost host : jndiHosts)
      {
         hosts.put(host.getId(), host);
      }
      
      // Return Map
      return hosts;
   }

   // Internal Helper Methods

   /**
    * Adds parsing rules for reading configuration specifying JNDI Hosts
    * 
    * @param digester
    */
   private void addJndiHostDefinitionsParsingRules(Digester digester)
   {

      // When the root is encountered, create a List
      // to hold the JNP Host Definitions
      digester.addObjectCreate("service-locator/jndi-hosts", ArrayList.class);

      // When a new host definition is encountered,
      // create a new JNP Host
      digester.addObjectCreate("service-locator/jndi-hosts/jndi-host", JndiHost.class);

      // Set all properties (in this case, "name")
      // from the "host" entry to the "JnpHost.name"
      // object
      digester.addSetProperties("service-locator/jndi-hosts/jndi-host");
      
      // Set the ID
      digester.addCallMethod("service-locator/jndi-hosts/jndi-host/id", "setId", 1);
      digester.addCallParam("service-locator/jndi-hosts/jndi-host/id", 0);

      // Set the address
      digester.addCallMethod("service-locator/jndi-hosts/jndi-host/address", "setAddress", 1);
      digester.addCallParam("service-locator/jndi-hosts/jndi-host/address", 0);

      // Set the port
      digester.addCallMethod("service-locator/jndi-hosts/jndi-host/port", "setPort", 1, new Class[]
      {Integer.class});
      digester.addCallParam("service-locator/jndi-hosts/jndi-host/port", 0);

      // Add the JNP Host to the List
      digester.addSetNext("service-locator/jndi-hosts/jndi-host", "add");

   }

}
