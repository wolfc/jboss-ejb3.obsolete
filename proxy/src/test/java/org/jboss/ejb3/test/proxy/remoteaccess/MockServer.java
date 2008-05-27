package org.jboss.ejb3.test.proxy.remoteaccess;

import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.ejb3.test.proxy.common.Utils;
import org.jboss.ejb3.test.proxy.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessBean;
import org.jboss.logging.Logger;

/**
 * MockServer
 * 
 * Launches a new MC Bootstrap, EJB Containers, and performs
 * all initialization to mock a remote server environment
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MockServer
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(MockServer.class);

   private static MockServer server;

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private EmbeddedTestMcBootstrap bootstrap;

   /**
    * Name of the SLSB Container for these tests
    */
   private String containerName;

   /**
    * The Test Class using this launcher
    */
   private Class<?> testClass;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    */
   public MockServer(Class<?> testClass)
   {
      this.setTestClass(testClass);
   }

   // --------------------------------------------------------------------------------||
   // Main ---------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Runtime Entry Point
    * 
    * @param args
    */
   public static void main(String... args)
   {

      // Assert test class passed in
      assert args.length == 1 : "String fully-qualified name of test class is the required first argument";

      // Get Test Class
      String testClassname = args[0];
      Class<?> testClass = null;
      try
      {
         testClass = Class.forName(testClassname);
      }
      catch (ClassNotFoundException cnfe)
      {
         throw new RuntimeException("Specified Test Class, \"" + testClassname + "\" could not be found", cnfe);
      }

      // Create a new Launcher
      MockServer launcher = new MockServer(testClass);
      MockServer.setServer(server);

      // Initialize the launcher in a new Thread
      new Startup(launcher).start();

   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Initializes the instance by starting up an MC Bootstrap, 
    * deploying relevant *-beans.xml, creating and installing EJB Containers
    */
   protected void initialize() throws Throwable
   {

      // Switch up to the hacky CL so that "jndi.properties" is not loaded
      ClassLoader olderLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(new JndiPropertiesRedirectClassloader());

      // Create and set a new MC Bootstrap 
      this.setBootstrap(EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap());

      // Add a Shutdown Hook
      Runtime.getRuntime().addShutdownHook(new ShutdownHook());

      // Bind the Ejb3Registrar
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));

      // Deploy *-beans.xml
      this.getBootstrap().deploy(this.getTestClass());

      // Create a SLSB Container
      StatelessContainer container = Utils.createSlsb(MyStatelessBean.class);
      log.info("Created SLSB Container: " + container.getName());
      this.setContainerName(container.getName());

      // Install into MC
      this.getBootstrap().installInstance(container.getName(), container);

      // Restore old CL
      Thread.currentThread().setContextClassLoader(olderLoader);

   }

   // --------------------------------------------------------------------------------||
   // Inner Classes ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected static class Startup extends Thread implements Runnable
   {

      // --------------------------------------------------------------------------------||
      // Instance Members ---------------------------------------------------------------||
      // --------------------------------------------------------------------------------||

      private MockServer launcher;

      // --------------------------------------------------------------------------------||
      // Constructor --------------------------------------------------------------------||
      // --------------------------------------------------------------------------------||

      /**
       * Constructor
       */
      public Startup(MockServer launcher)
      {
         this.setLauncher(launcher);
      }

      // --------------------------------------------------------------------------------||
      // Overridden Implementations -----------------------------------------------------||
      // --------------------------------------------------------------------------------||

      /**
       * Starts the Remote Launcher
       */
      @Override
      public void run()
      {
         // Initialize
         try
         {
            this.getLauncher().initialize();
         }
         catch (Throwable e)
         {
            throw new RuntimeException("Could not initialize " + this.getLauncher(), e);
         }

         // Run
         while (true);
      }

      // --------------------------------------------------------------------------------||
      // Accessors / Mutators -----------------------------------------------------------||
      // --------------------------------------------------------------------------------||

      public MockServer getLauncher()
      {
         return launcher;
      }

      public void setLauncher(MockServer launcher)
      {
         this.launcher = launcher;
      }
   }

   /**
    * Shutdown Hook for the MockServer
    */
   protected static class ShutdownHook extends Thread implements Runnable
   {

      // --------------------------------------------------------------------------------||
      // Overridden Implementations -----------------------------------------------------||
      // --------------------------------------------------------------------------------||

      /**
       * Shuts down the Bootstrap
       */
      @Override
      public void run()
      {
         getServer().bootstrap.shutdown();
      }
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public String getContainerName()
   {
      return this.containerName;
   }

   public void setContainerName(String containerName)
   {
      this.containerName = containerName;
   }

   public EmbeddedTestMcBootstrap getBootstrap()
   {
      return this.bootstrap;
   }

   public void setBootstrap(EmbeddedTestMcBootstrap bootstrap)
   {
      this.bootstrap = bootstrap;
   }

   public Class<?> getTestClass()
   {
      return testClass;
   }

   public void setTestClass(Class<?> testClass)
   {
      this.testClass = testClass;
   }

   public static MockServer getServer()
   {
      return server;
   }

   public static void setServer(MockServer server)
   {
      MockServer.server = server;
   }

}