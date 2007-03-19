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
package org.jboss.tutorial.packaging.bean;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(Session1.class)
public class Session1Bean implements Session1
{
   @PersistenceContext(unitName="manager1") EntityManager manager1;
   @PersistenceContext(unitName="manager2") EntityManager manager2;
   @PersistenceUnit(unitName="manager1") EntityManagerFactory factory1;
   @PersistenceUnit(unitName="manager2") EntityManagerFactory factory2;

   public int create1FromManager()
   {
      Entity1 one = new Entity1();
      one.setString("oneManager");
      manager1.persist(one);
      return one.getId();
   }
   public int create1FromFactory()
   {
      Entity1 one = new Entity1();
      one.setString("oneFactory");
      EntityManager m = factory1.createEntityManager();
      m.persist(one);
      System.out.println(one.getId());
      return one.getId();
   }

   public int create2FromManager()
   {
      Entity2 two = new Entity2();
      two.setString("twoManager");
      manager2.persist(two);
      return two.getId();
   }
   public int create2FromFactory()
   {
      Entity2 two = new Entity2();
      two.setString("twoFactory");
      EntityManager m = factory2.createEntityManager();
      m.persist(two);
      return two.getId();
   }
}
