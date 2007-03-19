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
package org.jboss.injection;

import org.jboss.ejb3.entity.ManagedEntityManagerFactory;
import org.jboss.ejb3.entity.ExtendedEntityManager;
import org.jboss.ejb3.entity.ExtendedHibernateSession;
import org.jboss.ejb3.entity.TransactionScopedEntityManager;
import org.jboss.ejb3.entity.TransactionScopedHibernateSession;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.naming.Util;

import javax.persistence.PersistenceContextType;
import javax.persistence.EntityManager;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class PcEncInjector implements EncInjector
{
   private String encName;
   private String unitName;
   private PersistenceContextType type;
   private Class injectionType;
   private String error;

   public PcEncInjector(String encName, String unitName, PersistenceContextType type, Class injectionType, String error)
   {
      this.encName = encName;
      this.unitName = unitName;
      this.type = type;
      this.injectionType = injectionType;
      this.error = error;
   }

   public void inject(InjectionContainer container)
   {
      String error1 = error;
      ManagedEntityManagerFactory factory = null;
      try
      {
         factory = PersistenceUnitHandler.getManagedEntityManagerFactory(
                 container, unitName);
      }
      catch (NameNotFoundException e)
      {
         error1 += " " + e.getMessage();
      }
      if (factory == null)
      {
         throw new RuntimeException(error1);
      }
      if (type == PersistenceContextType.EXTENDED)
      {
         if (!(container instanceof StatefulContainer))
            throw new RuntimeException("It is illegal to inject an EXTENDED PC into something other than a SFSB");
         container.getInjectors().add(0, new ExtendedPersistenceContextInjector(factory));
         Object extendedPc = null;
         if (injectionType == null
                 || injectionType.getName().equals(EntityManager.class.getName()))
         {
            extendedPc = new ExtendedEntityManager(factory.getKernelName());
         }
         else
         {
            extendedPc = new ExtendedHibernateSession(factory.getKernelName());
         }
         try
         {
            Util.rebind(container.getEnc(), encName, extendedPc);
         }
         catch (NamingException e)
         {
            throw new RuntimeException(error1, e);
         }
      }
      else
      {
         Object entityManager = null;
         if (injectionType == null
                 || injectionType.getName().equals(EntityManager.class.getName()))
         {
            entityManager = new TransactionScopedEntityManager(factory);
         }
         else
         {
            entityManager = new TransactionScopedHibernateSession(factory);
         }
         try
         {
            Util.rebind(container.getEnc(), encName, entityManager);
         }
         catch (NamingException e)
         {
            throw new RuntimeException(error1, e);
         }
      }
   }
}
