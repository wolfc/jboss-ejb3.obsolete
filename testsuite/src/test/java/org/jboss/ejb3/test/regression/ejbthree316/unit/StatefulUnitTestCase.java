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
package org.jboss.ejb3.test.regression.ejbthree316.unit;

import org.jboss.test.JBossTestCase;
import org.jboss.ejb3.test.regression.ejbthree316.StatefulRemote;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class StatefulUnitTestCase
extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public StatefulUnitTestCase(String name)
   {

      super(name);

   }

   public void testStateful() throws Exception
   {
      StatefulRemote remote = (StatefulRemote) getInitialContext().lookup("StatefulBean/remote");
      System.out.println("Before DOIT testStateful");
      int id = remote.doit();
      System.out.println("After DOIT testStateful");
      Thread.sleep(5000);
      remote.find(id);
      System.out.println("After find testStateful");
   }

   public void testTransientStateful() throws Exception
   {
      StatefulRemote remote = (StatefulRemote) getInitialContext().lookup("Transient");
      int id = remote.doit();
      Thread.sleep(5000);
      remote.find(id);
   }

   public void testNonExtended() throws Exception
   {
      StatefulRemote remote = (StatefulRemote) getInitialContext().lookup("NonExtended");
      int id = remote.doit();
      Thread.sleep(5000);
      remote.find(id);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(StatefulUnitTestCase.class, "regression-ejbthree316.jar");
   }

}
