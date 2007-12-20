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
package org.jboss.ejb3.test.regression.ejbthree625.unit;

import org.jboss.test.JBossTestCase;
import org.jboss.ejb3.test.regression.ejbthree290.DAO;
import org.jboss.ejb3.test.regression.ejbthree290.MyEntity;
import org.jboss.ejb3.test.regression.ejbthree290.unit.Ejb290UnitTestCase;
import org.jboss.ejb3.test.regression.ejbthree625.Silly;
import junit.framework.Test;

/**
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: Ejb625UnitTestCase.java 61136 2007-03-06 09:24:20Z wolfc $
 */

public class Ejb625UnitTestCase
extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public Ejb625UnitTestCase(String name)
   {

      super(name);

   }

   public void testDefaultInterceptors() throws Exception
   {
      Silly silly = (Silly)getInitialContext().lookup("SillyBean/remote");
      assertEquals(2, silly.add(1, 1));
      silly.clear();
      assertEquals(0, silly.sub(1, 1));
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(Ejb625UnitTestCase.class, "regression-ejbthree625-test.jar");
   }

}
