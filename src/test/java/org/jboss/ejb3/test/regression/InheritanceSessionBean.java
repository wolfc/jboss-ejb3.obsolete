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

import java.util.List;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.PersistenceContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(InheritanceSession.class)
public class InheritanceSessionBean implements InheritanceSession
{
   @PersistenceContext
   private EntityManager em;

   public long createBranch()
   {
      FootballTeam branch = new FootballTeam();
      branch.setCity("Foxboro");
      branch.setCountry("USA");
      branch.setName("NE Pats");
      branch.setPhone("617-666-6666");
      branch.setEmail("boston@boston.com");
      branch.setSuperbowlsWon(2);

      Employee employee = new Employee();
      employee.setName("Tom Brady");
      employee.setPhone("617-666-6666");
      employee.setEmail("boston@boston.com");
      employee.setFirst("Tom");
      employee.setLast("Brady");
      employee.setCell("617-666-6666");
      branch.addEmployee(employee);

      Manager manager = new Manager();
      manager.setName("Bill Belicheck");
      manager.setPhone("617-666-6666");
      manager.setEmail("boston@boston.com");
      manager.setFirst("Bill");
      manager.setLast("Belicheck");
      manager.setCell("617-666-6666");
      manager.setTitle("Head Coach");
      branch.addEmployee(manager);
      em.persist(branch);
      return branch.getId();
   }

   public Branch getBranch(long id)
   {
      return em.find(Branch.class, id);
   }

   public List getContacts()
   {
      Query query = em.createQuery("from Contact");
      return query.getResultList();
   }
}
