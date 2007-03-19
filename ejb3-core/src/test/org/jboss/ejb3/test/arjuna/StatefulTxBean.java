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
package org.jboss.ejb3.test.arjuna;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionManager;

import org.jboss.annotation.JndiInject;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.ejb3.test.arjuna.Entity;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateful(name="StatefulTx")
@Remote(StatefulTx.class)
@RemoteBinding(jndiBinding = "StatefulTx")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) 
public class StatefulTxBean implements StatefulTx
{
   private static final Logger log = Logger.getLogger(StatefulTxBean.class);
   
   @JndiInject(jndiName="java:/TransactionManager") private TransactionManager tm;
   
   @PersistenceContext private EntityManager manager;
   
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public boolean clear(Entity entity) throws javax.transaction.SystemException
   {
     entity = manager.find(Entity.class, entity.getId());
     if (entity != null)
        manager.remove(entity);
     
     return getReturn();
   }
   
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public boolean persist(Entity entity) throws javax.transaction.SystemException
   {
      manager.persist(entity);
      
      return getReturn();
   }
    
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public boolean isArjunaTransactedRequired() throws javax.transaction.SystemException
   {
      return getReturn();
   }
   
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public boolean isArjunaTransactedRequiresNew() throws javax.transaction.SystemException
   {
      return getReturn();
   }
   
   protected boolean getReturn() throws javax.transaction.SystemException
   {
      if (tm.getTransaction() == null)
         return false;
      
      if (!tm.getClass().toString().contains("arjuna"))
         return false;
      
      if (!tm.getTransaction().getClass().toString().contains("arjuna"))
         return false;
      
      return true;
   }

}
