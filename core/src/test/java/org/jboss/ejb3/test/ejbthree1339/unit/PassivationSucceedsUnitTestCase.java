/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.ejbthree1339.unit;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.ejb3.test.ejbthree1339.TestPassivationRemote;
import org.jboss.test.JBossTestCase;

/**
 * PassivationSucceedsUnitTestCase
 *
 * Tests that passivation succeeds, and invocation
 * is possible upon reactivation
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class PassivationSucceedsUnitTestCase extends JBossTestCase
{
   public PassivationSucceedsUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(PassivationSucceedsUnitTestCase.class, "ejbthree1339.jar");
   }

   public void testCalculator() throws Exception
   {

      // Lookup and create stateful instance
      TestPassivationRemote remote = (TestPassivationRemote) getInitialContext()
            .lookup(TestPassivationRemote.JNDI_NAME);

      // Make an invocation
      TestCase.assertEquals("Returned result was not expected", TestPassivationRemote.EXPECTED_RESULT, remote
            .returnTrueString());

      // Sleep, allow SFSB to passivate
      Thread.sleep(5000L);

      // Make another invocation
      TestCase.assertEquals("Returned result was not expected", TestPassivationRemote.EXPECTED_RESULT, remote
            .returnTrueString());

      // Ensure the bean was passivated during the client sleep
      TestCase.assertTrue("SFSB was not passivated, check CacheConfig and client sleep time", remote
            .hasBeenPassivated());
   }
}
