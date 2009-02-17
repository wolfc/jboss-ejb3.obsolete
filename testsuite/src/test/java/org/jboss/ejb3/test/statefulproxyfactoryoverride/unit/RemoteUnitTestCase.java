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
package org.jboss.ejb3.test.statefulproxyfactoryoverride.unit;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.ejb3.test.statefulproxyfactoryoverride.NoOpException;
import org.jboss.ejb3.test.statefulproxyfactoryoverride.StatefulRemote;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */

public class RemoteUnitTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(RemoteUnitTestCase.class);

   public RemoteUnitTestCase(String name)
   {
      super(name);
   }

   public void testRemoteBindingProxyFactory() throws Exception
   {
      StatefulRemote stateful = (StatefulRemote) getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
      try
      {
         stateful.method();
      }
      catch (NoOpException noe)
      {
         // Expected
         log.info("Got expected: " + noe);
         return;
      }

      // Should not reach here
      TestCase.fail("Custom Proxy Factory was not used");
   }

   public void testDeploymentDescriptorRemoteBindingProxyFactory() throws Exception
   {
      StatefulRemote stateful = (StatefulRemote) getInitialContext().lookup("StatefulDeploymentDescriptor");
      assertNotNull(stateful);
      try
      {
         stateful.method();
      }
      catch (NoOpException noe)
      {
         // Expected
         log.info("Got expected: " + noe);
         return;
      }

      // Should not reach here
      TestCase.fail("Custom Proxy Factory was not used");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(RemoteUnitTestCase.class, "statefulproxyfactoryoverride-test.jar");
   }

}
