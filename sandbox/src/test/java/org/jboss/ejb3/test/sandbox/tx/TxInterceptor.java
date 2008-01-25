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
package org.jboss.ejb3.test.sandbox.tx;

import java.lang.reflect.Method;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.sandbox.interceptorcontainer.InterceptorContainer;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class TxInterceptor
{
   private static final Logger log = Logger.getLogger(TxInterceptor.class);
   
   @AroundInvoke
   public Object aroundInvoke(InvocationContext ctx) throws Exception
   {
      log.debug("ctx = " + ctx);
      InterceptorContainer container = (InterceptorContainer) ctx.getTarget();
      Method method = (Method) ctx.getParameters()[0];
      //Object args[] = (Object[]) ctx.getParameters()[1];
      
      TransactionManager tm = getTransactionManager();
      Transaction tx = tm.getTransaction();
      if(tx == null)
         throw new RuntimeException("tx mandatory");
      
      return ctx.proceed();
   }
   
   private TransactionManager getTransactionManager()
   {
      try
      {
         return (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
      }
      catch(NamingException e)
      {
         throw new RuntimeException("No java:/TransactionManager found", e);
      }
   }
}
