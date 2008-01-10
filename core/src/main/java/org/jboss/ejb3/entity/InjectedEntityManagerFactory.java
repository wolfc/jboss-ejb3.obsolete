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
package org.jboss.ejb3.entity;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.jboss.ejb3.PersistenceUnitRegistry;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class InjectedEntityManagerFactory implements EntityManagerFactory, Externalizable
{
   private static final long serialVersionUID = -3734435119658196788L;
   
   private transient EntityManagerFactory delegate;
   private transient ManagedEntityManagerFactory managedFactory;
   
   public InjectedEntityManagerFactory() {}

   public InjectedEntityManagerFactory(ManagedEntityManagerFactory managedFactory)
   {
      assert managedFactory != null : "managedFactory is null";
      
      this.delegate = managedFactory.getEntityManagerFactory();
      this.managedFactory = managedFactory;
   }


   public EntityManagerFactory getDelegate()
   {
      return delegate;
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(managedFactory.getKernelName());
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      String kernelName = in.readUTF();
      PersistenceUnitDeployment deployment = PersistenceUnitRegistry.getPersistenceUnit(kernelName);
      if (deployment == null) throw new IOException("Unable to find persistence unit in registry: " + kernelName);
      managedFactory = deployment.getManagedFactory();
      delegate = managedFactory.getEntityManagerFactory();
   }

   public EntityManager createEntityManager()
   {
      return getDelegate().createEntityManager();
   }

   public EntityManager createEntityManager(Map map)
   {
      return delegate.createEntityManager(map);
   }


   public EntityManager getEntityManager()
   {
      return new TransactionScopedEntityManager(managedFactory);
   }

   public void close()
   {
      throw new IllegalStateException("It is illegal to close an injected EntityManagerFactory");
   }

   public boolean isOpen()
   {
      return getDelegate().isOpen();
   }
}
