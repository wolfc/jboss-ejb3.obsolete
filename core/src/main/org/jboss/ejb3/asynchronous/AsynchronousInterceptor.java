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
package org.jboss.ejb3.asynchronous;

import java.security.Principal;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aspects.asynch.AsynchAspect;
import org.jboss.aspects.asynch.Future;
import org.jboss.aspects.asynch.FutureHolder;
import org.jboss.aspects.asynch.ThreadPoolExecutor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.tx.ClientTxPropagationInterceptor;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.security.SecurityAssociation;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextUtil;


/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
public class AsynchronousInterceptor extends AsynchAspect implements Interceptor
{
   private static final Logger log = Logger.getLogger(AsynchronousInterceptor.class);

   public static final String ASYNCH = "ASYNCH";
   public static final String INVOKE_ASYNCH = "INVOKE_ASYNCH";
   public static final String FUTURE_HOLDER = "FUTURE_HOLDER";

   public AsynchronousInterceptor()
   {
      try
      {
         super.executor = (org.jboss.aspects.asynch.ExecutorAbstraction)ThreadPoolExecutor.class.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }

   public String getName()
   {
      return "AsynchronousInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      MethodInvocation mi = (MethodInvocation) invocation;

      if (invocation.getMetaData(ASYNCH, INVOKE_ASYNCH) != null)
      {
         //TODO This should maybe go somewhere nicer
         InvokerLocator locator = (InvokerLocator) invocation.getMetaData(InvokeRemoteInterceptor.REMOTING, InvokeRemoteInterceptor.INVOKER_LOCATOR);
         if (locator == null)
         {
            //We are a local invocation. Add security and current transaction info to the invocation
            // (for remote invocations this is done by the client side interceptors)
            TransactionPropagationContextFactory tpcFactory = TransactionPropagationContextUtil.getTPCFactoryClientSide();
            if (tpcFactory != null)
            {
               Object tpc = tpcFactory.getTransactionPropagationContext();
               if (tpc != null)
               {
                  invocation.getMetaData().addMetaData(ClientTxPropagationInterceptor.TRANSACTION_PROPAGATION_CONTEXT,
                                                       ClientTxPropagationInterceptor.TRANSACTION_PROPAGATION_CONTEXT, tpc);
               }
            }

            Principal principal = SecurityAssociation.getPrincipal();
            if (principal != null) invocation.getMetaData().addMetaData("security", "principal", principal);

            Object credential = SecurityAssociation.getCredential();
            if (credential != null) invocation.getMetaData().addMetaData("security", "credential", credential);
         }

         return super.execute(mi);
      }
      return mi.invokeNext();
   }

   //@Override
   protected void setupLocalFuture(MethodInvocation invocation, Future future)
   {
      FutureHolder provider = (FutureHolder) invocation.getMetaData(ASYNCH, FUTURE_HOLDER);
      provider.setFuture(future);
   }

   /**
    * We don't want to generate proxies for ejb 3 clients, to avoid dependencies on javassist. 
    * Use a dynamic proxy to the future instead
    */
   //@Override
   protected boolean generateProxy()
   {
      return false;
   }

   
}
