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
package org.jboss.ejb3.test.bank;

import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Resource;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import org.jboss.logging.Logger;

/**
 * @see <related>
 * @author $Author: wolfc $
 * @version $Revision: 61136 $
 */
public class TellerBean implements Teller
{
   private static final Logger log = Logger.getLogger(TellerBean.class);

   @Resource private TimerService ts;
   private TransactionManager tm;
   private Bank bank;
   private boolean constructed = false;
   private String defaultValue = "original";

   public boolean isConstructed()
   {
      return constructed;
   }

   public void setTransactionManager(TransactionManager tm)
   {
      this.tm = tm;
      System.out.println("TransactionManager set: " + tm);
   }

   public void transfer(Account from, Account to, float amount)
         throws BankException
   {
      try
      {
         from.withdraw(amount);
         to.deposit(amount);
      } catch (Exception e)
      {
         throw new BankException("Could not transfer " + amount + " from "
                                 + from + " to " + to, e);
      }
   }

   public Account createAccount(Customer customer, float balance)
         throws BankException
   {
      try
      {
        Bank bank = (Bank) new InitialContext()
               .lookup(Bank.JNDI_NAME);

         return null;
      } catch (Exception e)
      {
         log.debug("failed", e);
         throw new BankException("Could not create account", e);
      }
   }

   public Account getAccount(Customer customer, float balance)
         throws BankException
   {
      try
      {
         // Check for existing account
         Collection accounts = customer.getAccounts();
         if (accounts.size() > 0)
         {
            Iterator i = accounts.iterator();
            Account acct = (Account) i.next();
            // Set balance
            acct.withdraw(acct.getBalance() - balance);

            return acct;
         } else
         {
            // Create account
            return createAccount(customer, balance);
         }
      } catch (Exception e)
      {
         log.debug("failed", e);
         throw new BankException("Could not get account for " + customer, e);
      }
   }

   public Customer getCustomer(String name) throws BankException
   {
      try
      {
         // Check for existing customer

         return null;
      } catch (Exception e)
      {
         log.debug("failed", e);
         throw new BankException("Could not get customer for " + name, e);
      }
   }

   public void transferTest(Account from, Account to, float amount, int iter)
         throws java.rmi.RemoteException, BankException
   {
      for (int i = 0; i < iter; i++)
      {
         from.withdraw(amount);
         to.deposit(amount);
      }
   }

   public String greetWithRequiredTransaction(String greeting) throws Exception
   {
      if (tm.getTransaction() == null) throw new Exception("method has no tx set");
      return greeting;
   }

   public String greetWithNotSupportedTransaction(String greeting) throws Exception
   {
      if (tm.getTransaction() != null) throw new Exception("method has tx set");
      return greeting;
   }

   public String greetWithServiceTimer(String greeting) throws Exception
   {
      if (ts == null) throw new Exception("TimerService @Inject failed");
      return greeting;
   }

   public String greetUnchecked(String greeting) throws Exception
   {
      if (tm.getTransaction() == null) throw new Exception("method has no tx set");
      return greeting;
   }

   public String greetChecked(String greeting) throws Exception
   {
      if (tm.getTransaction() == null) throw new Exception("method has no tx set");
      return greeting;
   }

   public void storeCustomerId(String customerId) throws Exception
   {
      bank.storeCustomerId(customerId);
   }

   public String retrieveCustomerId() throws Exception
   {
      return bank.retrieveCustomerId();
   }

   public void excludedMethod()
   {

   }

   public void postConstruct()
   {
      constructed = true;
   }

   public String getDefaultValue()
   {
      return defaultValue;
   }
   
   public void testTransactionTimeout()
   {
      boolean exceptionCaught = false;
      try
      {
         log.info("************* calling bank.testTransactionTimeout()");
         bank.testTransactionTimeout();
         log.info("************* finished calling bank.testTransactionTimeout()");
      }
      catch (Exception e)
      {
         log.info("********** caught exception");
         exceptionCaught = true;
      }
      if (!exceptionCaught) throw new RuntimeException("Failed to catch transactionTimeout");
   }
}
