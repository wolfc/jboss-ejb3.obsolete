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
package org.jboss.ejb3.embedded;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.Context;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.KernelAbstractionFactory;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.xml.BeanXMLDeployer;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.logging.Logger;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * This class is used to bootstrap the ejb3 container.
 * It find things by ClassLoader.getResource and will load
 * by default the embedded-jbosss-beans.xml file
 * and the ejb3-interceptors-aop.xml file.
 * <p/>
 * This is usually only used in standalone Java programs or Junit tests.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class EJB3StandaloneBootstrap
{
   private static final Logger log = Logger.getLogger(EJB3StandaloneBootstrap.class);
   
   private final static ObjectName DEFAULT_LOADER_NAME =
      ObjectNameFactory.create(ServerConstants.DEFAULT_LOADER_NAME);

   public static Kernel kernel;
   private static MBeanServer mbeanServer;
   private static EJB3StandaloneDeployer deployer;
   
   /** Whether we have warned about the initial context */
   private static boolean warned = false;

   public static Kernel getKernel()
   {
      return kernel;
   }

   private static ArrayList<KernelDeployment> deployments = new ArrayList<KernelDeployment>();
   private static ArrayList<URL> aopDeployments = new ArrayList<URL>();


   /**
    * Deploy a JBoss Microcontainer XML file.
    *
    * @param resource
    */
   public static void deployXmlResource(String resource)
   {
      try
      {
         BeanXMLDeployer deployer = new BeanXMLDeployer(kernel);
         URL bootstrap = Thread.currentThread().getContextClassLoader().getResource(resource);
         if (bootstrap == null)
            throw new RuntimeException("Cannot find " + resource);
         KernelDeployment deployment = deployer.deploy(bootstrap);
         deployments.add(0, deployment);
         deployer.validate();
      }
      catch (Throwable throwable)
      {
         throw new RuntimeException(throwable);
      }
   }
   
   public static Object getDeployment(String name)
   {
      Iterator allDeployments = deployments.iterator();
      while (allDeployments.hasNext())
      {
         KernelDeployment deployment = (KernelDeployment)allDeployments.next();
         Iterator contexts = deployment.getInstalledContexts().iterator();
         while (contexts.hasNext())
         {
            org.jboss.kernel.plugins.dependency.AbstractKernelControllerContext context = (org.jboss.kernel.plugins.dependency.AbstractKernelControllerContext)contexts.next();
            if (context.getName().equals(name))
               return context.getTarget();
         }
      }
      
      return null;
   }

   public static HashSet ignoredJars = new HashSet();

   static
   {
      ignoredJars.add("antlr-2.7.5H3.jar");
      ignoredJars.add("asm-attrs.jar");
      ignoredJars.add("asm.jar");
      ignoredJars.add("cglib-2.1.1.jar");
      ignoredJars.add("commons-collections.jar");
      ignoredJars.add("commons-logging-api.jar");
      ignoredJars.add("commons-logging.jar");
      ignoredJars.add("concurrent.jar");
      ignoredJars.add("dom4j.jar");
      ignoredJars.add("ejb3-persistence.jar");
      ignoredJars.add("hibernate3.jar");
      ignoredJars.add("hibernate-annotations.jar");
      ignoredJars.add("hibernate-entitymanager.jar");
      ignoredJars.add("hsqldb.jar");
      ignoredJars.add("javassist.jar");
      ignoredJars.add("jboss-annotations-ejb3.jar");
      ignoredJars.add("jboss-aop-jdk50.jar");
      ignoredJars.add("jboss-aspect-library-jdk50.jar");
      ignoredJars.add("jboss-common.jar");
      ignoredJars.add("jboss-common-jdbc-wrapper.jar");
      ignoredJars.add("jboss-container.jar");
      ignoredJars.add("jboss-dependency.jar");
      ignoredJars.add("jboss-ejb3.jar");
      ignoredJars.add("jboss-ejb3x.jar");
      ignoredJars.add("jboss-j2ee.jar");
      ignoredJars.add("jboss-j2se.jar");
      ignoredJars.add("jboss.jar");
      ignoredJars.add("jboss-jca.jar");
      ignoredJars.add("jboss-local-jdbc.jar");
      ignoredJars.add("jboss-microcontainer.jar");
      ignoredJars.add("jbossmq.jar");
      ignoredJars.add("jboss-remoting.jar");
      ignoredJars.add("jbosssx.jar");
      ignoredJars.add("jboss-system.jar");
      ignoredJars.add("jboss-transaction.jar");
      ignoredJars.add("jboss-xa-jdbc.jar");
      ignoredJars.add("jnpserver.jar");
      ignoredJars.add("log4j.jar");
      ignoredJars.add("resolver.jar");
      ignoredJars.add("trove.jar");
      ignoredJars.add("xercesImpl.jar");
      ignoredJars.add("xml-apis.jar");
      ignoredJars.add("jboss-ejb3-all.jar");
      ignoredJars.add("hibernate-all.jar");
      ignoredJars.add("thirdparty-all.jar");
   }


   public static Hashtable getInitialContextProperties()
   {
      Hashtable hash = null;
      ControllerContext context = kernel.getController().getInstalledContext("InitialContextProperties");
      if (context != null)
         hash = (Hashtable) context.getTarget();
      else if (warned == false)
      {
         log.warn("Could not find an configured InitialContextProperties, this is ok if your already have a correct jndi.properties file");
         warned = true;
      }
      return hash;
   }


   /**
    * Scan java.class.path System property for jars that contain EJB3 files.
    * <p/>
    * This is unportable and unreliable.  Use with caution.
    */
   public static void scanClasspath(String paths)
   {
      try
      {
         String classpath = System.getProperty("java.class.path");
         StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
         String[] split = paths.split(",");
         for (int i = 0; i < split.length; i++)
         {
            split[i] = split[i].trim();
         }

         deployer = createDeployer();

         while (tokenizer.hasMoreTokens())
         {
            String path = tokenizer.nextToken().trim();
            boolean found = false;
            for (String wantedPath : split)
            {
               if (path.endsWith(System.getProperty("file.separator") + wantedPath))
               {
                  found = true;
                  break;
               }
            }
            if (!found) continue;
            File fp = new File(path);
            if (ignoredJars.contains(fp.getName())) continue;
            URL archive = fp.toURL();
            deployer.getArchives().add(archive);
         }
         if (deployer.getArchives().size() == 0)
         {
            deployer = null;
            return;
         }
         deployer.create();
         deployer.start();

      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      if (!KernelErrors.validate(kernel)) throw new RuntimeException("Problems scanning classpath");

   }

   /**
    * Scan java.class.path System property for jars that contain EJB3 files.
    * <p/>
    * This is unportable and unreliable.  Use with caution.
    */
   public static void scanClasspath()
   {
      try
      {
         String classpath = System.getProperty("java.class.path");
         StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);

         deployer = createDeployer();

         while (tokenizer.hasMoreTokens())
         {
            String path = tokenizer.nextToken();
            File fp = new File(path);
            if (ignoredJars.contains(fp.getName())) continue;
            URL archive = fp.toURL();
            deployer.getArchives().add(archive);
         }
         if (deployer.getArchives().size() == 0)
         {
            deployer = null;
            return;
         }
         deployer.create();
         deployer.start();

      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      if (!KernelErrors.validate(kernel)) throw new RuntimeException("Problems scanning classpath");

   }

   /**
    * Create a deployer that you can start deploying to.  You must call create/start and stop/destroy to bootstrap
    * and shutdown
    *
    * @return
    */
   public static EJB3StandaloneDeployer createDeployer()
   {
      EJB3StandaloneDeployer deployer;
      deployer = new EJB3StandaloneDeployer();
      deployer.setJndiProperties(getInitialContextProperties());
      deployer.setKernel(kernel);
      deployer.setMbeanServer(mbeanServer);
      
      return deployer;
   }

   public static void shutdown()
   {
      try
      {
         if (deployer != null)
         {
            deployer.stop();
            deployer.destroy();
            deployer = null;
         }
         BeanXMLDeployer deployer = new BeanXMLDeployer(kernel);
         for (KernelDeployment deployment : deployments)
         {
            deployer.undeploy(deployment);
         }

         for (URL url : aopDeployments)
         {
            try
            {
               AspectXmlLoader.undeployXML(url);
            }
            catch (Exception e)
            {
               log.warn(e);
            }
         }
         kernel = null;
      }
      catch (Throwable throwable)
      {
         log.warn(throwable);
      }

   }
   
   private static void initInitialContext()
   {
      Properties jndiProps = new Properties();
      jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.LocalOnlyContextFactory");
      jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
      InitialContextFactory.setProperties(jndiProps);
   }

   /**
    * Bootstrap the EJB3 container by loading up JBoss Microcontainer files.
    *
    * @param configPath base path for default resources.  null or "" should be good enough.
    */
   public static void boot(String configPath)
   {
      initInitialContext();
      
      String basePath = "";
      if (configPath != null && !configPath.equals(""))
      {
         basePath = configPath;
         if (!configPath.endsWith("/"))
         {
            basePath += "/";
         }
      }
      
      try
      {
         URL bootstrap = Thread.currentThread().getContextClassLoader().getResource(basePath + "embedded-jboss-beans.xml");
         createKernel();
         loadMBeanServer();
         BeanXMLDeployer deployer = new BeanXMLDeployer(kernel);
         if (bootstrap == null)
            throw new RuntimeException("Cannot find embedded-jboss-beans.xml");
         deployments.add(0, deployer.deploy(bootstrap));
         deployer.validate();

         URL ejb3_interceptors = Thread.currentThread().getContextClassLoader().getResource(basePath + "ejb3-interceptors-aop.xml");
         if (ejb3_interceptors == null)
            throw new RuntimeException("Cannot find ejb3-interceptors-aop.xml");
         AspectXmlLoader.deployXML(ejb3_interceptors);
         aopDeployments.add(ejb3_interceptors);
         
         getDeployment("");
      }
      catch (Throwable throwable)
      {
         throw new RuntimeException(throwable);
      }
   }
   
   public static void loadMBeanServer() throws Exception
   {
      ControllerContext context = kernel.getController().getInstalledContext("MBeanServer");
       
      if (context != null)
      {
         mbeanServer = (MBeanServer) context.getTarget();
      }
      else
      {
         ArrayList servers = MBeanServerFactory.findMBeanServer(null);
         if (servers.size() == 0)
         {
            mbeanServer = MBeanServerFactory.createMBeanServer();
            org.jboss.mx.util.MBeanServerLocator.setJBoss(mbeanServer);
         }
      }
   }


   public static void createKernel()
           throws Exception
   {
      BasicBootstrap bootstrap1 = new BasicBootstrap();
      bootstrap1.run();
      kernel = bootstrap1.getKernel();
      KernelAbstractionFactory.setKernel(kernel);
   }
}
