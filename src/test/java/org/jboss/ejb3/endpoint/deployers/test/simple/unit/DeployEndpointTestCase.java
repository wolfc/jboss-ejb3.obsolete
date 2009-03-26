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
package org.jboss.ejb3.endpoint.deployers.test.simple.unit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.jboss.bootstrap.microcontainer.ServerImpl;
import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.bootstrap.spi.microcontainer.MCServer;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.ejb3.endpoint.deployers.test.simple.Greeter;
import org.jboss.ejb3.endpoint.deployers.test.simple.StatefulGreeter;
import org.jboss.ejb3.endpoint.reflect.EndpointInvocationHandler;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DeployEndpointTestCase
{
   private static final Logger log = Logger.getLogger(DeployEndpointTestCase.class);
   
   private static MCServer server;
   private static MainDeployer mainDeployer;
   
   @AfterClass
   public static void afterClass()
   {
      if(server != null)
         server.shutdown();
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      server = new ServerImpl();
      
      String dir = mkdir("target/bootstrap");
      
      Properties props = new Properties();
      props.put(ServerConfig.HOME_DIR, dir);
      props.put(ServerConfig.SERVER_CONFIG_URL, findDir("src/test/resources/conf"));
      server.init(props);
      
      server.start();
      
      mainDeployer = (MainDeployer) server.getKernel().getController().getContext("MainDeployer", ControllerState.INSTALLED).getTarget();
      
      // TODO: another hack that simulates profile service going through deploy dir
      VirtualFile deployDir = VFS.getRoot(findDirURI("src/test/resources/deploy"));
      deployDir(deployDir);
      
      // TODO:
      deployResource("ejb3-interceptors-aop.xml");
      
      URL url = new File("src/main/resources").toURI().toURL();
      log.debug("url = " + url);
      deploy(url);
      
      deploy(getURLToTestClasses());
   }
   
   protected static void deployResource(String name) throws DeploymentException, IOException
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(name);
      if(url == null)
         throw new IllegalArgumentException("Resource " + name + " not found");
      deploy(url);
   }
   
   protected static void deploy(URL url) throws DeploymentException, IOException
   {
      log.info("Deploying " + url);
      VirtualFile root = VFS.getRoot(url);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      mainDeployer.deploy(deployment);
   }
   
   /**
    * Simulate the going through one deploy directory.
    * @param deployDir  the deploy directory       
    * @throws DeploymentException
    * @throws IOException
    */
   protected static void deployDir(VirtualFile deployDir) throws DeploymentException, IOException
   {
      log.info("Deploying directory " + deployDir);
      List<VirtualFile> files = deployDir.getChildren();
      VFSDeployment deployments[] = new VFSDeployment[files.size()];
      for(int i = 0; i < deployments.length; i++)
         deployments[i] = VFSDeploymentFactory.getInstance().createVFSDeployment(files.get(i));
      mainDeployer.deploy(deployments);
   }
   
   private static String findDir(String path) throws IOException
   {
      File file = new File(path);
      boolean success = file.isDirectory();
      if(!success)
         throw new IOException("failed to find " + path);
      return file.toURI().toString();
   }
   
   private static URI findDirURI(String path) throws IOException
   {
      File file = new File(path);
      boolean success = file.isDirectory();
      if(!success)
         throw new IOException("failed to find " + path);
      return file.toURI();
   }
   
   private static URL getURLToTestClasses()
   {
      try
      {
         String p = "org/jboss/ejb3/endpoint/deployers/test";
         URL url = Thread.currentThread().getContextClassLoader().getResource(p);
         String s = url.toString();
         return new URL(s.substring(0, s.length() - p.length()));
      }
      catch(MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
   }

   private static String mkdir(String path) throws IOException
   {
      File file = new File(path);
      boolean success = file.mkdirs() || file.isDirectory();
      if(!success)
         throw new IOException("failed to create " + path);
      return file.getAbsolutePath();
   }
   
   @Test
   public void test1() throws Throwable
   {
      // name is not important
      String name = "jboss.j2ee:jar=tests-classes,name=MyStatelessBean,service=EJB3_endpoint";
      ControllerState state = null;
      ControllerContext context = server.getKernel().getController().getContext(name, state);
      if(context.getState() != ControllerState.INSTALLED)
         context.getController().change(context, ControllerState.INSTALLED);
      Endpoint endpoint = (Endpoint) context.getTarget();
      EndpointInvocationHandler handler = new EndpointInvocationHandler(endpoint, null, Greeter.class);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { Greeter.class };
      Greeter bean = (Greeter) Proxy.newProxyInstance(loader, interfaces, handler);
      String result = bean.sayHi("Thingy");
      assertEquals("Hi Thingy from MyStatelessBean", result);
   }

   @Test
   public void testStateful() throws Throwable
   {
      // name is not important
      String name = "jboss.j2ee:jar=tests-classes,name=MyStatefulBean,service=EJB3_endpoint";
      ControllerState state = null;
      ControllerContext context = server.getKernel().getController().getContext(name, state);
      if(context.getState() != ControllerState.INSTALLED)
         context.getController().change(context, ControllerState.INSTALLED);
      Endpoint endpoint = (Endpoint) context.getTarget();
      Serializable session = endpoint.getSessionFactory().createSession(null, null);
      Class<?> businessInterface = StatefulGreeter.class;
      EndpointInvocationHandler handler = new EndpointInvocationHandler(endpoint, session, businessInterface);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { businessInterface };
      StatefulGreeter bean = (StatefulGreeter) Proxy.newProxyInstance(loader, interfaces, handler);
      bean.setName("testStateful");
      String result = bean.sayHi();
      assertEquals("Hi testStateful from MyStatefulBean", result);
   }
}
