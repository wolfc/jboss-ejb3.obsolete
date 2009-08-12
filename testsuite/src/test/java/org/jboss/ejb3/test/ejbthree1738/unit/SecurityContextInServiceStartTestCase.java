/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1738.unit;

import javax.naming.Context;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1738.ProtectedLocalBusiness;
import org.jboss.ejb3.test.ejbthree1738.RunAsRemoteBusiness;
import org.jboss.logging.Logger;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;
import org.junit.Assert;

/**
 * SecurityContextInServiceStartTestCase
 * 
 * Test Cases to ensure that we may invoke within a 
 * security context in @Service bean lifecycle start()
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SecurityContextInServiceStartTestCase extends JBossTestCase
{
   //------------------------------------------------------------------------||
   // Class Members ---------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(SecurityContextInServiceStartTestCase.class);

   /**
    * Name of the deployment
    */
   private static final String NAME_DEPLOYMENT = "ejbthree1738.jar";

   //------------------------------------------------------------------------||
   // Instance Members ------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * The EJB used to test security context in start() lifecycle
    */
   private RunAsRemoteBusiness bean;

   //------------------------------------------------------------------------||
   // Constructor -----------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Constructor
    */
   public SecurityContextInServiceStartTestCase()
   {
      super(SecurityContextInServiceStartTestCase.class.getName());
   }

   //------------------------------------------------------------------------||
   // Suite -----------------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Defines the Suite
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(SecurityContextInServiceStartTestCase.class, NAME_DEPLOYMENT);
   }

   //------------------------------------------------------------------------||
   // Tests -----------------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Ensures that we may invoke within the context of a security 
    * domain as a defined role while in the start() lifecycle 
    * of a @Service bean
    */

   public void testSecurityContextInServiceStart() throws Throwable
   {
      // Log
      log.info("testSecurityContextInServiceStart");

      // Log in as some user (with "unauthorized" role)
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("someuser", "password");
      client.login();

      // Get the bean
      final RunAsRemoteBusiness bean = this.getBean();

      // Get the value from a protected service lookup; the @RunAs should change the role used to "authorized"
      final String valueFromBusinessInvocation = bean.getValueFromProtectedService();
      // Ensure the value is correct
      Assert
            .assertEquals("The obtained value was incorrect", ProtectedLocalBusiness.VALUE, valueFromBusinessInvocation);

      // Get the value which should be set upon instance start()
      final String valueFromStart = bean.getProtectedValueObtainedFromStart();

      // Ensure the value's set
      Assert.assertNotNull("The value from the protected bean was not set", valueFromStart);
      // Ensure the value is correct
      Assert.assertEquals("The obtained value was incorrect", ProtectedLocalBusiness.VALUE, valueFromStart);
   }

   /**
    * Ensures a bean with a @SecurityDomain may be redeployed
    * 
    * JBAS-6362
    * 
    * @throws Exception
    */
   public void testBeanOnRedeploy() throws Throwable
   {

      // Redeploy
      redeploy(NAME_DEPLOYMENT);

      // Log in as some user (with "unauthorized" role)
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("someuser", "password");
      client.login();

      // Get the bean
      final RunAsRemoteBusiness bean = this.getBean();

      // Get the value from a protected service lookup; the @RunAs should change the role used to "authorized"
      final String valueFromBusinessInvocation = bean.getValueFromProtectedService();
      // Ensure the value is correct
      Assert
            .assertEquals("The obtained value was incorrect", ProtectedLocalBusiness.VALUE, valueFromBusinessInvocation);

   }

   //------------------------------------------------------------------------||
   // Internal Helper Methods -----------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Obtains the EJB via which we'll access the endpoint
    */
   private RunAsRemoteBusiness getBean() throws Throwable
   {
      // If we haven't yet getten the bean
      if (bean == null)
      {
         // Look it up and set
         final Context context = this.getInitialContext();
         bean = (RunAsRemoteBusiness) context.lookup(RunAsRemoteBusiness.JNDI_NAME);
      }

      // Return
      return bean;
   }
}
