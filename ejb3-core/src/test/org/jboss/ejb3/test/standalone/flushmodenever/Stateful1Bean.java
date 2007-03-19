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
package org.jboss.ejb3.test.standalone.flushmodenever;

import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import org.jboss.annotation.JndiInject;
import org.jboss.ejb3.entity.ExtendedEntityManager;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateful
@Remote(Stateful1.class)
public class Stateful1Bean implements Stateful1, java.io.Serializable
{
   @PersistenceContext(unitName="manager1", type =PersistenceContextType.EXTENDED) EntityManager manager1;
   @PersistenceContext(unitName = "../flushmodenever-session2.jar#manager2", type =PersistenceContextType.EXTENDED) EntityManager manager2;

   Entity1 one;
   Entity2 two;

   @JndiInject(jndiName="java:/TransactionManager") TransactionManager tm;

   public int create1()
   {
      one = new Entity1();
      one.setString("oneManager");
      if (!(manager1 instanceof ExtendedEntityManager)) throw new RuntimeException("assert failed");
      manager1.persist(one);
      return one.getId();
   }

   public int create2()
   {
      two = new Entity2();
      two.setString("twoManager");
      manager2.persist(two);
      return two.getId();
   }

   public void update1()
   {
      if (!manager1.contains(one)) throw new RuntimeException("assert failed");
      one.setString("changed");
   }

   public void update2()
   {
      if (!manager2.contains(two)) throw new RuntimeException("assert failed");
      two.setString("changed");
   }

   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public void never()
   {
      one.setString("never");
      two.setString("never");
   }

   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public void never2(Entity1 uno, Entity2 dos)
   {
      Transaction tx = null;
      try
      {
         tx = tm.getTransaction();
      }
      catch (SystemException e)
      {
         throw new RuntimeException(e);
      }
      if (tx != null) throw new RuntimeException("TRANSACTION IS NOT NULL!");
      if (manager1.merge(uno) != one) throw new RuntimeException("NOT EQUAL!!");
      if (!uno.getString().equals("never2")) throw new RuntimeException("NOT_EQUAL");
      if (manager2.merge(dos) != two) throw new RuntimeException("NOT EQUAL!");
      if (!dos.getString().equals("never2")) throw new RuntimeException("NOT_EQUAL");
   }

   @Remove
   public void checkout()
   {
      /*
      Entity1 uno = manager1.find(Entity1.class, one.getId());
      if (uno != one) throw new RuntimeException("NOT EQUAL");
      if (!uno.equals("never2")) throw new RuntimeException("NOT EQUAL");
      manager1.flush();
      manager2.flush();
      */
   }


}
