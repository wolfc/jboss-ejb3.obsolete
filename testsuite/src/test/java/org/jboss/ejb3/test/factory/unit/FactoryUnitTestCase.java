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
package org.jboss.ejb3.test.factory.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.common.EJB3TestCase;
import org.jboss.ejb3.test.factory.Entity1;
import org.jboss.ejb3.test.factory.Entity2;
import org.jboss.ejb3.test.factory.MyService;
import org.jboss.ejb3.test.factory.Session1;
import org.jboss.ejb3.test.factory.Session2;
import org.jboss.ejb3.test.factory.Stateful1;
import org.jboss.ejb3.test.factory.Util;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class FactoryUnitTestCase extends EJB3TestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public FactoryUnitTestCase(String name)
   {

      super(name);

   }

   public void testMe() throws Exception
   {
      Session1 session1 = (Session1) this.getInitialContext().lookup("factory-test/Session1Bean/remote");
      Session2 session2 = (Session2) this.getInitialContext().lookup("factory-test/Session2Bean/remote");
      MyService service = (MyService) this.getInitialContext().lookup("factory-test/MyServiceBean/remote");

      int oneF = session1.create1FromFactory();
      int oneM = session1.create1FromManager();
      int twoF = session1.create2FromFactory();
      int twoM = session1.create2FromManager();
      session1.doUtil(new Util());

      session2.find1FromFactory(oneF);
      assertNotNull(session2.find1FromManager(oneM));
      session2.find2FromFactory(twoF);
      assertNotNull(session2.find2FromManager(twoM));
      assertNotNull(service.find2FromManager(twoM));
      assertNotNull(session2.findUtil1FromManager(1));
      assertNotNull(session2.findUtil2FromManager(2));

   }

   public void testExtended() throws Exception
   {
      Stateful1 stateful1 = (Stateful1) this.getInitialContext().lookup("factory-test/Stateful1Bean/remote");
      Session2 session2 = (Session2) this.getInitialContext().lookup("factory-test/Session2Bean/remote");

      int oneId = stateful1.create1();
      int twoId = stateful1.create2();

      stateful1.update1();
      stateful1.update2();

      {
         Entity1 one = session2.find1FromManager(oneId);
         assertEquals(one.getString(), "changed");

         Entity2 two = session2.find2FromManager(twoId);
         assertEquals(two.getString(), "changed");
      }

      stateful1.never();

      {
         Entity1 one = session2.find1FromManager(oneId);
         assertEquals(one.getString(), "changed");

         Entity2 two = session2.find2FromManager(twoId);
         assertEquals(two.getString(), "changed");
      }

      stateful1.checkout();

      {
         Entity1 one = session2.find1FromManager(oneId);
         assertEquals(one.getString(), "never");

         Entity2 two = session2.find2FromManager(twoId);
         assertEquals(two.getString(), "never");
      }
   }

   public void testExtended2() throws Exception
   {
      Stateful1 stateful1 = (Stateful1) this.getInitialContext().lookup("factory-test/Stateful1Bean/remote");
      Session2 session2 = (Session2) this.getInitialContext().lookup("factory-test/Session2Bean/remote");

      int oneId = stateful1.create1();
      int twoId = stateful1.create2();

      stateful1.update1();
      stateful1.update2();

      Entity1 one = session2.find1FromManager(oneId);
      assertEquals(one.getString(), "changed");

      Entity2 two = session2.find2FromManager(twoId);
      assertEquals(two.getString(), "changed");
      one.setString("never2");
      two.setString("never2");
      stateful1.never2(one, two);

      {
         Entity1 uno = session2.find1FromManager(oneId);
         assertEquals(uno.getString(), "changed");

         Entity2 dos = session2.find2FromManager(twoId);
         assertEquals(dos.getString(), "changed");
      }

      stateful1.checkout();

      {
         Entity1 uno = session2.find1FromManager(oneId);
         assertEquals(uno.getString(), "never2");

         Entity2 dos = session2.find2FromManager(twoId);
         assertEquals(dos.getString(), "never2");
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(FactoryUnitTestCase.class, "factory-test.ear");
   }

}
