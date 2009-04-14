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
package org.jboss.ejb3.embedded.test.stateless.unit;

import static org.jboss.ejb3.embedded.JBossEJBContainer.on;
import static org.jboss.ejb3.embedded.dsl.DeploymentBuilder.deployment;
import static org.jboss.ejb3.embedded.dsl.PackageBuilder.pkg;
import static org.jboss.ejb3.embedded.test.dsl.PersistenceBuilder.persistence;
import static org.jboss.ejb3.embedded.test.dsl.PersistenceUnitBuilder.unit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.embedded.test.common.AbstractEmbeddedTestCase;
import org.jboss.ejb3.embedded.test.stateless.Greeter;
import org.jboss.ejb3.embedded.test.stateless.GreeterRemote;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class GreeterTestCase extends AbstractEmbeddedTestCase
{
   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(GreeterTestCase.class);

   @BeforeClass
   public static void beforeClass() throws DeploymentException, IOException
   {
      AbstractEmbeddedTestCase.beforeClass();
      
      on(container).deploy(
            deployment(
                  pkg("org.jboss.ejb3.embedded.test.stateless"), 
                  // FIXME: filtering doesn't work yet
                  persistence(
                        unit("tempdb")
                           .jtaDataSource("java:/DefaultDS")
                           .property("hibernate.hbm2ddl.auto", "create-drop")
                           )));
   }

   @Test
   public void test1() throws NamingException
   {
      InitialContext ctx = new InitialContext();
      Greeter greeter = (Greeter) ctx.lookup("GreeterBean/local");
      String now = new Date().toString();
      String actual = greeter.sayHi(now);
      assertEquals("Hi " + now, actual);
   }

   @Test
   public void testGreeter2() throws NamingException
   {
      InitialContext ctx = new InitialContext();
      Greeter greeter = (Greeter) ctx.lookup("Greeter2/local");
      String now = new Date().toString();
      String actual = greeter.sayHi(now);
      assertEquals("Hi " + now, actual);
   }

   @Test
   public void testGreeterRemote() throws Exception
   {
      Context ctx = new InitialContext();
      GreeterRemote remoteGreeter = (GreeterRemote) ctx.lookup("GreeterBean/remote");
      String name = "newuser";
      String messageFromGreeter = remoteGreeter.sayHi(name);
      logger.info("Remote Greeter bean says: " + messageFromGreeter);
      assertNotNull("Remote Greeter bean returned null message", messageFromGreeter);
      assertEquals("Remote Greeter bean returned unexpected message", "Hi " + name, messageFromGreeter);
   }
}
