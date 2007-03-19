/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.ejb3.test.stateful;

import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.logging.Logger;

import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;


/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateful
@Remote(EntityFacade.class)
@CacheConfig(maxSize = 1000, idleTimeoutSeconds = 1)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class EntityFacadeBean implements EntityFacade
{
   private @PersistenceContext(type = PersistenceContextType.EXTENDED)
   EntityManager manager;
   
   private static final Logger log = Logger.getLogger(EntityFacadeBean.class);

   public Entity createEntity(String name) {
      log.info("********* createEntity " + name);
      Entity entity = new Entity();
      entity.setName(name);
	   manager.persist(entity);
	   return entity;
   }
   
   public Entity loadEntity(Long id) {
      log.info("********* loadEntity " + id);
      Entity entity =  manager.find(Entity.class, id);
	   return entity;
   }
   
   @PrePassivate
   public void passivate()
   {
      log.info("************ passivating");  
   }
   
   @PostActivate
   public void activate()
   {
      log.info("************ activating");
   }
   
   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   @Remove(retainIfException=true)
   public void remove()
   {
      
   }
   
   @Remove(retainIfException=true)
   public void removeWithTx()
   {
      
   }
   
   @PreDestroy
   public void destroy()
   {
      log.info("************ destroying");  
      // throw RuntimeException
//      Object o = null;
//      o.getClass();
      throw new RuntimeException("From destroy");
   }
}
