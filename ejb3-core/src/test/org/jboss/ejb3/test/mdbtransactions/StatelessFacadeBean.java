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
package org.jboss.ejb3.test.mdbtransactions;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateless(name="Stateless")
@Remote(StatelessFacade.class)
@RemoteBinding(jndiBinding = "StatelessFacade")
public class StatelessFacadeBean implements StatelessFacade
{
   private static final Logger log = Logger.getLogger(StatelessFacadeBean.class);
   
   private @PersistenceContext EntityManager manager;
   
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public void clear(Entity entity)
   {
      try
      {
         entity = manager.find(Entity.class, entity.getId());
         if (entity != null)
            manager.remove(entity);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }
   
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public void persist(Entity entity)
   {
      try
      {
         manager.persist(entity);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }
}
