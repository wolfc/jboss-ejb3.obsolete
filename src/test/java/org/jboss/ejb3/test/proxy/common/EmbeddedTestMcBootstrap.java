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
package org.jboss.ejb3.test.proxy.common;

import java.net.URL;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.xml.BasicXMLDeployer;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.logging.Logger;

/**
 * EmbeddedTestMcBootstrap
 * 
 * A MicroContainer Bootstrap for general use in testing 
 * runtimes
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EmbeddedTestMcBootstrap extends BasicBootstrap
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(EmbeddedTestMcBootstrap.class);

   private static final String DEFAULT_SUFFIX_DEPLOYABLE_XML = "-beans.xml";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private BasicXMLDeployer deployer;

   private Thread shutdownHook;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public EmbeddedTestMcBootstrap()
   {
      super();
   }

   // --------------------------------------------------------------------------------||
   // Static Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates, runs, and returns a new Embedded MC Bootstrap 
    */
   public static EmbeddedTestMcBootstrap createEmbeddedMcBootstrap()
   {
      EmbeddedTestMcBootstrap bootstrap = new EmbeddedTestMcBootstrap();
      log.debug("Starting " + bootstrap + "...");
      bootstrap.run();
      log.info("Started: " + bootstrap);
      return bootstrap;
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   protected void bootstrap() throws Throwable
   {
      // Call super implementation
      super.bootstrap();

      // Create and set an XML Deployer
      this.setDeployer(new BasicXMLDeployer(this.getKernel()));

      // Add a shutdown hook
      shutdownHook = new ShutdownDeployerThread();
      Runtime.getRuntime().addShutdownHook(shutdownHook);
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Deploys the specified URL
    * 
    * @param url
    */
   public void deploy(URL url)
   {
      try
      {
         log.debug("Deploying " + url.toString() + "...");
         this.getDeployer().deploy(url);
         log.info("Deployed: " + url.toString());
      }
      catch (Throwable e)
      {
         throw new RuntimeException("Could not deploy " + url.toString(), e);
      }
   }

   /**
    * Deploys the specified URL expressed as a String
    * 
    * @param url
    */
   public void deploy(ClassLoader cl, String url)
   {
      URL deployable = this.getResource(cl, url);
      this.deploy(deployable);
   }

   /**
    * Deploy a URL in the form "fullyQualifiedClassName-beans.xml" constructed
    * from the specified testClass
    * 
    * @param testClass
    */
   public void deploy(Class<?> testClass)
   {
      this.deploy(testClass, null);
   }

   /**
    * Deploy a URL in the form "packageName.filename-beans.xml" constructed
    * from the specified testClass and filename
    * 
    * @param testClass
    * @param filename
    */
   public void deploy(Class<?> testClass, String filename)
   {
      this.deploy(testClass.getClassLoader(), this.getDeployableXmlUrl(testClass, filename));
   }

   /**
    * @param name
    */
   public <T> T lookup(String name, Class<T> expectedType) throws Throwable
   {
      KernelController controller = getKernel().getController();
      ControllerContext context = controller.getContext(name, null);
      controller.change(context, ControllerState.INSTALLED);
      if (context.getError() != null)
         throw context.getError();

      if (context.getState() != ControllerState.INSTALLED)
      {
         log.error(context.getDependencyInfo().getUnresolvedDependencies(null));
      }
      // TODO: it can be stalled because of dependencies
      assert context.getState() == ControllerState.INSTALLED;

      return expectedType.cast(context.getTarget());
   }

   /**
    * Undeploys the specified URL
    * 
    * @param url
    */
   public void undeploy(URL url)
   {
      try
      {
         log.debug("Undeploying " + url.toString() + "...");
         this.getDeployer().undeploy(url);
         log.info("Undeployed: " + url.toString());
      }
      catch (Throwable e)
      {
         throw new RuntimeException("Could not undeploy " + url.toString(), e);
      }
   }

   /**
    * Undeploys the specified URL expressed as a String
    * 
    * @param url
    */
   public void undeploy(ClassLoader cl, String url)
   {
      URL deployable = this.getResource(cl, url);
      this.undeploy(deployable);
   }

   /**
    * Undeploy a URL in the form "fullyQualifiedClassName-beans.xml" constructed
    * from the specified testClass
    * 
    * @param testClass
    */
   public void undeploy(Class<?> testClass)
   {
      this.undeploy(testClass, null);
   }

   /**
    * Undeploy a URL in the form "packageName.filename-beans.xml" constructed
    * from the specified testClass and filename
    * 
    * @param testClass
    * @param filename
    */
   public void undeploy(Class<?> testClass, String filename)
   {
      this.undeploy(testClass.getClassLoader(), this.getDeployableXmlUrl(testClass, filename));
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected BasicXMLDeployer getDeployer()
   {
      return deployer;
   }

   protected void setDeployer(BasicXMLDeployer deployer)
   {
      this.deployer = deployer;
   }

   // --------------------------------------------------------------------------------||
   // Inner Classes ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * ShutdownDeployerThread
    * 
    * A Simple Thread that, when run, will shut down its deployer
    *
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   protected final class ShutdownDeployerThread extends Thread
   {
      @Override
      public void run()
      {
         super.run();

         shutdownHook();
      }

   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private String getDeployableXmlUrl(Class<?> clazz, String filename)
   {
      // Initialize
      StringBuffer url = new StringBuffer();

      // If the filename is specified
      if (filename != null)
      {
         // Assemble filename in form "packagename.filename"
         url.append(clazz.getClass().getPackage().toString());
         url.append('.');
         url.append(filename);
      }
      // Use default filename for the test class
      else
      {
         // Assemble filename in form "fullyQualifiedClassName"
         url.append(clazz.getName());
      }

      // Make a String
      String flatten = url.toString();

      // Adjust for filename structure instead of package structure
      flatten = flatten.replace('.', '/');

      // Append Suffix
      flatten = flatten + EmbeddedTestMcBootstrap.DEFAULT_SUFFIX_DEPLOYABLE_XML;

      // Return
      return flatten;
   }

   private URL getResource(ClassLoader cl, String resource)
   {
      // Ensure specified
      assert cl != null : "Specified " + ClassLoader.class.getSimpleName() + " is null";
      assert resource != null && !resource.equals("") : "Resource must be specified";

      // Obtain URL
      URL url = cl.getResource(resource);
      if (url == null)
      {
         throw new RuntimeException("Resource \"" + resource + "\" could not be obtained from current classloader");
      }
      return url;
   }

   public void installInstance(String name, Object instance) throws Throwable
   {
      BeanMetaDataBuilder bmdb = BeanMetaDataBuilder.createBuilder(name, instance.getClass().getName());
      this.getKernel().getController().install(bmdb.getBeanMetaData(),instance);
      log.info("Installed in MC at \"" + name + "\": " + instance);
   }

   /**
    * Perform a clean shutdown. 
    */
   public void shutdown()
   {
      // remove the hook
      Runtime.getRuntime().removeShutdownHook(shutdownHook);

      // and call it.
      shutdownHook();
   }

   private void shutdownHook()
   {
      log.debug("Shutting down " + this.deployer + "...");
      getDeployer().shutdown();
      log.info("Shut down: " + getDeployer());
   }
}
