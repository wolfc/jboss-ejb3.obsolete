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
package org.jboss.ejb3.embedded.test.jpa.unit;

import static org.jboss.ejb3.embedded.JBossEJBContainer.on;
import static org.jboss.ejb3.embedded.dsl.DeploymentBuilder.deployment;
import static org.jboss.ejb3.embedded.dsl.PackageBuilder.pkg;
import static org.jboss.ejb3.embedded.dsl.ResourceFinder.resource;
import static org.jboss.ejb3.embedded.test.dsl.DataSourceBuilder.localDataSource;
import static org.jboss.ejb3.embedded.test.dsl.PersistenceBuilder.persistence;
import static org.jboss.ejb3.embedded.test.dsl.PersistenceUnitBuilder.unit;

import java.io.IOException;
import java.util.Properties;

import javax.ejb.EJBContainer;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.embedded.test.common.AbstractEmbeddedTestCase;
import org.jboss.ejb3.embedded.test.jpa.PhoneBookLocal;
import org.jboss.logging.Logger;
import org.jboss.metadata.rar.jboss.mcf.NonXADataSourceDeploymentMetaData;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PhoneBookTestCase extends AbstractEmbeddedTestCase
{
   private static Logger log = Logger.getLogger(PhoneBookTestCase.class);

   @BeforeClass
   public static void beforeClass() throws DeploymentException, IOException
   {
      Properties properties = new Properties();
      container = EJBContainer.createEJBContainer(properties);
      
      on(container).deploy(deployment(resource("explicit/ds-deployers-beans.xml")));
      
      on(container).deploy(
            deployment("default-ds",
               NonXADataSourceDeploymentMetaData.class,
               localDataSource()
                  .connectionURL("jdbc:hsqldb:mem:defaultdb")
                  .driverClass("org.jboss.ejb3.embedded.test.common.HSQLDBService")
                  .jndiName("java:/DefaultDS")
                  .user("sa")
                  .password("")
                  .getMetaData()
            ),
            deployment(
                  pkg("org.jboss.ejb3.embedded.test.jpa"),
                  persistence(
                        unit("tempdb")
                           .jtaDataSource("java:/DefaultDS")
                           .property("hibernate.hbm2ddl.auto", "create-drop")
                        ))
            );
   }

   @Test
   public void test1() throws NamingException
   {
      InitialContext ctx = new InitialContext();
      PhoneBookLocal phoneBook = (PhoneBookLocal) ctx.lookup("PhoneBookBean/local");
      
      phoneBook.addEntry("test", "test");
   }
}
