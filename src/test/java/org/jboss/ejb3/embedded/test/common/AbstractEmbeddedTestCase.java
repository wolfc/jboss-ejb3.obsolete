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
package org.jboss.ejb3.embedded.test.common;

import static org.jboss.ejb3.embedded.JBossEJBContainer.on;
import static org.jboss.ejb3.embedded.dsl.DeploymentBuilder.deployment;
import static org.jboss.ejb3.embedded.dsl.ResourceFinder.resource;
import static org.jboss.ejb3.embedded.test.dsl.DataSourceBuilder.localDataSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

import javax.ejb.EJBContainer;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.proxy.remoting.ProxyRemotingUtils;
import org.jboss.metadata.rar.jboss.mcf.NonXADataSourceDeploymentMetaData;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class AbstractEmbeddedTestCase
{
   protected static EJBContainer container;
   
   @AfterClass
   public static void afterClass()
   {
      if (container != null)
         container.close();
   }

   @BeforeClass
   public static void beforeClass() throws DeploymentException, IOException
   {
      clearProxyRemotingUtilsDefaultClientBindingHack();
      
      Properties properties = new Properties();
      container = EJBContainer.createEJBContainer(properties);
      
      // TODO: scanning filter doesn't work, so we need to deploy the data source
      on(container).deploy(
            deployment(resource("explicit/ds-deployers-beans.xml")),
            deployment("default-ds",
               NonXADataSourceDeploymentMetaData.class,
               localDataSource()
                  .connectionURL("jdbc:hsqldb:mem:defaultdb")
                  .driverClass("org.jboss.ejb3.embedded.test.common.HSQLDBService")
                  .jndiName("java:/DefaultDS")
                  .user("sa")
                  .password("")
                  .getMetaData()
            ));
//            deployment("temp-persistence-unit",
//               persistence(
//                  unit("tempdb")
//                     .jtaDataSource("java:/DefaultDS")
//                     .property("hibernate.hbm2ddl.auto", "create-drop")
//                  )));
      
   }

   private static void clearProxyRemotingUtilsDefaultClientBindingHack()
   {
      try
      {
         Field field = ProxyRemotingUtils.class.getDeclaredField("DEFAULT_CLIENT_BINDING");
         field.setAccessible(true);
         field.set(null, null);
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
