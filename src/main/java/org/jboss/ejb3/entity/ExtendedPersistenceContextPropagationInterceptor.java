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

import java.util.Map;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.ejb3.stateful.StatefulContainerInvocation;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.jpa.deployment.ManagedEntityManagerFactory;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ExtendedPersistenceContextPropagationInterceptor implements Interceptor
{
   private static final Logger log = Logger.getLogger(ExtendedPersistenceContextPropagationInterceptor.class);

   public String getName()
   {
      return this.getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      log.debug("++++ LongLivedSessionPropagationInterceptor");
      StatefulContainerInvocation ejb = (StatefulContainerInvocation) invocation;
      StatefulBeanContext ctx = (StatefulBeanContext) ejb.getBeanContext();

      Map<String, EntityManager> extendedPCs = ctx.getExtendedPersistenceContexts();
      if (extendedPCs == null || extendedPCs.size() == 0)
      {
         return invocation.invokeNext();
      }

      TransactionManager tm = TxUtil.getTransactionManager();
      if (tm.getTransaction() != null)
      {
         for (String kernelname : extendedPCs.keySet())
         {
            EntityManager manager = extendedPCs.get(kernelname);
            ManagedEntityManagerFactory factory = ManagedEntityManagerFactoryHelper.getManagedEntityManagerFactory(kernelname);
            factory.registerExtendedWithTransaction(manager);
         }
      }

      return invocation.invokeNext();
   }
}
