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
package org.jboss.ejb3.profile3_1.test.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.jboss.bootstrap.microcontainer.ServerImpl;
import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.bootstrap.spi.microcontainer.MCServer;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * AbstractProfile3_1_TestCase
 * 
 * This provides the necessary "profile3_1" runtime environment for testcases to run.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class AbstractProfile3_1_TestCase
{

   /**
    * The server
    */
   private static MCServer server;

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(AbstractProfile3_1_TestCase.class);

   /**
    * The home of our server runtime environment
    */
   protected static final String SERVER_HOME_DIR_PATH = "target/profile3_1_bootstrap";

   /**
    * The home of our "profile3_1" server profile runtime environment 
    */
   protected static final String SERVER_PROFILE3_1_HOME_DIR_PATH = SERVER_HOME_DIR_PATH + "/server/profile3_1";

   // The following are all mandated by the bootstrap server, so let's just have them even
   // though they are not used in our tests
   /**
    * "profile3_1" data dir 
    */
   protected static final String SERVER_PROFILE3_1_DATA_DIR_PATH = SERVER_PROFILE3_1_HOME_DIR_PATH + "/data";

   /**
    * "profile3_1" log dir 
    */
   protected static final String SERVER_PROFILE3_1_LOG_DIR_PATH = SERVER_PROFILE3_1_HOME_DIR_PATH + "/log";

   /**
    * "profile3_1" tmp dir 
    */
   protected static final String SERVER_PROFILE3_1_TMP_DIR_PATH = SERVER_PROFILE3_1_HOME_DIR_PATH + "/tmp";

   /**
    * "profile3_1" tmp/deploy dir 
    */
   protected static final String SERVER_PROFILE3_1_TMP_DEPLOY_DIR_PATH = SERVER_PROFILE3_1_TMP_DIR_PATH + "/deploy";

   /**
    * "profile3_1" tmp/native dir 
    */
   protected static final String SERVER_PROFILE3_1_TMP_NATIVE_DIR_PATH = SERVER_PROFILE3_1_TMP_DIR_PATH + "/native";

   /**
    * This is where we have our bootstrap configurations for our
    *  "profile3_1" tests. This is the place where we place our bootstrap.xml. 
    *  
    */
   protected static final String SERVER_PROFILE3_1_CONFIG_DIR_PATH = "src/test/resources/conf";

   /**
    * This is where we place our configuration files which provide the runtime environment
    * for our "profile3_1". Ex: ejb3-deployer-jboss-beans.xml is placed here
    */
   protected static final String SERVER_PROFILE3_1_DEPLOYERS_DIR_PATH = "src/main/resources/conf";

   /**
    * Bootstrap the server.
    * It first creates a server through the bootstrap.xml configuration file
    * and once the server is started, it then deploys the deployers.
    * 
    * @throws Exception
    */
   public static void bootstrap() throws Exception
   {
      server = new ServerImpl();
      Properties serverBootstrapProperties = createBootstrapEnv();
      server.init(serverBootstrapProperties);
      logger.trace("Inited profile3_1 server");
      long start = System.currentTimeMillis();
      server.start();
      long end = System.currentTimeMillis();
      logger.info("Profile3_1 server started in " + (end - start) + " milli sec.");

      // now deploy the Profile3_1 deployers
      deploy(new File(SERVER_PROFILE3_1_DEPLOYERS_DIR_PATH).toURL());
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
         logger.info("Profile3_1 server has been shutdown");
      }
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
      URL serverProfile3_1Home = mkdir(SERVER_PROFILE3_1_HOME_DIR_PATH);
      if (logger.isTraceEnabled())
      {
         logger.trace("Server Home is " + serverHome);
         logger.trace("Profile3_1 Home is " + serverProfile3_1Home);
      }

      mkdir(SERVER_PROFILE3_1_DATA_DIR_PATH);
      mkdir(SERVER_PROFILE3_1_LOG_DIR_PATH);
      mkdir(SERVER_PROFILE3_1_TMP_DIR_PATH);
      mkdir(SERVER_PROFILE3_1_TMP_DEPLOY_DIR_PATH);
      mkdir(SERVER_PROFILE3_1_TMP_NATIVE_DIR_PATH);

      Properties serverBootstrapProperties = new Properties();
      serverBootstrapProperties.put(ServerConfig.HOME_DIR, serverHome.toString());
      serverBootstrapProperties.put(ServerConfig.SERVER_HOME_DIR, serverProfile3_1Home.toString());

      URL profile3_1_ConfigDir = findDir(SERVER_PROFILE3_1_CONFIG_DIR_PATH);
      serverBootstrapProperties.put(ServerConfig.SERVER_CONFIG_URL, profile3_1_ConfigDir.toString());
      if (logger.isTraceEnabled())
      {
         logger.trace("Profile3_1 config dir is " + profile3_1_ConfigDir);
      }

      return serverBootstrapProperties;
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
      logger.debug("Deploying " + deployURL);
      MainDeployer mainDeployer = getMainDeployer();
      VirtualFile root = VFS.getRoot(deployURL);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      mainDeployer.deploy(deployment);
      mainDeployer.checkComplete(deployment);
      logger.debug("Completely deployed " + deployURL);

   }
   
   /**
    * Deploys a class.
    * 
    * TODO: Jaikiran - This is in testing phase, since i am not yet sure whether we have a
    * deployer in EJB3 which works only on class instead of .jar, .ear components.
    * The main intention to allow deploying a class is to avoid the additional jar/ear creation
    * during testcases
    * 
    * 
    * @param deployableClass
    * @throws Exception
    */
   protected static void deploy(Class<?> deployableClass) throws Exception
   {
      String classFQN = deployableClass.getName();
      String klass = classFQN.replaceAll("\\.", "/") + ".class";
      if (logger.isTraceEnabled())
      {
         logger.trace("Deploying class = " + klass);
      }
      URL classURL =Thread.currentThread().getContextClassLoader().getResource(klass);
      if (logger.isTraceEnabled())
      {
         logger.trace("URL form of class " + classFQN + " is = " + classURL);
      }
      deploy(classURL);
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
}
