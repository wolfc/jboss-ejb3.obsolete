/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1028.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.common.EJB3TestCase;
import org.jboss.ejb3.test.ejbthree1028.ATM;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class LazyTransactionEnlistmentUnitTestCase extends EJB3TestCase
{
   public LazyTransactionEnlistmentUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(LazyTransactionEnlistmentUnitTestCase.class, "ejbthree1028.jar");
   }
   
   public void test1() throws Exception
   {
      ATM atm = lookup("ATMBean/remote", ATM.class);
      // if only
      long id = atm.createAccount(1000000);
      System.out.println("*** id " + id);
      double balance = atm.getBalance(id);
      System.out.println("*** balance " + balance);
      assertEquals(1000000, balance);
      
      balance = atm.depositTwiceWithRollback(id, 125000, 250000);
      System.out.println("*** balance " + balance);
      // the entity state itself won't be rolled back
      assertEquals(1375000, balance);
      balance = atm.getBalance(id);
      System.out.println("*** balance " + balance);
      assertEquals(1125000, balance);
   }
   
   public void testRawSQL() throws Exception
   {
      ATM atm = lookup("ATMBean/remote", ATM.class);
      // if only
      long id = atm.createAccount(1000000);
      System.out.println("*** id " + id);
      double balance = atm.getBalance(id);
      System.out.println("*** balance " + balance);
      assertEquals(1000000, balance);
      
      balance = atm.withdrawTwiceWithRollback(id, 125000, 250000);
      System.out.println("*** balance " + balance);
      balance = atm.getBalance(id);
      System.out.println("*** balance " + balance);
      assertEquals(875000, balance);
   }
}
