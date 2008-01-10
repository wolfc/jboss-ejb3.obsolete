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
package org.jboss.ejb3.test.persistenceunits;

import org.jboss.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateless
@Remote(EntityTest.class)
public class EntityTestBean implements EntityTest
{
   private static final Logger log = Logger.getLogger(EntityTestBean.class);
   
   private @PersistenceContext(unitName="Entity1") EntityManager manager1;
   private @PersistenceContext(unitName="Entity2") EntityManager manager2;

   public Long persistEntity1(Entity1 entity1)
   {
      manager1.persist(entity1);
      return entity1.getId();
   }

   public Entity1 loadEntity1(Long id)
   {
      return manager1.find(Entity1.class, id);
   }

   public Long persistEntity2(Entity2 entity2)
   {
      manager2.persist(entity2);
      return entity2.getId();
   }

   public Entity2 loadEntity2(Long id)
   {
      return manager2.find(Entity2.class, id);
   }
}
