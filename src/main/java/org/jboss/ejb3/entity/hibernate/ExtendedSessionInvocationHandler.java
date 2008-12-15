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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.jboss.ejb3.stateful.StatefulBeanContext;

/**
 * Handle method execution delegation to an Hibernate Session following the extended persistence context rules
 *
 * @author Emmanuel Bernard
 */
public class ExtendedSessionInvocationHandler implements InvocationHandler, Serializable
{
   private static final long serialVersionUID = -2091491765429086936L;
   
   private String identity;

   public ExtendedSessionInvocationHandler(String identity)
   {
      this.identity = identity;
   }

   //is it useful?
   public ExtendedSessionInvocationHandler()
   {
   }

   public EntityManager getPersistenceContext()
   {
      StatefulBeanContext beanContext = StatefulBeanContext.currentBean.get();
      EntityManager persistenceContext = beanContext.getExtendedPersistenceContext(identity);
      if (persistenceContext == null)
         throw new RuntimeException("Unable to determine persistenceContext: " + identity
                                    + " in injected SFSB: " + beanContext.getContainer().getObjectName());
      return persistenceContext;
   }

   public Session getHibernateSession()
   {
      EntityManager persistenceContext = getPersistenceContext();
      if (persistenceContext instanceof HibernateEntityManager )
      {
         return ((HibernateEntityManager) persistenceContext).getSession();
      }
      throw new RuntimeException("ILLEGAL ACTION:  Not a Hibernate persistence provider");
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String methodName = method.getName();
      if ( "getPersistenceContext".equals( methodName ) ) {
         return getPersistenceContext();
      }
      else if ( "close".equals( methodName ) ) {
         throw new IllegalStateException("It is illegal to close an injected Hibernate Session");
      }
      else if("toString".equals(methodName)) {
         return toString() + "[identity=" + identity + "]";
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
