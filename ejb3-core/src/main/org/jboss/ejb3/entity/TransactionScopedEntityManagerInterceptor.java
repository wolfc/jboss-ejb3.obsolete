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

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;

import javax.persistence.EntityManager;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * If a transaction-scoped entitymanager is accessed outside of a transaction
 * an entitymanager is created for the duration of the current EJB method call
 * this entitymanager must be closed at the end of the method invocation.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class TransactionScopedEntityManagerInterceptor implements Interceptor
{
   private static final Logger log = Logger.getLogger(TransactionScopedEntityManagerInterceptor.class);

   public String getName()
   {
      return this.getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      try
      {
         ManagedEntityManagerFactory.nonTxStack.push(new IdentityHashMap());
         return invocation.invokeNext();
      }
      finally
      {
         Map map = ManagedEntityManagerFactory.nonTxStack.pop();
         for (Object obj : map.values())
         {
            try
            {
               ((EntityManager)obj).close();
               log.debug("********************* CLOSING tx scoped nontx entity manager");
            }
            catch (Exception ignored)
            {
            }
         }
      }
   }
}
