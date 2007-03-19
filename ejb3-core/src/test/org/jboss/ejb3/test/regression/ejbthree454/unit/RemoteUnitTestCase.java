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
package org.jboss.ejb3.test.regression.ejbthree454.unit;

import junit.framework.Test;
import org.jboss.ejb3.test.regression.ejbthree454.AEJB;
import org.jboss.ejb3.test.regression.ejbthree454.BEJB;
import org.jboss.test.JBossTestCase;

/**
 * Test EJBs of the same name and code that are deployed in different ears.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class RemoteUnitTestCase
        extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public RemoteUnitTestCase(String name)
   {

      super(name);

   }

   public void testScopedClassLoaders() throws Exception
   {
      AEJB remote = (AEJB) getInitialContext().lookup("ejbthree454-a/AEJBBean/remote");
      remote.doit();
      BEJB bremote = (BEJB) getInitialContext().lookup("ejbthree454-b/BEJBBean/remote");
      bremote.doit();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(RemoteUnitTestCase.class, "ejbthree454-a.ear, ejbthree454-b.ear");
   }

}
