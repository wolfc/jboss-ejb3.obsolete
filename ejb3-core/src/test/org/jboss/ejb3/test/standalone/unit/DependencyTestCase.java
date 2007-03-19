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
package org.jboss.ejb3.test.standalone.unit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.embedded.EJB3StandaloneDeployer;
import org.jboss.ejb3.test.standalone.DependsRemote;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class DependencyTestCase extends TestCase
{
   private static boolean booted = false;

   public DependencyTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(DeployerTestCase.class);


      // setup test so that embedded JBoss is started/stopped once for all tests here.
      TestSetup wrapper = new TestSetup(suite)
      {
         protected void setUp()
         {
            startupEmbeddedJboss();
         }

         protected void tearDown()
         {
            shutdownEmbeddedJboss();
         }
      };

      return wrapper;
   }

   public static void startupEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.boot(null);
   }

   public static void shutdownEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.shutdown();
   }


   protected InitialContext getInitialContext() throws Exception
   {
      return new InitialContext(getInitialContextProperties());
   }

   protected Hashtable getInitialContextProperties()
   {
      return EJB3StandaloneBootstrap.getInitialContextProperties();
   }

   private Properties getDefaultPersistenceProperties()
           throws IOException
   {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("default.persistence.properties");
      assertNotNull(is);
      Properties defaults = new Properties();
      defaults.load(is);
      return defaults;
   }

   public void testDepends() throws Throwable
   {
      InitialContext ctx = getInitialContext();

      URL res = Thread.currentThread().getContextClassLoader().getResource("marker.txt");
      URL archive = EJB3StandaloneDeployer.getContainingUrlFromResource(res, "marker.txt");
      EJB3StandaloneDeployer deployer = new EJB3StandaloneDeployer();
      deployer.setKernel(EJB3StandaloneBootstrap.getKernel());
      deployer.setJndiProperties(getInitialContextProperties());
      deployer.getArchives().add(archive);

      completeTest(deployer, ctx);

   }
   
   public void testCleanup() throws Throwable
   {
      boolean exceptionThrown = false;
      try
      {
         executeEJBs(getInitialContext());
      }
      catch (Exception e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);

   }

   private void completeTest(EJB3StandaloneDeployer deployer, InitialContext ctx)
           throws Exception
   {
      deployer.create();
      deployer.start();

      executeEJBs(ctx);

      deployer.stop();
      deployer.destroy();
   }

   private void executeEJBs(InitialContext ctx)
           throws NamingException
   {
      DependsRemote stateless = (DependsRemote)ctx.lookup("DependsBean/remote");
      assertNotNull(stateless);
      stateless.test();
     
   }
}