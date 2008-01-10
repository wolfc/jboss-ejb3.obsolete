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
package org.jboss.ejb3.test.txexceptions;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(Dao.class)
public class DaoBean implements Dao
{
   private static final Logger log = Logger
   .getLogger(DaoBean.class);
   
   @PersistenceContext EntityManager manager;

   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public void testRequiresNewWithLookedUpEntityManager() throws Exception
   {
      RequiresNewTest.doit();
   }

   public SimpleEntity get(int id)
   {
      SimpleEntity en = manager.find(SimpleEntity.class, id);
      return en;
   }

   public void remove(int id)
   {
      SimpleEntity en = manager.find(SimpleEntity.class, id);
      manager.remove(en);
   }

   public void createThrowAnnotatedAppException(int id) throws AnnotatedAppException
   {
      persist(id);
      throw new AnnotatedAppException();
   }
   
   public void createThrowDeploymentDescriptorAppException(int id) throws DeploymentDescriptorAppException
   {
      persist(id);
      throw new DeploymentDescriptorAppException();
   }

   private void persist(int id)
   {
      SimpleEntity entity = new SimpleEntity();
      entity.setId(id);
      entity.setStuff("stuff");
      manager.persist(entity);
   }

   public void createThrowAppException(int id) throws AppException
   {
      persist(id);
      throw new AppException();
   }

   public void createThrowCheckedRollbackException(int id) throws CheckedRollbackException
   {
      persist(id);
      throw new CheckedRollbackException();
   }
   
   public void createThrowDeploymentDescriptorCheckedRollbackException(int id) throws DeploymentDescriptorCheckedRollbackException
   {
      persist(id);
      throw new DeploymentDescriptorCheckedRollbackException();
   }

   public void createThrowNoRollbackRemoteException(int id) throws NoRollbackRemoteException
   {
      persist(id);
      throw new NoRollbackRemoteException();
   }

   public void createThrowNoRollbackRuntimeException(int id) throws NoRollbackRuntimeException
   {
      persist(id);
      throw new NoRollbackRuntimeException();
   }

   public void createThrowRollbackRemoteException(int id) throws RollbackRemoteException
   {
      persist(id);
      throw new RollbackRemoteException();
   }

   public void createThrowRollbackRuntimeException(int id) throws RollbackRuntimeException
   {
      persist(id);
      throw new RollbackRuntimeException();
   }
}
