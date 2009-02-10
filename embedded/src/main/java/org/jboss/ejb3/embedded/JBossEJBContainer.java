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
package org.jboss.ejb3.embedded;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.ejb.EJBContainer;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.xml.BasicXMLDeployer;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class JBossEJBContainer extends EJBContainer
{
   private static final Logger log = Logger.getLogger(JBossEJBContainer.class);
   
   private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
   
   // stage 1
   private BasicBootstrap bootstrap = new BasicBootstrap();
   private Kernel kernel;
   private BasicXMLDeployer deployer;
   
   // stage 2
   private MainDeployer mainDeployer;
   
   public JBossEJBContainer(Map<?, ?> properties, String... modules) throws Throwable
   {
      try
      {
         bootstrap.run();
         kernel = bootstrap.getKernel();
         deployer = new BasicXMLDeployer(kernel);
         // bring the main deployer online
         deploy("META-INF/embedded-bootstrap-beans.xml");
         
         // we're at stage 2
         mainDeployer = getBean("MainDeployer", ControllerState.INSTALLED, MainDeployer.class);
         
         deploy("META-INF/ejb-deployers-beans.xml");
         
         deploy("META-INF/namingserver-beans.xml");
         
         deploy("META-INF/aop-beans.xml");
         
         deploy("META-INF/transactionmanager-beans.xml");
         
         deploy("META-INF/jpa-deployers-beans.xml");
         
         deploy("META-INF/ejb-container-beans.xml");
         
         deployMain("ejb3-interceptors-aop.xml");
         
         deployModules(modules);
      }
      catch(Throwable t)
      {
         close();
         throw t;
      }
   }
   
   @Override
   public void close()
   {
      if(mainDeployer != null)
      {
         mainDeployer.prepareShutdown();
         mainDeployer.shutdown();
         mainDeployer = null;
      }
      
      if(deployer != null)
      {
         deployer.shutdown();
         deployer = null;
      }
      kernel = null;
      bootstrap = null;
   }
   
   private KernelDeployment deploy(String name) throws Throwable
   {
      return deploy(getResource(name));
   }
   
   private KernelDeployment deploy(URL url) throws Throwable
   {
      log.info("Deploying " + url);
      KernelDeployment deployment = deployer.deploy(url);
      deployer.validate(deployment);
      return deployment;
   }
   
   private void deployMain(String name) throws DeploymentException, IllegalArgumentException, MalformedURLException, IOException
   {
      URL url = getResource(name);
      deployMain(url);
   }
   
   private void deployMain(URL url) throws DeploymentException, IOException
   {
      log.info("Deploying " + url);
      VirtualFile root = VFS.getRoot(url);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      mainDeployer.deploy(deployment);
      mainDeployer.checkComplete(deployment);
   }
   
   private void deployModules(String modules[]) throws MalformedURLException, IOException, DeploymentException
   {
      // TODO: deploy the world!
      if(modules == null)
         return;
      
      for(String module : modules)
      {
         deployMain(new URL(module));
      }
   }
   
   /**
    * Get a bean
    *
    * @param name the name of the bean
    * @param state the state of the bean
    * @return the bean
    * @throws IllegalStateException when the bean does not exist at that state
    */
   protected Object getBean(final Object name, final ControllerState state) throws IllegalStateException
   {
      KernelControllerContext context = getControllerContext(name, state);
      return context.getTarget();
   }
   
   /**
    * Get a bean
    *
    * @param <T> the expected type
    * @param name the name of the bean
    * @param state the state of the bean
    * @param expected the expected type
    * @return the bean
    * @throws ClassCastException when the bean can not be cast to the expected type
    * @throws IllegalStateException when the bean does not exist at that state
    */
   protected <T> T getBean(final Object name, final ControllerState state, final Class<T> expected) throws ClassCastException, IllegalStateException
   {
      if (expected == null)
         throw new IllegalArgumentException("Null expected");
      Object bean = getBean(name, state);
      return expected.cast(bean);
   }
   
   /**
    * Get a context
    *
    * @param name the name of the bean
    * @param state the state of the bean
    * @return the context
    * @throws IllegalStateException when the context does not exist at that state
    */
   protected KernelControllerContext getControllerContext(final Object name, final ControllerState state) throws IllegalStateException
   {
      KernelController controller = kernel.getController();
      KernelControllerContext context = (KernelControllerContext) controller.getContext(name, state);
      if (context == null)
         return handleNotFoundContext(controller, name, state);
      return context;
   }
   
   private URL getResource(String name) throws IllegalArgumentException
   {
      URL url = classLoader.getResource(name);
      if(url == null)
         throw new IllegalArgumentException("No resource named " + name + " found on " + classLoader);
      return url;
   }
   
   /**
    * Handle not found context.
    *
    * @param controller the controller
    * @param name the name of the bean
    * @param state the state of the bean
    * @return the context
    * @throws IllegalStateException when the context does not exist at that state
    */
   protected KernelControllerContext handleNotFoundContext(Controller controller, Object name, ControllerState state) throws IllegalStateException
   {
      throw new IllegalStateException("Bean not found " + name + " at state " + state + " in controller " + controller);
   }

}
