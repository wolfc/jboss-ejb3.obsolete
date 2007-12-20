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
package org.jboss.ejb3.test.regressionHHH275.unit;

import org.jboss.ejb3.test.regressionHHH275.SearchDAORemote;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Test reentrant remote call.  There was a bug that remoting didn't route locally
 * and the tx propagation was happening when it shouldn't
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: HHH275UnitTestCase.java 61136 2007-03-06 09:24:20Z wolfc $
 */

public class HHH275UnitTestCase
extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public HHH275UnitTestCase(String name)
   {

      super(name);

   }

   public void testOneToOne() throws Exception
   {
      SearchDAORemote remote = (SearchDAORemote) getInitialContext().lookup("SearchDAO/remote");
      remote.create();
      assertEquals(0, remote.find());
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(HHH275UnitTestCase.class, "regressionHHH275-test.jar");
   }

}
