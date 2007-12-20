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
public interface Bank
{
   public static final String JNDI_NAME = "bank/Bank";
   
   public String getId() throws RemoteException;
   
   public String getEnvEntryId() throws javax.naming.NamingException, RemoteException;

   public String createAccountId(Customer customer) throws RemoteException;

   public String createCustomerId() throws RemoteException;

   public void storeCustomerId(String customerId) throws RemoteException;

   public String retrieveCustomerId() throws RemoteException;
   
   public String interceptCustomerId(String customerId) throws RemoteException;
   
   public void testResource() throws Exception;
   
   public void remove();
   
   public String isInitialized();
   
   public String isActivated();
   
   public void testTransactionTimeout();
   
   public String getTransactionState();
}

/*
 * $Id: Bank.java 61136 2007-03-06 09:24:20Z wolfc $ Currently locked
 * by:$Locker$ Revision: $Log$
 * by:$Locker:  $ Revision: Revision 1.11  2005/10/30 00:06:46  starksm
 * by:$Locker:  $ Revision: Update the jboss LGPL headers
 * by:$Locker:  $ Revision:
 * by:$Locker$ Revision: Revision 1.10  2005/10/13 19:14:42  bdecoste
 * by:$Locker$ Revision: added transaction timeouts via annotation or jboss.xml
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.9  2005/08/24 20:30:43  bdecoste
 * by:$Locker$ Revision: support for <env-entry>
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.8  2005/06/02 23:25:14  bdecoste
 * by:$Locker$ Revision: ejb3 jboss.xml support
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.7  2005/05/26 02:24:09  bdecoste
 * by:$Locker$ Revision: support for @Init
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.6  2005/05/25 18:57:19  bdecoste
 * by:$Locker$ Revision: added support for @Remove and @Exclude
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.5  2005/05/17 22:37:42  bdecoste
 * by:$Locker$ Revision: remove ejb2.1 rules
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.4  2005/05/14 20:54:02  bdecoste
 * by:$Locker$ Revision: added ejb3 dd support for Resource
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.3  2005/05/12 20:31:46  bdecoste
 * by:$Locker$ Revision: added ejb3 dd support for injection, callbacks, EJB
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.2  2005/05/03 23:51:01  bdecoste
 * by:$Locker$ Revision: fixed formatting
 * by:$Locker$ Revision: Revision 1.1 2005/05/03 20:35:11
 * bdecoste test for ejb3 deployment descriptors Revision 1.2 2001/01/07
 * 23:14:35 peter Trying to get JAAS to work within test suite. Revision 1.1.1.1
 * 2000/06/21 15:52:38 oberg Initial import of jBoss test. This module contains
 * CTS tests, some simple examples, and small bean suites.
 */
