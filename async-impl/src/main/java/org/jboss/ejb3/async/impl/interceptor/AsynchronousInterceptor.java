/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.async.impl.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.ejb.Asynchronous;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.async.impl.future.AsyncFutureWrapper;
import org.jboss.ejb3.async.impl.hack.DevelopmentHacks;
import org.jboss.ejb3.async.spi.container.AsyncInvocationProcessor;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityContext;

/**
 * AsynchronousInterceptor
 *
 * Examines invocation metadata to determine if this
 * should be handled asynchronously; if so, short-circuits and
 * spawns off into a new Thread, returning a handle back to the client
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AsynchronousInterceptor implements Interceptor
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(AsynchronousInterceptor.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * No-arg constructor required
    */
   public AsynchronousInterceptor()
   {
      log.debug("Created: " + this);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.aop.advice.Interceptor#getName()
    */
   public String getName()
   {
      return this.getClass().getSimpleName();
   }

   /* (non-Javadoc)
    * @see org.jboss.aop.advice.Interceptor#invoke(org.jboss.aop.joinpoint.Invocation)
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      // If asynchronous
      if (this.isAsyncInvocation(invocation))
      {
         // Spawn
         return this.invokeAsync(invocation);
      }
      // Regular synchronous call
      else
      {
         // Continue along the chain
         return invocation.invokeNext();
      }
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Breaks off the specified invocation into 
    * a queue for asynchronous processing, returning 
    * a handle to the task
    */
   private Future<?> invokeAsync(Invocation invocation)
   {
      // Get the target container
      AsyncInvocationProcessor container = this.getInvocationProcessor(invocation);

      // Get the ExecutorService
      ExecutorService executorService = container.getAsynchronousExecutor();

      // Get the existing SecurityContext
      SecurityContext sc = SecurityActions.getSecurityContext();

      // Make the asynchronous task from the invocation
      Callable<Object> asyncTask = new AsyncInvocationTask<Object>(invocation.copy(), sc);

      // Short-circuit the invocation into new Thread 
      Future<Object> task = executorService.submit(asyncTask);

      // Make a Future handle for the caller
      Future<Object> handle = new AsyncFutureWrapper(task);

      // Return
      return handle;
   }

   /**
    * Determines whether the specified invocation is asynchronous
    * by inspecting its metadata
    * 
    * EJB 3.1 4.5.2.2
    */
   private boolean isAsyncInvocation(Invocation invocation)
   {
      // Precondition check
      assert invocation instanceof MethodInvocation : this.getClass().getName() + " supports only "
            + MethodInvocation.class.getSimpleName() + ", but has been passed: " + invocation;
      MethodInvocation si = (MethodInvocation) invocation;

      // Get the actual method
      Method actualMethod = si.getActualMethod();

      // Determine if asynchronous (either returns Future or has @Asynchronous)
      if (invocation.resolveAnnotation(Asynchronous.class) != null || actualMethod.getReturnType().equals(Future.class))
      {
         // Log
         if (log.isTraceEnabled())
         {
            log.trace("Intercepted: " + actualMethod);
         }

         // We'll take it
         return true;
      }

      //TODO 
      /*
       * Business interface defines method with same name, arguments, return type Future<V>
       * of bean impl class is eligible for async handling
       */

      //TODO 
      /*
       * Should this be better handled by jboss-metadata
       * (ie. JBossSessionBeanMetadata.getAsynchronousMethods().match(method))) ? 
       */

      // Has met no conditions
      return false;
   }

   /**
    * 
    * @return
    */
   private AsyncInvocationProcessor getInvocationProcessor(Invocation invocation)
   {
      //TODO Need to get at the container from here
      return new AsyncInvocationProcessor()
      {

         public ExecutorService getAsynchronousExecutor()
         {
            return DevelopmentHacks.getDefaultAsyncExecutorService();
         }
      };
   }

   // --------------------------------------------------------------------------------||
   // Inner Classes ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Task to invoke the held invocation in a new Thread, either 
    * returning the result or throwing the generated Exception
    */
   private class AsyncInvocationTask<V> implements Callable<V>
   {
      private Invocation invocation;

      /**
       * SecurityContext to use for the invocation
       */
      private SecurityContext sc;

      public AsyncInvocationTask(Invocation invocation, SecurityContext sc)
      {
         this.invocation = invocation;
         this.sc = sc;
      }

      @SuppressWarnings("unchecked")
      public V call() throws Exception
      {
         // Get existing security context
         SecurityContext oldSc = SecurityActions.getSecurityContext();

         try
         {
            // Set new sc
            SecurityActions.setSecurityContext(this.sc);

            // Invoke
            return (V) invocation.invokeNext();
         }
         catch (Throwable t)
         {
            throw new Exception(t);
         }
         finally
         {
            // Replace the old security context
            SecurityActions.setSecurityContext(oldSc);
         }
      }

   }

}
