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
package org.jboss.ejb3.test.deployer.unit;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.ejb3.test.stateless.RunAsStateless;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class DeployerUnitTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(DeployerUnitTestCase.class);

   public DeployerUnitTestCase(String name)
   {
      super(name);
   }

   public void testDeployEjb3ExtensionOnly() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName on = new ObjectName("jboss.ejb3:service=EJB3Deployer");
      server.setAttribute(on, new Attribute("DeployEjb3ExtensionOnly", new Boolean(true)));
      Boolean value = (Boolean)server.getAttribute(on, "DeployEjb3ExtensionOnly");
      assertTrue(value.booleanValue());
      
      this.deploy("stateless-test.jar");
      try
      {
         RunAsStateless runAs = (RunAsStateless) getInitialContext().lookup("RunAsStatelessEjbName/remote");
         fail(".jar should not have deployed");
      } catch (javax.naming.NameNotFoundException e)
      {
      }
      this.undeploy("stateless-test.jar");
      
      server.setAttribute(on, new Attribute("DeployEjb3ExtensionOnly", new Boolean(false)));
      value = (Boolean)server.getAttribute(on, "DeployEjb3ExtensionOnly");
      assertFalse(value.booleanValue());
      
      this.deploy("stateless-test.jar");
      RunAsStateless runAs = (RunAsStateless) getInitialContext().lookup("RunAsStatelessEjbName/remote");
      this.undeploy("stateless-test.jar");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(DeployerUnitTestCase.class, "");
   }

}
