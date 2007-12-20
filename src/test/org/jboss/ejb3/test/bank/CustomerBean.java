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

import java.util.*;


/**
 * @see <related>
 * @author $Author: wolfc $
 * @version $Revision: 61136 $
 */
public class CustomerBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   public String id;

   public String name;

   public Collection accounts;

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
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public Collection getAccounts()
   {
      return accounts;
   }

   public void addAccount(Account acct)
   {
      accounts.add(acct);
   }

   public void removeAccount(Account acct)
   {
      accounts.remove(acct);
   }

   // EntityHome implementation -------------------------------------
   public CustomerPK ejbCreate(String id, String name)
   {
      setId(id);
      setName(name);
      accounts = new ArrayList();

      CustomerPK pk = new CustomerPK();
      pk.id = id;
      pk.name = name;

      return pk;
   }

   public void ejbPostCreate(String id, String name)
   {
   }
}

/*
 * $Id: CustomerBean.java 61136 2007-03-06 09:24:20Z wolfc $ Currently
 * locked by:$Locker$ Revision: $Log$
 * locked by:$Locker:  $ Revision: Revision 1.4  2005/10/30 00:06:46  starksm
 * locked by:$Locker:  $ Revision: Update the jboss LGPL headers
 * locked by:$Locker:  $ Revision:
 * locked by:$Locker$ Revision: Revision 1.3  2005/05/17 22:37:42  bdecoste
 * locked by:$Locker$ Revision: remove ejb2.1 rules
 * locked by:$Locker$ Revision:
 * locked by:$Locker$ Revision: Revision 1.2  2005/05/03 23:51:01  bdecoste
 * locked by:$Locker$ Revision: fixed formatting
 * locked by:$Locker$ Revision: Revision 1.1
 * 2005/05/03 20:35:11 bdecoste test for ejb3 deployment descriptors Revision
 * 1.6 2003/08/27 04:32:49 patriot1burke 4.0 rollback to 3.2 Revision 1.4
 * 2001/01/20 16:32:51 osh More cleanup to avoid verifier warnings. Revision 1.3
 * 2001/01/07 23:14:34 peter Trying to get JAAS to work within test suite.
 * Revision 1.2 2000/09/30 01:00:54 fleury Updated bank tests to work with new
 * jBoss version Revision 1.1.1.1 2000/06/21 15:52:37 oberg Initial import of
 * jBoss test. This module contains CTS tests, some simple examples, and small
 * bean suites.
 */
