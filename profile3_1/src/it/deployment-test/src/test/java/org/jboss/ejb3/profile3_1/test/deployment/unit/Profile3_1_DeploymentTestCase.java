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
package org.jboss.ejb3.profile3_1.test.deployment.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.profile3_1.test.common.AbstractProfile3_1_TestCase;
import org.jboss.ejb3.profile3_1.test.deployment.SimpleSLSB;
import org.jboss.ejb3.profile3_1.test.deployment.SimpleSLSBLocal;
import org.jboss.ejb3.profile3_1.test.deployment.SimpleSLSBRemote;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Profile3_1_DeploymentTestCase
 *
 * Testcase to ensure that the profile3_1 bootstrap loads properly
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class Profile3_1_DeploymentTestCase extends AbstractProfile3_1_TestCase
{
   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(Profile3_1_DeploymentTestCase.class);

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      bootstrap();
      startProfile();
   }

   @AfterClass
   public static void afterClass() throws Throwable
   {
      shutdown();
   }

   /**
    * This test ensures that the basic deployment support provided by
    * profile3_1 component is working
    *
    * @throws Throwable
    */
   @Test
   public void testBeanDeployment() throws Throwable
   {
      // Deploy the jar (containing the bean)
      deploy(Thread.currentThread().getContextClassLoader().getResource("ejb3-profile-test.jar"));
      logger.info("ejb3-profile-test.jar deployed");

      // lookup the bean
      Context ctx = new InitialContext();
      SimpleSLSBLocal localBean = (SimpleSLSBLocal) ctx.lookup("SimpleSLSB/local");
      logger.debug("Successfully looked up local bean " + localBean);

      // invoke a method
      String message = "hello";
      String returnedMessage = localBean.echo(message);
      logger.debug("Client sent message = " + message + " ; Local bean returned message = " + returnedMessage );

      assertNotNull("Local bean returned null message",returnedMessage);
      assertEquals("Local bean returned incorrect message",message,returnedMessage);

      // now let's check the remote business interface
      SimpleSLSBRemote remoteBean = (SimpleSLSBRemote) ctx.lookup("SimpleSLSB/remote");
      logger.debug("Successfully looked up remote bean " + remoteBean);

      String messageFromRemoteBean = remoteBean.echo(message);
      logger.debug("Client sent message = " + message + " ; remote bean returned = " + messageFromRemoteBean);

      assertNotNull("Local bean returned null message",messageFromRemoteBean);
      assertEquals("Local bean returned incorrect message",message,messageFromRemoteBean);

   }

//   @Test
//   public void testClassDeployment() throws Exception
//   {
//      deploy(SimpleSLSB.class);
//      Context ctx = new InitialContext();
//       SimpleSLSBLocal localBean = (SimpleSLSBLocal) ctx.lookup("SimpleSLSB/local");
//       logger.debug("Successfully looked up local bean " + localBean);
//      logger.info("Done test2");
//
//   }
}
