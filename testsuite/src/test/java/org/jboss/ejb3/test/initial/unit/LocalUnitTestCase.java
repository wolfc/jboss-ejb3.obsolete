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
package org.jboss.ejb3.test.initial.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class LocalUnitTestCase
        extends JBossTestCase
{
   private static Logger log = Logger.getLogger(LocalUnitTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public LocalUnitTestCase(String name)
   {

      super(name);

   }

   public void testSimple() throws Exception
   {
	   MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=initial");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "test", params, sig);
      server.invoke(testerName, "statefulTest", params, sig);
      server.invoke(testerName, "testInterceptors", params, sig);
      server.invoke(testerName, "testCallbacks", params, sig);
      server.invoke(testerName, "testSLSBCollocation", params, sig);
      server.invoke(testerName, "testSFSBCollocation", params, sig);
   }

   public void testTx() throws Exception
   {
	   MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=TxTester,test=initial");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testTransactions", params, sig);
   }

   public void testSecurityAssociation() throws Exception
   {
	   MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=SecurityTester,test=initial");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testSecurityAssociation", params, sig);
   }
   
   public void testSecurityClient() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=SecurityTester,test=initial");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testSecurityClient", params, sig);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(LocalUnitTestCase.class, "initial-ejb3-test.sar");
   }

}
