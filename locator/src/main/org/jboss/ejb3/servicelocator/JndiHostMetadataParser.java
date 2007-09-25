package org.jboss.ejb3.servicelocator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class JndiHostMetadataParser
{

   // Class Members
   private static JndiHostMetadataParser instance = null;

   // Internal Constructor
   private JndiHostMetadataParser()
   {

   }

   // Singleton Accessor
   public static synchronized JndiHostMetadataParser getInstance()
   {
      // Ensure instanciated
      if (JndiHostMetadataParser.instance == null)
      {
         JndiHostMetadataParser.instance = new JndiHostMetadataParser();
      }

      // Return
      return JndiHostMetadataParser.instance;
   }

   // Functional Methods

   @SuppressWarnings(value = "unchecked")
   public List<JndiHost> parse(InputStream inStream)
   {
      // Initialize
      Digester jndiHostDefinitionsDigester = new Digester();
      // Add Rules for parsing configuration
      this.addJnpHostDefinitionsParsingRules(jndiHostDefinitionsDigester);

      //TODO Assign each JNDI Host a unique ID
      // Parse
      try
      {
         return (List<JndiHost>) jndiHostDefinitionsDigester.parse(inStream);
      }
      catch (IOException e)
      {
         throw new ServiceLocatorException(e);
      }
      catch (SAXException e)
      {
         throw new ServiceLocatorException(e);
      }
   }

   // Internal Helper Methods

   /**
    * Adds parsing rules for reading configuration specifying JNDI Hosts
    * 
    * @param digester
    */
   private void addJnpHostDefinitionsParsingRules(Digester digester)
   {
      // When the root is encountered, create a List
      // to hold the JNP Host Definitions
      digester.addObjectCreate("jndi-hosts", ArrayList.class);

      // When a new host definition is encountered,
      // create a new JNP Host
      digester.addObjectCreate("jndi-hosts/host", JndiHost.class);

      // Set all properties (in this case, "name")
      // from the "host" entry to the "JnpHost.name"
      // object
      digester.addSetProperties("jndi-hosts/host");

      // Set the address
      digester.addCallMethod("jndi-hosts/host/address", "setAddress", 1);
      digester.addCallParam("jndi-hosts/host/address", 0);

      // Set the port
      digester.addCallMethod("jndi-hosts/host/port", "setPort", 1, new Class[]
      {Integer.class});
      digester.addCallParam("jndi-hosts/host/port", 0);

      // Add the JNP Host to the List
      digester.addSetNext("jndi-hosts/host", "add");

   }

}
