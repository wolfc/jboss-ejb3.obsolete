//$Id: $
/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.entity.hibernate;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.jboss.jpa.deployment.ManagedEntityManagerFactory;
import org.jboss.jpa.util.ManagedEntityManagerFactoryHelper;

/**
 * Handle method execution delegation to an Hibernate session following the transaction scoped persistence context rules
 *
 * @author Emmanuel Bernard
 */
public class TransactionScopedSessionInvocationHandler implements InvocationHandler, Externalizable
{
   private static final long serialVersionUID = -5788395417446868080L;
   
   private transient ManagedEntityManagerFactory factory;

   public TransactionScopedSessionInvocationHandler(ManagedEntityManagerFactory factory)
   {
      if ( factory == null ) throw new NullPointerException( "factory must not be null" );
      this.factory = factory;
   }

   //is it useful?
   public TransactionScopedSessionInvocationHandler()
   {
   }

   protected Session getHibernateSession()
   {
      if ( getSession() instanceof HibernateEntityManager )
      {
         return ( (HibernateEntityManager) getSession() ).getSession();
      }
      throw new RuntimeException( "ILLEGAL ACTION: Not a Hibernate persistence provider" );
   }

   protected EntityManager getSession()
   {
      return factory.getTransactionScopedEntityManager();
   }


   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF( factory.getKernelName() );
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      String kernelName = in.readUTF();
      factory = ManagedEntityManagerFactoryHelper.getManagedEntityManagerFactory(kernelName);
      if ( factory == null ) throw new IOException( "Unable to find persistence unit in registry: " + kernelName );
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if ( "close".equals( method.getName() ) ) {
         throw new IllegalStateException("Illegal to call this method from injected, managed EntityManager");
      }
      else {
         //catch all
         try {
            return method.invoke( getHibernateSession(), args );
         }
         catch ( InvocationTargetException e ) {
				if ( e.getTargetException() instanceof RuntimeException ) {
					throw ( RuntimeException ) e.getTargetException();
				}
				else {
					throw e;
				}
			}
      }
   }
}
