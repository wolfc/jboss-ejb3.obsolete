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

import java.rmi.*;


/**
 * @see <related>
 * @author $Author: wolfc $
 * @version $Revision: 61136 $
 */
public interface Teller
{
   public static final String JNDI_NAME = "bank/Teller";
   
   public void transfer(Account from, Account to, float amount)
         throws RemoteException, BankException;

   public Account createAccount(Customer customer, float balance)
         throws RemoteException, BankException;

   public Account getAccount(Customer customer, float balance)
         throws RemoteException, BankException;

   public Customer getCustomer(String name) throws RemoteException,
         BankException;

   public void transferTest(Account from, Account to, float amount, int iter)
         throws java.rmi.RemoteException, BankException;

   public String greetWithRequiredTransaction(String greeting) throws Exception;
   
   public String greetWithNotSupportedTransaction(String greeting) throws Exception;
   
   public String greetWithServiceTimer(String greeting) throws Exception;
   
   public String greetUnchecked(String greeting) throws Exception;
   
   public String greetChecked(String greeting) throws Exception;
   
   public void storeCustomerId(String customerId) throws Exception;

   public String retrieveCustomerId() throws Exception;
   
   public boolean isConstructed() throws Exception;
   
   public void excludedMethod();
   
   public String getDefaultValue();
   
   public void testTransactionTimeout();
}

/*
 * $Id: Teller.java 61136 2007-03-06 09:24:20Z wolfc $ Currently locked
 * by:$Locker$ Revision: $Log$
 * by:$Locker:  $ Revision: Revision 1.10  2005/10/30 00:06:46  starksm
 * by:$Locker:  $ Revision: Update the jboss LGPL headers
 * by:$Locker:  $ Revision:
 * by:$Locker$ Revision: Revision 1.9  2005/10/13 19:14:42  bdecoste
 * by:$Locker$ Revision: added transaction timeouts via annotation or jboss.xml
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.8  2005/06/01 00:31:11  bdecoste
 * by:$Locker$ Revision: ejb3 web support
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.7  2005/05/25 18:57:19  bdecoste
 * by:$Locker$ Revision: added support for @Remove and @Exclude
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.6  2005/05/17 22:37:42  bdecoste
 * by:$Locker$ Revision: remove ejb2.1 rules
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.5  2005/05/14 17:48:45  bdecoste
 * by:$Locker$ Revision: added ejb3 dd support for callback annotations
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.4  2005/05/12 01:46:06  bdecoste
 * by:$Locker$ Revision: added ejb3 dd support for RunAs and Inject
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.3  2005/05/10 23:54:21  bdecoste
 * by:$Locker$ Revision: added ejb3 dd security and transaction support
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.2  2005/05/03 23:51:01  bdecoste
 * by:$Locker$ Revision: fixed formatting
 * by:$Locker$ Revision: Revision 1.1 2005/05/03
 * 20:35:11 bdecoste test for ejb3 deployment descriptors Revision 1.2
 * 2001/01/07 23:14:36 peter Trying to get JAAS to work within test suite.
 * Revision 1.1.1.1 2000/06/21 15:52:38 oberg Initial import of jBoss test. This
 * module contains CTS tests, some simple examples, and small bean suites.
 */
