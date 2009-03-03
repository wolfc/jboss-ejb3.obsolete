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
package org.jboss.ejb3.timerservice.quartz.test.simple.unit;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.bootstrap.microcontainer.ServerImpl;
import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.bootstrap.spi.microcontainer.MCServer;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.spi.TimerServiceFactory;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
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
public class SimpleUnitTestCase
{
   private static Logger log = Logger.getLogger(SimpleUnitTestCase.class);
   
   private static MCServer server;
   
   private static String findDir(String path) throws IOException
   {
      File file = new File(path);
      boolean success = file.isDirectory();
      if(!success)
         throw new IOException("failed to find " + path);
      return file.toURI().toString();
   }
   
   private static String mkdir(String path) throws IOException
   {
      File file = new File(path);
      boolean success = file.mkdirs() || file.isDirectory();
      if(!success)
         throw new IOException("failed to create " + path);
      return file.getAbsolutePath();
   }
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      server = new ServerImpl();
      
      Properties props = new Properties();
      String dir = mkdir("target/bootstrap");
      mkdir("target/bootstrap/server/default");
      //mkdir("target/bootstrap/server/default/deploy");
      mkdir("target/bootstrap/server/default/data");
      mkdir("target/bootstrap/server/default/log");
      mkdir("target/bootstrap/server/default/tmp");
      mkdir("target/bootstrap/server/default/tmp/deploy");
      mkdir("target/bootstrap/server/default/tmp/native");
      log.info("dir = " + dir);
      props.put(ServerConfig.HOME_DIR, dir);
      props.put(ServerConfig.SERVER_CONFIG_URL, findDir("src/test/resources/conf"));
      server.init(props);
      
      server.start();
      
      MainDeployer mainDeployer = (MainDeployer) server.getKernel().getController().getContext("MainDeployer", ControllerState.INSTALLED).getTarget();
      
      URL url = new File("src/main/resources").toURI().toURL();
      log.debug("url = " + url);
      VirtualFile root = VFS.getRoot(url);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      mainDeployer.deploy(deployment);
      mainDeployer.checkComplete(deployment);
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if(server != null)
         server.shutdown();
   }
   
   @Test
   public void test1() throws Exception
   {
      final Semaphore semaphore = new Semaphore(0);
      
      TimedObjectInvoker dummy = new TimedObjectInvoker()
      {
         public void callTimeout(Timer timer) throws Exception
         {
            log.info("Timeout " + timer);
            semaphore.release();
         }

         public String getTimedObjectId()
         {
            return "dummy";
         }
      };
      
      TimerServiceFactory factory = getTimerServiceFactory();
      
      TimerService timerService = factory.createTimerService(dummy);
      
      factory.restoreTimerService(timerService);
      
      Timer timer = timerService.createTimer(1000, "Hello world");
      log.info("timer = " + timer);
      
      boolean success = semaphore.tryAcquire(10, TimeUnit.SECONDS);
      
      factory.suspendTimerService(timerService);
      
      assertTrue(success);
   }

   private static TimerServiceFactory getTimerServiceFactory()
   {
      Set<KernelControllerContext> candidates = server.getKernel().getController().getContexts(TimerServiceFactory.class, ControllerState.INSTALLED);
      if(candidates.size() == 0)
         throw new IllegalStateException("No TimerServiceFactory installed");
      if(candidates.size() > 1)
         throw new IllegalStateException("Multiple TimerServiceFactories found " + candidates);
      return (TimerServiceFactory) candidates.iterator().next().getTarget();
   }
}
