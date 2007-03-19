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
package org.jboss.ejb3.test.hbm;

import java.util.List;
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
@Remote(EntityTest.class)
public class EntityTestBean implements EntityTest
{
   @PersistenceContext private EntityManager em;
   @PersistenceContext(unitName="hbm2-test") private EntityManager em2;

   public void createBoth()
   {
      Annotated an = new Annotated();
      an.setName("Bill");

      HBM hbm = new HBM();
      hbm.setName("Gavin");

      an.setHbm(hbm);

      em.persist(an);

      HBM2 hbm2 = new HBM2();
      hbm2.setName("Bill");
      em2.persist(hbm2);
   }

   public List findAnnotated()
   {
      return em.createQuery("from Annotated an").getResultList();
   }

   public List findHBM()
   {
      return em.createQuery("from HBM hbm").getResultList();
   }
}
