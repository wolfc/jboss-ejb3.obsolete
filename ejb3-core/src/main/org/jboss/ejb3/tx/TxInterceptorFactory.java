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
package org.jboss.ejb3.tx;

import java.lang.reflect.Method;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import org.jboss.annotation.ejb.TransactionTimeout;
import org.jboss.aop.Advisor;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aop.joinpoint.MethodJoinpoint;
import org.jboss.aspects.tx.TxInterceptor;
import org.jboss.logging.Logger;
import org.jboss.tm.TransactionManagerLocator;
import org.jboss.tm.TxManager;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.ejb3.stateless.StatelessContainer;

/**
 * This interceptor handles transactions for AOP
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class TxInterceptorFactory extends org.jboss.aspects.tx.TxInterceptorFactory
{
   private static final Logger log = Logger
   .getLogger(TxInterceptorFactory.class);

   protected String resolveTxType(Advisor advisor, Joinpoint jp)
   {
      Method method = ((MethodJoinpoint) jp).getMethod();
      TransactionAttribute tx = (TransactionAttribute) advisor.resolveAnnotation(method, TransactionAttribute.class);

      if (tx == null)
         tx = (TransactionAttribute) advisor.resolveAnnotation(TransactionAttribute.class);

      String value = "REQUIRED";
      if (tx != null)
      {
         TransactionAttributeType type = tx.value();

         if (type == null)
         {
            value = "REQUIRED";
         }
         else if (type == TransactionAttributeType.NOT_SUPPORTED)
         {
            value = "NOTSUPPORTED";
         }
         else if (type == TransactionAttributeType.REQUIRES_NEW)
         {
            value = "REQUIRESNEW";
         }
         else
         {
            value = type.name();
         }
      }

      return value;
   }

   protected int resolveTransactionTimeout(Advisor advisor, Method method)
   {
      TransactionTimeout annotation = (TransactionTimeout)advisor.resolveAnnotation(method, TransactionTimeout.class);
      
      if (annotation == null)
         annotation = (TransactionTimeout)advisor.resolveAnnotation(TransactionTimeout.class);
      
      if (annotation != null)
      {
         return annotation.value();
      }

      return -1;
   }

   protected void initializePolicy()
   {
      policy = new Ejb3TxPolicy();
   }

   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   {
      // We have to do this until AOP supports matching based on annotation attributes
      TransactionManagementType type = TxUtil.getTransactionManagementType(advisor);
      if (type == TransactionManagementType.BEAN)
         return new BMTInterceptor(TxUtil.getTransactionManager(), !(advisor instanceof StatefulContainer));

      Method method = ((MethodJoinpoint) jp).getMethod();
      int timeout = resolveTransactionTimeout(advisor, method);

      if (policy == null);
         super.initialize();

      String txType = resolveTxType(advisor, jp).toUpperCase();
      if (txType.equals("REQUIRED"))
      {
         return new TxInterceptor.Required(TxUtil.getTransactionManager(), policy, timeout);
      }
      else if (txType.equals("REQUIRESNEW"))
      {
         return new TxInterceptor.RequiresNew(TxUtil.getTransactionManager(), policy, timeout);
      }
      else
      {
         return super.createPerJoinpoint(advisor, jp);
      }
   }


   public String getName()
   {
      return getClass().getName();
   }
}
