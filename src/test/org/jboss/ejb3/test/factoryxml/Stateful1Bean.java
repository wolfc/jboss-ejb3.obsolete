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
package org.jboss.ejb3.test.factoryxml;

import javax.ejb.Remove;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class Stateful1Bean implements Stateful1
{
   EntityManager manager1;
   @PersistenceContext(name="manager2") EntityManager manager2;

   Entity1 one;
   Entity2 two;

   public int create1()
   {
      one = new Entity1();
      one.setString("oneManager");
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
      one.setString("changed");
   }

   public void update2()
   {
      two.setString("changed");
   }

   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public void never()
   {
      one.setString("never");
      two.setString("never");
   }

   @Remove
   public void checkout() {}


}
