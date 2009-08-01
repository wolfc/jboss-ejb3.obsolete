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
package org.jboss.ejb3.nointerface.test.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.jboss.bootstrap.impl.mc.server.MCServerImpl;
import org.jboss.bootstrap.spi.config.ServerConfig;
import org.jboss.bootstrap.spi.mc.config.MCServerConfig;
import org.jboss.bootstrap.spi.mc.config.MCServerConfigFactory;
import org.jboss.bootstrap.spi.mc.server.MCServer;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * AbstractNoInterfaceTestCase
 *
 * This provides the necessary test support for the no-interface
 * component
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class AbstractNoInterfaceTestCase
{

   /**
    * The server
    */
   private static MCServer server;


   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(AbstractNoInterfaceTestCase.class);

   /**
    * The home of our server runtime environment
    */
   protected static final String SERVER_HOME_DIR_PATH = "target/bootstrap";

   /**
    * The home of our server profile runtime environment
    */
   protected static final String SERVER_PROFILE_HOME_DIR_PATH = SERVER_HOME_DIR_PATH + "/server/default";

   // The following are all mandated by the bootstrap server, so let's just have them even
   // though they are not used in our tests
   /**
    * Data dir
    */
   protected static final String SERVER_PROFILE_DATA_DIR_PATH = SERVER_PROFILE_HOME_DIR_PATH + "/data";

   /**
    *  log dir
    */
   protected static final String SERVER_PROFILE_LOG_DIR_PATH = SERVER_PROFILE_HOME_DIR_PATH + "/log";

   /**
    *  tmp dir
    */
   protected static final String SERVER_PROFILE_TMP_DIR_PATH = SERVER_PROFILE_HOME_DIR_PATH + "/tmp";

   /**
    * tmp/deploy dir
    */
   protected static final String SERVER_PROFILE_TMP_DEPLOY_DIR_PATH = SERVER_PROFILE_TMP_DIR_PATH + "/deploy";

   /**
    *  tmp/native dir
    */
   protected static final String SERVER_PROFILE_TMP_NATIVE_DIR_PATH = SERVER_PROFILE_TMP_DIR_PATH + "/native";

   /**
    * This is where we have our bootstrap configurations for our
    * tests. This is the place where we place our bootstrap.xml.
    *
    */
   protected static final String SERVER_PROFILE_CONFIG_DIR_PATH = System.getProperty("basedir") + "/src/test/resources/conf";

   /**
    * This is where we place our configuration files which provide the runtime environment
    * for our server. Ex: ejb3-deployer-jboss-beans.xml is placed here
    */
   protected static final String SERVER_PROFILE_DEPLOYERS_DIR_PATH = System.getProperty("basedir") + "/src/test/resources/deployers";

   /**
    * This is where we place our applications to be deployed. The "applications" can also include
    * EJB3 remoting connectors, interceptors etc...
    */
   protected static final String SERVER_PROFILE_DEPLOY_DIR_PATH = System.getProperty("basedir") + "/src/test/resources/deploy";



   /**
    * Bootstrap the server.
    * It first creates a server through the bootstrap.xml. This does not start the server profile
    * (i.e. it does NOT deploy the deployers or the applications).
    *
    * @see #startServerConfiguration()
    * @throws Exception
    */
   public static void bootstrap() throws Exception
   {
      MCServerConfig config = createServerConfig();
      server = new MCServerImpl(config);
      server.start();
      logger.trace("Started the server");
   }

   protected static MCServerConfig createServerConfig() throws Exception
   {
      MCServerConfig mcServerConfig = MCServerConfigFactory.createServerConfig();
      mcServerConfig.bootstrapHome(findDir(SERVER_PROFILE_CONFIG_DIR_PATH));

      return mcServerConfig;
   }

   /**
    * Shutdown the server
    * @throws Exception
    */
   public static void shutdown() throws Exception
   {
      if (server != null)
      {
         server.shutdown();
         logger.info("no-interface server has been shutdown");
      }
   }

   /**
    * Deploys the deployers and the applications
    * to start the server configuration
    *
    * @throws Exception
    */
   public static void startServerConfiguration() throws Exception
   {

      deployDeployers();

      // Some of the deployers/mc beans come from the classpath jars (ex: For JTA support,
      // jboss-jta-profile.jar/META-INF/jboss-beans.xml). We need to deploy those too.
      // This approach of leniently deploying all META-INF/jboss-beans.xml from the
      // classpath is OK as long as we know that those are non-conflicting and are infact
      // required to be deployed.
      deployClasspathJBossBeans();
//
//      logger.debug("Deployers ready");
      deployApplications();
      logger.debug("no-interface server completely started");
   }

   /**
    * Deploys the deployers
    *
    * @throws Exception
    */
   public static void deployDeployers() throws Exception
   {
      // now deploy the deployers
      deploy(new File(SERVER_PROFILE_DEPLOYERS_DIR_PATH).toURL());

   }

   /**
    * Deploy the applications (in deploy folder)
    * @throws Throwable
    */
   public static void deployApplications() throws Exception
   {
      deploy(new File(SERVER_PROFILE_DEPLOY_DIR_PATH).toURL());

      // additionally we need the ejb3-interceptors-aop.xml which we pull in from our
      // ejb3-core dependency jar (instead of duplicating that file in our test setup)
      URL ejb3InterceptorsConfigFile = Thread.currentThread().getContextClassLoader().getResource("ejb3-interceptors-aop.xml");
      logger.debug("ejb3-interceptors-aop.xml being picked up from " + ejb3InterceptorsConfigFile);
      deploy(ejb3InterceptorsConfigFile);
   }

   /**
    * Create the necessary infrastructure to boot the server. This includes, creating
    * the necessary folder structure mandated by the {@link MCServer}. This furthermore
    * sets the server home and profile home URLs for the bootstrap.
    *
    * @return
    * @throws IOException
    */
   protected static Properties createBootstrapEnv() throws IOException
   {
      URL serverHome = mkdir(SERVER_HOME_DIR_PATH);
      URL serverProfileHome = mkdir(SERVER_PROFILE_HOME_DIR_PATH);
      if (logger.isTraceEnabled())
      {
         logger.trace("Server Home is " + serverHome);
         logger.trace("Profile Home is " + serverProfileHome);
      }

      mkdir(SERVER_PROFILE_DATA_DIR_PATH);
      mkdir(SERVER_PROFILE_LOG_DIR_PATH);
      mkdir(SERVER_PROFILE_TMP_DIR_PATH);
      mkdir(SERVER_PROFILE_TMP_DEPLOY_DIR_PATH);
      mkdir(SERVER_PROFILE_TMP_NATIVE_DIR_PATH);

      Properties serverBootstrapProperties = new Properties();
      serverBootstrapProperties.put(ServerConfig.PROP_KEY_BOOTSTRAP_HOME_URL, serverHome);
      //serverBootstrapProperties.put(ServerConfig., serverProfileHome.getPath());

      URL configDir = findDir(SERVER_PROFILE_CONFIG_DIR_PATH);
      //serverBootstrapProperties.put(ServerConfig.SERVER_CONFIG_URL, configDir.toString());
      if (logger.isTraceEnabled())
      {
         logger.trace("Config URL is " + configDir);
      }

      return serverBootstrapProperties;
   }

   /**
    *
    * @return
    */
   protected static URL getTestClassesURL() throws Exception
   {
      String noInterfaceTestPath = "org/jboss/ejb3/nointerface/test";
      URL noInterfaceTestsURL = Thread.currentThread().getContextClassLoader().getResource(noInterfaceTestPath);
      String entirePathToNoInterfaceTest = noInterfaceTestsURL.toString();
      String testClassesRoot = entirePathToNoInterfaceTest.substring(0, entirePathToNoInterfaceTest.length() - noInterfaceTestPath.length());
      URL testClassesRootURL = new URL(testClassesRoot);
      logger.debug("Test classes URL = " + testClassesRootURL);
      return testClassesRootURL;
   }
   /**
    * Utility method to create a dir
    *
    * @param path
    * @return
    * @throws IOException
    */
   protected static URL mkdir(String path) throws IOException
   {
      File file = new File(path);
      boolean success = file.mkdirs() || file.isDirectory();
      if (!success)
         throw new IOException("Could not create " + path);
      return file.toURL();

   }

   /**
    * Utility method to look for a dir
    *
    * @param path
    * @return
    * @throws IOException
    */
   protected static URL findDir(String path) throws IOException
   {
      File file = new File(path);
      boolean success = file.isDirectory();
      if (!success)
         throw new IOException(path + " is either not present or is not a dir");
      return file.toURL();

   }

   /**
    * Deploys the URL.
    *
    * This uses the {@link MainDeployer} (configured during the bootstrap process) to deploy
    * the URL. The {@link MainDeployer} internally will pass this deployment through all available
    * deployers
    *
    * @param deployURL
    * @throws Exception
    */
   protected static void deploy(URL deployURL) throws Exception
   {
      if (deployURL == null)
      {
         throw new IllegalArgumentException("Null URL passed to deploy");
      }
      logger.debug("Deploying " + deployURL);
      MainDeployer mainDeployer = getMainDeployer();
      VirtualFile root = VFS.getRoot(deployURL);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      mainDeployer.deploy(deployment);
      mainDeployer.checkComplete(deployment);
      logger.debug("Completely deployed " + deployURL);

   }


   /**
    * Deploy a resource
    */
   protected static void deploy(String resourceName) throws Exception
   {
      if (resourceName == null)
      {
         throw new IllegalArgumentException("Null resourceName passed to deploy");
      }
      URL resourceURL = Thread.currentThread().getContextClassLoader().getResource(resourceName);
      deploy(resourceURL);

   }

   /**
    * Returns the {@link MainDeployer} installed during bootstrap
    *
    * @return
    */
   protected static MainDeployer getMainDeployer()
   {
      ControllerContext context = server.getKernel().getController().getContext("MainDeployer",
            ControllerState.INSTALLED);
      if (context == null)
      {
         throw new RuntimeException("No MainDeployer is available or MainDeployer is not in INSTALLED state");
      }
      return (MainDeployer) context.getTarget();

   }

   protected static void deployClasspathJBossBeans() throws Exception
   {
      Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources("META-INF/jboss-beans.xml");
      while(urls.hasMoreElements())
      {
         URL url = urls.nextElement();
         logger.debug("Deploying META-INF/jboss-beans.xml from classpath: " + url);
         deploy(url);
      }
   }
}
