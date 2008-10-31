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
package org.jboss.ejb3.test.regressionHHH275;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.PersistenceContext;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Stateless
@Remote(SearchDAORemote.class)
public class SearchDAO implements SearchDAORemote
{

   @PersistenceContext private EntityManager em;

   public void create()
   {
      {
         SavedSearch s1 = new SavedSearch();
         s1.setId("USER_ALL_USERS");
         s1.setSearchTitle("TITLE");
         em.persist(s1);
      }

      {
         SavedSearch s1 = new SavedSearch();
         s1.setId("u1");
         s1.setSearchTitle("TITLE");
         em.persist(s1);
      }

   }

   public int find()
   {
      Query q = em.createQuery("From SavedSearch s WHERE (s.id = 'u1' OR s.id = 'USER_ALL_USERS') AND s.searchTitle = 'CRAP'");
      return q.getResultList().size();
   }
}
