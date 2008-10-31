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

import java.rmi.RemoteException;
import javax.ejb.CreateException;

/**
 * @see <related>
 * @author $Author$
 * @version $Revision$
 */
public class AccountBeanCMP extends AccountBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   public String id;

   public float balance;

   public Customer owner;

   private boolean dirty;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
      dirty = true;
   }

   public float getBalance()
   {
      return balance;
   }

   public void setBalance(float balance)
   {
      this.balance = balance;
      dirty = true;
   }

   public Customer getOwner()
   {
      return owner;
   }

   public void setOwner(Customer owner)
   {
      this.owner = owner;
      dirty = true;
   }

   public void setData(AccountData data)
   {
      setBalance(data.getBalance());
      setOwner(data.getOwner());
   }

   public AccountData getData()
   {
      AccountData data = new AccountData();
      data.setId(id);
      data.setBalance(balance);
      data.setOwner(owner);
      return data;
   }

   public boolean isModified()
   {
      return dirty;
   }

   // EntityBean implementation -------------------------------------
   public String ejbCreate(AccountData data) throws RemoteException,
         CreateException
   {
      setId(data.id);
      setData(data);
      dirty = false;
      return null;
   }

   public void ejbPostCreate(AccountData data) throws RemoteException,
         CreateException
   {
   }

   public void ejbLoad() throws RemoteException
   {
      dirty = false;
   }
}

/*
 * $Id$ Currently
 * locked by:$Locker$ Revision: $Log$
 * locked by:$Locker:  $ Revision: Revision 1.5  2006/03/09 05:12:58  starksm
 * locked by:$Locker:  $ Revision: cleanup unused imports
 * locked by:$Locker:  $ Revision:
 * locked by:$Locker$ Revision: Revision 1.4  2005/10/30 00:06:46  starksm
 * locked by:$Locker$ Revision: Update the jboss LGPL headers
 * locked by:$Locker$ Revision:
 * locked by:$Locker$ Revision: Revision 1.3  2005/05/17 22:37:42  bdecoste
 * locked by:$Locker$ Revision: remove ejb2.1 rules
 * locked by:$Locker$ Revision:
 * locked by:$Locker$ Revision: Revision 1.2  2005/05/03 23:51:01  bdecoste
 * locked by:$Locker$ Revision: fixed formatting
 * locked by:$Locker$ Revision: Revision 1.1
 * 2005/05/03 20:35:11 bdecoste test for ejb3 deployment descriptors Revision
 * 1.2 2001/01/07 23:14:34 peter Trying to get JAAS to work within test suite.
 * Revision 1.1.1.1 2000/06/21 15:52:37 oberg Initial import of jBoss test. This
 * module contains CTS tests, some simple examples, and small bean suites.
 */
