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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.xml.BeanXMLDeployer;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ServletBootstrapListener implements ServletContextListener
{
   private static final Logger log = Logger.getLogger(ServletBootstrapListener.class);

   protected Kernel kernel;

   protected List<KernelDeployment> deployments = new ArrayList<KernelDeployment>();
   protected List<URL> aopDeployments = new ArrayList<URL>();


   protected void createKernel()
           throws Exception
   {
      BasicBootstrap bootstrap1 = new BasicBootstrap();
      bootstrap1.run();
      kernel = bootstrap1.getKernel();
   }

   protected void deployXmlResource(String resource)
   {
      try
      {
         BeanXMLDeployer deployer = new BeanXMLDeployer(kernel);
         URL bootstrap = Thread.currentThread().getContextClassLoader().getResource(resource);
         if (bootstrap == null)
            throw new RuntimeException("Cannot find " + resource);
         KernelDeployment deployment = deployer.deploy(bootstrap);
         deployer.validate();
         deployments.add(0, deployment);
      }
      catch (Throwable throwable)
      {
         throwable.printStackTrace();
         throw new RuntimeException(throwable);
      }

   }

   public static HashSet ignoredJars = new HashSet();

   static
   {
      ignoredJars.add("/WEB-INF/lib/antlr-2.7.5H3.jar");
      ignoredJars.add("/WEB-INF/lib/asm-attrs.jar");
      ignoredJars.add("/WEB-INF/lib/asm.jar");
      ignoredJars.add("/WEB-INF/lib/cglib-2.1.1.jar");
      ignoredJars.add("/WEB-INF/lib/commons-collections.jar");
      ignoredJars.add("/WEB-INF/lib/commons-logging-api.jar");
      ignoredJars.add("/WEB-INF/lib/commons-logging.jar");
      ignoredJars.add("/WEB-INF/lib/concurrent.jar");
      ignoredJars.add("/WEB-INF/lib/dom4j.jar");
      ignoredJars.add("/WEB-INF/lib/ejb3-persistence.jar");
      ignoredJars.add("/WEB-INF/lib/hibernate3.jar");
      ignoredJars.add("/WEB-INF/lib/hibernate-annotations.jar");
      ignoredJars.add("/WEB-INF/lib/hibernate-entitymanager.jar");
      ignoredJars.add("/WEB-INF/lib/hsqldb.jar");
      ignoredJars.add("/WEB-INF/lib/javassist.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-annotations-ejb3.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-aop-jdk50.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-aspect-library-jdk50.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-common.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-common-jdbc-wrapper.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-container.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-dependency.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-ejb3.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-ejb3x.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-j2ee.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-j2se.jar");
      ignoredJars.add("/WEB-INF/lib/jboss.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-jca.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-local-jdbc.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-microcontainer.jar");
      ignoredJars.add("/WEB-INF/lib/jbossmq.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-remoting.jar");
      ignoredJars.add("/WEB-INF/lib/jbosssx.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-system.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-transaction.jar");
      ignoredJars.add("/WEB-INF/lib/jboss-xa-jdbc.jar");
      ignoredJars.add("/WEB-INF/lib/jnpserver.jar");
      ignoredJars.add("/WEB-INF/lib/log4j.jar");
      ignoredJars.add("/WEB-INF/lib/resolver.jar");
      ignoredJars.add("/WEB-INF/lib/trove.jar");
      ignoredJars.add("/WEB-INF/lib/xercesImpl.jar");
      ignoredJars.add("/WEB-INF/lib/xml-apis.jar");
   }

   private EJB3StandaloneDeployer deployer;


   public void contextInitialized(ServletContextEvent servletContextEvent)
   {
      try
      {
         createKernel();

         ServletContext servletContext = servletContextEvent.getServletContext();
         String aop = (String) servletContext.getInitParameter("jboss-aop-deployments");

         if (aop == null) aop = "ejb3-interceptors-aop.xml";

         StringTokenizer tokenizer = new StringTokenizer(aop, ",");
         while (tokenizer.hasMoreTokens())
         {
            String token = tokenizer.nextToken().trim();
            log.debug("deploying aop xml: " + token);
            URL url = Thread.currentThread().getContextClassLoader().getResource(token);
            try
            {
               AspectXmlLoader.deployXML(url);
               aopDeployments.add(0, url);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
         }

         String deployments = (String) servletContext.getInitParameter("jboss-kernel-deployments");

         if (deployments == null) deployments = "embedded-jboss-beans.xml";

         tokenizer = new StringTokenizer(deployments, ",");
         while (tokenizer.hasMoreTokens())
         {
            String token = tokenizer.nextToken().trim();
            log.debug("deploying kernel xml: " + token);
            deployXmlResource(token);
         }

         String scan = (String) servletContext.getInitParameter("automatic-scan");
         if (scan != null && !scan.equals("false")) return;

         Set libJars = servletContext.getResourcePaths("/WEB-INF/lib");
         deployer = new EJB3StandaloneDeployer();
         deployer.setJndiProperties(getInitialContextProperties());
         deployer.setKernel(kernel);
         for (Object jar : libJars)
         {
            if (ignoredJars.contains(jar)) continue;
            URL archive = servletContext.getResource((String)jar);
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
         log.error(e);
         throw new RuntimeException(e);
      }

      if (!KernelErrors.validate(kernel)) throw new RuntimeException("Problems scanning ServletContext.getResourcePaths");
   }

   protected Hashtable getInitialContextProperties()
   {
      Hashtable hash = null;
      ControllerContext context = kernel.getController().getInstalledContext("InitialContextProperties");
      if (context != null)
         hash = (Hashtable) context.getTarget();
      else
         log.warn("could not find an configured InitialContextProperties, this is ok if your already have a correct jndi.properties file");
      return hash;
   }


   public void contextDestroyed(ServletContextEvent servletContextEvent)
   {
      try
      {
         if (deployer != null)
         {
            deployer.stop();
            deployer.destroy();
         }
         BeanXMLDeployer deployer = new BeanXMLDeployer(kernel);
         for (KernelDeployment deployment : deployments)
         {
            deployer.undeploy(deployment);
         }

         for (URL url : aopDeployments)
         {
            AspectXmlLoader.undeployXML(url);
         }
      }
      catch (Throwable throwable)
      {
         throwable.printStackTrace();
         throw new RuntimeException(throwable);
      }
   }
}
