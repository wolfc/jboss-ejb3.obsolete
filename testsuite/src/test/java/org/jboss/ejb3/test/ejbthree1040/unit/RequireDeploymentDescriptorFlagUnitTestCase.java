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

package org.jboss.ejb3.test.ejbthree1040.unit;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.NameNotFoundException;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1040.TestStateless1040Remote;
import org.jboss.ejb3.test.ejbthree1040.TestStatelessBean;
import org.jboss.test.JBossTestCase;

/**
 * Unit Tests for EJBTHREE-1040
 * 
 * @author <a href="mailto:alr@alrubinger.com">ALR</a>
 * @version $Revision:64940 $
 *
 */
public final class RequireDeploymentDescriptorFlagUnitTestCase extends JBossTestCase
{

   // Class Members
   private static final String TEST_JAR_NAME = "ejbthree1040.jar";

   //TODO Incorrect; available in JMX?
   private static final String OBJECT_NAME_EJB3_DEPLOYER = "jboss.ejb3:service=EJB3Deployer";

   private static final String ATTRIBUTE_REQUIRE_DEPLOYMENT_DESCRIPTOR = "requireDeploymentDescriptor";

   // Instance Members

   private MBeanServerConnection server = null;

   private ObjectName on = null;

   // Constructor
   /**
    * Calls upon super implementation
    * 
    * @param name Name of the test case
    */
   public RequireDeploymentDescriptorFlagUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   // Test Suite
   /**
    * Static initializer for the Test Suite
    */
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(RequireDeploymentDescriptorFlagUnitTestCase.class, null);
   }

   /**
    * Undeploys the test JAR, calls super implementation
    */
   @Override
   protected void tearDown() throws Exception
   {
      this.undeploy(RequireDeploymentDescriptorFlagUnitTestCase.TEST_JAR_NAME);
      super.tearDown();
   }

   /**
    * Obtains a reference to the server and EJB3 Deployer
    */
   @Override
   protected void setUp() throws Exception
   {
      // Call super implementation
      super.setUp();
      // Obtain a reference to the server
      this.server = this.getServer();
      // Obtain the EJB3 Deployer
      this.on = new ObjectName(RequireDeploymentDescriptorFlagUnitTestCase.OBJECT_NAME_EJB3_DEPLOYER);
   }

   /**
    * Ensures that deployment of a JAR with no Deployment Descriptors 
    * succeeds in default deployer configuration
    * 
    * @throws Exception
    */
   public void testDeploymentSuccessWithDefaultConfiguration() throws Exception
   {
      try
      {
         // Attempt to redeploy the JAR
         this.redeploy(RequireDeploymentDescriptorFlagUnitTestCase.TEST_JAR_NAME);

         // Lookup EJB, attempt cast
         TestStateless1040Remote stateless = (TestStateless1040Remote) this.getInitialContext().lookup(
               TestStatelessBean.JNDI_BINDING_REMOTE);

         // If we've reached here, we're good
         log.info("TEST SUCCESSFUL: testDeploymentSuccessWithDefaultConfiguration, received deployed "
               + stateless.toString());
      }
      // Handle unexpected error and fail the test
      catch (Throwable t)
      {
         log.error(t);
         JBossTestCase.fail("Exception received: " + t.getClass().getName() + ", with message \"" + t.getMessage()
               + "\"");
      }
      finally
      {
         // Remove the Deployment
         this.undeploy(RequireDeploymentDescriptorFlagUnitTestCase.TEST_JAR_NAME);
      }
   }

   /**
    * Ensures that deployment of a JAR with no Deployment Descriptors 
    * fails when deployer is configured to require either ejb-jar.xml or jboss.xml
    * 
    * @throws Exception
    */
   public void testDeploymentFailureWithRequiredFlagSet() throws Exception
   {

      // Configure the deployer to require a DD
      this.setDeployerToRequireDd(true);

      try
      {
         // Attempt to redeploy the JAR
         this.redeploy(RequireDeploymentDescriptorFlagUnitTestCase.TEST_JAR_NAME);

         // Lookup EJB, attempt cast, and determine if deployed
         TestStateless1040Remote stateless = (TestStateless1040Remote) this.getInitialContext().lookup(
               TestStatelessBean.JNDI_BINDING_REMOTE);

         // Deployment should have failed
         JBossTestCase.fail("Expected " + NameNotFoundException.class.getName() + " not encountered for deployment "
               + stateless.toString());
      }
      // Handle unexpected error and fail the test
      catch (Throwable t)
      {
         if (t instanceof NameNotFoundException)
         {
            log.info("TEST SUCCESSFUL: " + NameNotFoundException.class.getName()
                  + " encountered as expected for deployment without DD when required by deployer");
         }
      }
      // Regardless of pass/fail, perform the following
      finally
      {
         // Reset the deployer to default configuration
         this.setDeployerToRequireDd(false);

         // Remove the Deployment
         this.undeploy(RequireDeploymentDescriptorFlagUnitTestCase.TEST_JAR_NAME);
      }
   }

   /**
    * Ensures that a deployable unit with a jboss.xml file will deploy when the 
    * deployer is configured to require a desployment descriptor 
    */
   public void testDeploySuccessWithRequiredDdAndJbossXml() throws Exception
   {
      fail("IMPLEMENT");
   }

   /**
    * Ensures that a deployable unit with an ejb-jar.xml file will deploy when the 
    * deployer is configured to require a desployment descriptor 
    */
   public final void testDeploySuccessWithRequiredDdAndEjbJarXml() throws Exception
   {
      fail("IMPLEMENT");
   }

   // Internal Helper Methods

   /**
    * Configures the deployer to require or not require a deployment descriptor for deployable units
    * 
    * @param required If the DD is required
    */
   private void setDeployerToRequireDd(boolean required) throws Exception
   {
      try
      {
         // Set the flag for the deployer to require a deployment descriptor
         this.server.setAttribute(this.on, new Attribute(
               RequireDeploymentDescriptorFlagUnitTestCase.ATTRIBUTE_REQUIRE_DEPLOYMENT_DESCRIPTOR, required
                     ? Boolean.TRUE
                     : Boolean.FALSE));
      }
      catch(AttributeNotFoundException e)
      {
         fail("Attribute not found " + RequireDeploymentDescriptorFlagUnitTestCase.ATTRIBUTE_REQUIRE_DEPLOYMENT_DESCRIPTOR + " on " + this.on);
      }
      catch(InstanceNotFoundException e)
      {
         fail(this.on + " does not expose a MBean interface");
      }
      catch(MBeanException e)
      {
         if(e.getCause() instanceof InstanceNotFoundException)
            fail(this.on + " does not expose a MBean interface");
         throw e;
      }
   }

}
