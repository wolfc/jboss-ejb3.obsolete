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
package org.jboss.ejb3.test.servicedependency.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.ejb3.test.stateless.RunAsStateless;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

import org.jboss.ejb3.test.servicedependency.Account;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision: 61136 $
 */
public class ServiceUnitTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(ServiceUnitTestCase.class);

   public ServiceUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testNoDependency() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Account account = (Account) jndiContext.lookup("AccountBean/remote");
      assertNotNull(account);
      
      account.debit("account", 1);
   }
   
   public void testDependency() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("acme:service=pinnumber");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "createRandom", params, sig);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ServiceUnitTestCase.class, "servicedependency.jar");
   }

}
