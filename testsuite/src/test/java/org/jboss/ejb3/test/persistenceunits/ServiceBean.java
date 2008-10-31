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

import java.util.List;

import javax.ejb.Remote;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.ejb3.annotation.Service;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Service
@Remote(ServiceRemote.class)
public class ServiceBean implements ServiceRemote
{
   private static final Logger log = Logger.getLogger(ServiceBean.class);
   
   @PersistenceContext(unitName = "Entity1")
   private EntityManager manager;
   
   public void testSharedEntity()
   {
      Entity1 entity = new Entity1();
      entity.setData("TestShared3");
      manager.persist(entity);
      
      entity = new Entity1();
      entity.setData("TestShared4");
      manager.persist(entity);

      List<Entity1> result = manager.createNamedQuery(Entity1.FIND_ALL).getResultList();
      for (Entity1 e : result)
      {
         log.info("*** testSharedEntity result=" + e.getData());
      }
   }
}
