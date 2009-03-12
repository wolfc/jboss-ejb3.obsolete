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
package org.jboss.ejb3.test.cache;

import java.io.File;

import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.jmx.CacheJmxWrapperMBean;
import org.jboss.cache.loader.FileCacheLoader;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Tester extends ServiceMBeanSupport implements TesterMBean
{
   public void test() throws Exception
   {
      ObjectName cacheON = new ObjectName("jboss.cache:service=EJB3TreeCache");
      CacheJmxWrapperMBean mbean = (CacheJmxWrapperMBean) MBeanProxy.get(CacheJmxWrapperMBean.class, cacheON, server);
      Cache cache = mbean.getCache();
//      PassivationEvictionPolicy policy = (PassivationEvictionPolicy) cache.getEvictionPolicy();
//      policy.createRegion("/mySFSB", 100, 1L);

      Fqn mysfbf1234 = Fqn.fromString("/mySFSB/1234");
      cache.put(mysfbf1234, "hello", "world");
      System.out.println("After PUT");
      Thread.sleep(5000);

      System.out.println("WAKE UP!");
      File fp = new File(System.getProperty(ServerConfig.SERVER_TEMP_DIR) + "/stateful/mySFSB." + FileCacheLoader.DIR_SUFFIX + "/1234." + FileCacheLoader.DIR_SUFFIX);
      System.out.println("exists in DB: " + fp.exists());
      if (!fp.exists()) throw new RuntimeException("No passivation happened.");
      System.out.println(cache.get(mysfbf1234, "hello"));
      System.out.println("exists in DB: " + fp.exists());
      if (fp.exists()) throw new RuntimeException("Should have been removed on activation.");
      if (cache.getRoot().hasChild(mysfbf1234))
      {
         cache.removeNode(Fqn.fromString("/mySFSB/1234"));
//         synchronized (policy)
//         {
//            policy.removeRegion("/mySFSB");
//         }
         cache.removeNode(Fqn.fromString("/mySFSB"));
      }
   }

   public void testSimpleRemote() throws Exception
   {
      SimpleStatefulRemote remote = (SimpleStatefulRemote) new InitialContext().lookup("SimpleStatefulBean/remote");
      remote.reset();
      remote.setState("hello");
      remote.longRunning();
      if (!"hello".equals(remote.getState())) throw new RuntimeException("failed state");
      if (!remote.getPostActivate()) throw new RuntimeException("failed to postActivate");
      if (!remote.getPrePassivate()) throw new RuntimeException("failed to prePassivate");

   }

   public void testSimpleLocal() throws Exception
   {
      SimpleStatefulLocal local = (SimpleStatefulLocal) new InitialContext().lookup("SimpleStatefulBean/local");
      local.reset();
      local.setState("hello");
      local.longRunning();
      if (!"hello".equals(local.getState())) throw new RuntimeException("failed state");
      if (!local.getPostActivate()) throw new RuntimeException("failed to postActivate");
      if (!local.getPrePassivate()) throw new RuntimeException("failed to prePassivate");

   }
}
