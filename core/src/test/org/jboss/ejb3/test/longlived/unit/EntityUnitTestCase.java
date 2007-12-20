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
package org.jboss.ejb3.test.longlived.unit;

import org.jboss.ejb3.test.longlived.Customer;
import org.jboss.ejb3.test.longlived.ShoppingCart;
import org.jboss.ejb3.test.longlived.StatelessRemote;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: EntityUnitTestCase.java 61136 2007-03-06 09:24:20Z wolfc $
 */

public class EntityUnitTestCase
extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public EntityUnitTestCase(String name)
   {

      super(name);

   }

   public void testWithFlushMode() throws Exception
   {
      ShoppingCart test = (ShoppingCart) this.getInitialContext().lookup("ShoppingCartBean/remote");
      StatelessRemote remote = (StatelessRemote) this.getInitialContext().lookup("StatelessSessionBean/remote");
      Customer c;
      long id;


      id = test.createCustomer();
      c = remote.find(id);
      assertEquals("William", c.getName());
      c = test.find(id);
      assertEquals("William", c.getName());
      test.never();
      c = remote.find(id);
      assertEquals("William", c.getName());

      test.checkout();
      c = remote.find(id);
      assertEquals("Bob", c.getName());

      // make sure FlushMode

      test = (ShoppingCart) this.getInitialContext().lookup("ShoppingCartBean/remote");


      id = test.createCustomer();
      c = remote.find(id);
      assertEquals("William", c.getName());
      c = test.find(id);
      assertEquals("William", c.getName());
      test.update();
      c = remote.find(id);
      assertEquals("Bill", c.getName());
      c = test.find(id);
      assertEquals("Bill", c.getName());
      test.update2();
      c = remote.find(id);
      assertEquals("Billy", c.getName());
      c = test.find(id);
      assertEquals("Billy", c.getName());
      test.update3();
      c = remote.find(id);
      assertEquals("Bill Jr.", c.getName());
      c = test.find(id);
      assertEquals("Bill Jr.", c.getName());
      test.checkout();
   }

   public void testLongLivedSession() throws Exception
   {
      ShoppingCart test = (ShoppingCart) this.getInitialContext().lookup("ShoppingCartBean/remote");
      StatelessRemote remote = (StatelessRemote) this.getInitialContext().lookup("StatelessSessionBean/remote");
      Customer c;

      long id = test.createCustomer();
      c = remote.find(id);
      assertEquals("William", c.getName());
      c = test.find(id);
      assertEquals("William", c.getName());
      test.update();
      c = remote.find(id);
      assertEquals("Bill", c.getName());
      c = test.find(id);
      assertEquals("Bill", c.getName());
      test.update2();
      c = remote.find(id);
      assertEquals("Billy", c.getName());
      c = test.find(id);
      assertEquals("Billy", c.getName());
      test.update3();
      c = remote.find(id);
      assertEquals("Bill Jr.", c.getName());
      c = test.find(id);
      assertEquals("Bill Jr.", c.getName());
      test.setContainedCustomer();
      Thread.sleep(6000); // passivation
      assertTrue(remote.isPassivated());
      test.checkContainedCustomer();  
      test.findAndUpdateStateless();
      test.updateContained();
      remote.clearDestroyed();
      assertTrue(test.isContainedActivated());
      test.checkout();
      assertTrue(remote.isDestroyed());
   }

   public void testHibernateLongLivedSession() throws Exception
   {
      ShoppingCart test = (ShoppingCart) this.getInitialContext().lookup("HibernateShoppingCart");
      StatelessRemote remote = (StatelessRemote) this.getInitialContext().lookup("StatelessSessionBean/remote");
      Customer c;

      long id = test.createCustomer();
      c = remote.find(id);
      assertEquals("William", c.getName());
      c = test.find(id);
      assertEquals("William", c.getName());
      test.update();
      c = remote.find(id);
      assertEquals("Bill", c.getName());
      c = test.find(id);
      assertEquals("Bill", c.getName());
      test.update2();
      c = remote.find(id);
      assertEquals("Billy", c.getName());
      c = test.find(id);
      assertEquals("Billy", c.getName());
      test.update3();
      c = remote.find(id);
      assertEquals("Bill Jr.", c.getName());
      c = test.find(id);
      assertEquals("Bill Jr.", c.getName());
      test.checkout();
   }

   public void testHibernateWithFlushMode() throws Exception
   {
      ShoppingCart test = (ShoppingCart) this.getInitialContext().lookup("HibernateShoppingCart");
      StatelessRemote remote = (StatelessRemote) this.getInitialContext().lookup("StatelessSessionBean/remote");
      Customer c;
      long id;


      id = test.createCustomer();
      c = remote.find(id);
      assertEquals("William", c.getName());
      c = test.find(id);
      assertEquals("William", c.getName());
      test.never();
      c = remote.find(id);
      assertEquals("William", c.getName());

      test.checkout();
      c = remote.find(id);
      assertEquals("Bob", c.getName());

      // make sure FlushMode

      test = (ShoppingCart) this.getInitialContext().lookup("HibernateShoppingCart");


      id = test.createCustomer();
      c = remote.find(id);
      assertEquals("William", c.getName());
      c = test.find(id);
      assertEquals("William", c.getName());
      test.update();
      c = remote.find(id);
      assertEquals("Bill", c.getName());
      c = test.find(id);
      assertEquals("Bill", c.getName());
      test.update2();
      c = remote.find(id);
      assertEquals("Billy", c.getName());
      c = test.find(id);
      assertEquals("Billy", c.getName());
      test.update3();
      c = remote.find(id);
      assertEquals("Bill Jr.", c.getName());
      c = test.find(id);
      assertEquals("Bill Jr.", c.getName());
      test.checkout();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(EntityUnitTestCase.class, "longlived-test.jar");
   }

}
