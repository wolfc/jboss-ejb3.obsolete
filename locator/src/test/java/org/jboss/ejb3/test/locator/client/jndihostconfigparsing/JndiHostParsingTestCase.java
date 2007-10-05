/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.locator.client.jndihostconfigparsing;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.ejb3.locator.client.JndiHost;
import org.jboss.ejb3.locator.client.JndiHostMetadataParser;

public class JndiHostParsingTestCase extends TestCase
{
   // Class Members

   private static final Log logger = LogFactory.getLog(JndiHostParsingTestCase.class);

   private static final String FILE_NAME_CONFIGURATION_DEFAULT = "jboss-ejb3-servicelocator.xml";

   // Overridden Implementations
   /**
    * Setup
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   // Test Methods
   public void testParse()
   {
      // Initialize
      boolean noParseErrors = true;

      try
      {
         // Parse
         this.getConfigurationFromConfigFile(JndiHostParsingTestCase.FILE_NAME_CONFIGURATION_DEFAULT);
      }
      catch (Exception e)
      {
         logger.error(e);
         noParseErrors = false;
      }

      // Test
      assertTrue(noParseErrors);
   }

   // Internal Helper Methods
   /**
    * Obtains the configuration parsed from the specified file name
    */
   private List<JndiHost> getConfigurationFromConfigFile(String fileName)
   {
      return JndiHostMetadataParser.getInstance().parse(
            Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
   }
}
