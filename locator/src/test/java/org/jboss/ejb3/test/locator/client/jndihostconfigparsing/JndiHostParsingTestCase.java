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
import org.jboss.ejb3.locator.client.JndiHostConfigurationParser;
import org.jboss.ejb3.locator.client.ServiceLocatorException;

public class JndiHostParsingTestCase extends TestCase
{
   // Class Members

   private static final Log logger = LogFactory.getLog(JndiHostParsingTestCase.class);

   private static final String FILE_NAME_CONFIGURATION_DEFAULT = "jboss-ejb3-servicelocator-default.xml";

   private static final String FILE_NAME_CONFIGURATION_MULTIPLE_HOSTS = "jboss-ejb3-servicelocator-multiplehosts.xml";

   private static final String FILE_NAME_CONFIGURATION_ID_DEFINED = "jboss-ejb3-servicelocator-id_defined.xml";

   private static final String FILE_NAME_CONFIGURATION_ID_COLLISION = "jboss-ejb3-servicelocator-id_collision.xml";

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
   /**
    * Tests a config file of one host may be parsed, with values defined as expected
    */
   public void testParseOneHost()
   {
      // Initialize
      boolean noParseErrors = true;

      List<JndiHost> hosts = null;

      try
      {
         // Parse
         hosts = this.getConfigurationFromConfigFile(JndiHostParsingTestCase.FILE_NAME_CONFIGURATION_DEFAULT);
      }
      catch (Exception e)
      {
         logger.error(e);
         noParseErrors = false;
      }

      // Test
      assertTrue(noParseErrors);
      assertNotNull(hosts);
      assertTrue(hosts.size() == 1);
      JndiHost host = hosts.get(0);
      assertEquals(host.getAddress(), "localhost");
      assertEquals(host.getPort(), 1099);
      assertNotNull(host.getId());
      assertTrue(host.getId().startsWith(JndiHostConfigurationParser.DEFAULT_JNDI_HOST_ID_PREFIX));
   }

   /**
    * Tests a config file with many hosts may be parsed, with values defined as expected 
    */
   public void testParseMultipleHosts()
   {
      // Parse
      List<JndiHost> hosts = this
            .getConfigurationFromConfigFile(JndiHostParsingTestCase.FILE_NAME_CONFIGURATION_MULTIPLE_HOSTS);

      // Test
      assertNotNull(hosts);
      assertTrue(hosts.size() == 3);
      JndiHost host1 = hosts.get(0);
      JndiHost host2 = hosts.get(1);
      assertEquals(host1.getPort(), 1099);
      assertEquals(host2.getPort(), 1199);
   }

   /**
    * Tests a config file with one host, specifying own ID
    */
   public void testParseWithIdDefined()
   {
      // Parse
      List<JndiHost> hosts = this
            .getConfigurationFromConfigFile(JndiHostParsingTestCase.FILE_NAME_CONFIGURATION_ID_DEFINED);

      // Test
      assertNotNull(hosts);
      assertTrue(hosts.size() == 1);
      JndiHost host = hosts.get(0);
      assertEquals(host.getId(), "MyID1");
   }

   /**
    * Tests a config file with one host, specifying own ID
    */
   public void testParseWithIdCollisions()
   {
      // Parse
      try
      {
         this.getConfigurationFromConfigFile(JndiHostParsingTestCase.FILE_NAME_CONFIGURATION_ID_COLLISION);
      }
      catch (ServiceLocatorException sle)
      {
         // Expected, return
         return;
      }

      // Should have thrown a parse error, fail
      fail("ID Collisions within the configuration file are not allowed");
   }

   // Internal Helper Methods
   /**
    * Obtains the configuration parsed from the specified file name
    */
   private List<JndiHost> getConfigurationFromConfigFile(String fileName)
   {
      return JndiHostConfigurationParser.getInstance().parse(
            Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
   }

}
