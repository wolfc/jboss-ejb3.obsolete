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

/**
 * @see <related>
 * @author $Author: wolfc $
 * @version $Revision: 61136 $
 */
public abstract class AccountBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void deposit(float amount)
   {
      setBalance(getBalance() + amount);
   }

   public void withdraw(float amount)
   {
      setBalance(getBalance() - amount);
   }

   public abstract float getBalance();

   public abstract void setBalance(float balance);
}

/*
 * $Id: AccountBean.java 61136 2007-03-06 09:24:20Z wolfc $ Currently
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
 * 1.2 2001/01/07 23:14:34 peter Trying to get JAAS to work within test suite.
 * Revision 1.1.1.1 2000/06/21 15:52:37 oberg Initial import of jBoss test. This
 * module contains CTS tests, some simple examples, and small bean suites.
 */
