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
package org.jboss.ejb3.test.microbench.unit;

import junit.framework.Test;
import org.jboss.ejb3.test.microbench.StatelessHomeRemote;
import org.jboss.ejb3.test.microbench.StatelessRemote;
import org.jboss.ejb3.test.microbench.StatelessRemote21;
import org.jboss.test.JBossTestCase;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class BenchUnitTestCase
        extends JBossTestCase
{
   public BenchUnitTestCase(String name)
   {

      super(name);

   }

   public void testLocalBenchmark() throws Exception
   {
	   MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Benchmark");
      Object[] params = {new Integer(100000)};
      String[] sig = {"int"};
      System.out.println("21Local: " + server.invoke(testerName, "benchLocalStateless21", params, sig));
      System.out.println("30Local: " + server.invoke(testerName, "benchLocalStateless30", params, sig));


   }

   public void testRemoteBenchmark() throws Exception
   {
      StatelessRemote remote = (StatelessRemote) getInitialContext().lookup("StatelessBean/remote");
      StatelessHomeRemote home = (StatelessHomeRemote) getInitialContext().lookup("StatelessBean21Remote");
      StatelessRemote21 remote21 = home.create();

      long start = System.currentTimeMillis();
      for (int i = 0; i < 1000; i++)
      {
         remote21.test(i);
      }
      long end = System.currentTimeMillis() - start;
      System.out.println("21Remote: " + end);

      start = System.currentTimeMillis();
      for (int i = 0; i < 1000; i++)
      {
         remote.test(i);
      }
      end = System.currentTimeMillis() - start;
      System.out.println("30Remote: " + end);

   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(BenchUnitTestCase.class, "benchmark-ejb3-test.sar");
   }

}
