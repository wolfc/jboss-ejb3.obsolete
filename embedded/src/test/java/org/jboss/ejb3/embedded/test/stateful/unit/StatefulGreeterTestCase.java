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
package org.jboss.ejb3.embedded.test.stateful.unit;

import static org.jboss.ejb3.embedded.JBossEJBContainer.on;
import static org.jboss.ejb3.embedded.dsl.DeploymentBuilder.deployment;
import static org.jboss.ejb3.embedded.dsl.PackageBuilder.pkg;
import static org.jboss.ejb3.embedded.test.dsl.PersistenceBuilder.persistence;
import static org.jboss.ejb3.embedded.test.dsl.PersistenceUnitBuilder.unit;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.embedded.test.common.AbstractEmbeddedTestCase;
import org.jboss.ejb3.embedded.test.stateful.StatefulGreeter;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class StatefulGreeterTestCase extends AbstractEmbeddedTestCase
{
   @BeforeClass
   public static void beforeClass() throws DeploymentException, IOException
   {
      AbstractEmbeddedTestCase.beforeClass();
      
      on(container).deploy(
            deployment(
                  pkg("org.jboss.ejb3.embedded.test.stateful"),
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
      StatefulGreeter greeter = (StatefulGreeter) ctx.lookup("StatefulGreeterBean/local");
      String now = new Date().toString();
      greeter.setName(now);
      String actual = greeter.sayHi();
      assertEquals("Hi " + now, actual);
   }
}
