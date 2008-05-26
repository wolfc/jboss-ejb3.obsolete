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
package org.jboss.ejb3.test.regression;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Stateless
@Remote(AccountDAO.class)
public class AccountDAOBean implements AccountDAO
{
   @PersistenceContext private EntityManager em;

   public long createAccount()
   {
      Account account = new Account();
      em.persist(account);
      AccountName name1 = new AccountName();
      name1.setAccount(account);
      account.getNames().add(name1);
      AccountNamePK pk = new AccountNamePK(account.getId(), "US");
      name1.setPk(pk);
      em.persist(name1);

      AccountName name2 = new AccountName();
      name2.setAccount(account);
      account.getNames().add(name2);
      AccountNamePK pk2 = new AccountNamePK(account.getId(), "FRA");
      name2.setPk(pk2);
      em.persist(name2);
      return account.getId();
   }

   public Account findAccount(long id)
   {
      return em.find(Account.class, id);
   }
}
