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
package org.jboss.ejb3.async.impl.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.logging.Logger;

/**
 * AsyncFutureWrapper
 * 
 * Client view of an EJB 3.1 Asynchronous invocation's return
 * value
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AsyncFutureWrapper<V> implements Future<V>
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(AsyncFutureWrapper.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Underlying target for inquiry
    */
   private Future<Future<V>> target;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructs a new AsyncFuture for the given invocation
    * 
    * @param target The Future returned by the executor
    */
   public AsyncFutureWrapper(final Future<Future<V>> target)
   {
      // Set properties
      this.setTarget(target);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public boolean cancel(boolean mayInterruptIfRunning)
   {
      return this.getUnderlyingTarget().cancel(mayInterruptIfRunning);
   }

   /**
    * Obtains the 
    */
   public V get() throws InterruptedException, ExecutionException
   {
      // Get the underlying target
      Future<V> underlyingTarget = this.getUnderlyingTarget();

      // Invoke (ignore a null value as this indicates no return/void)
      V returnValue = underlyingTarget == null ? null : underlyingTarget.get();

      // Return
      return returnValue;
   }

   public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
   {
      Future<V> underlyingTarget = this.getUnderlyingTarget();
      return underlyingTarget == null ? null : underlyingTarget.get(timeout, unit);
   }

   public boolean isCancelled()
   {
      return this.getUnderlyingTarget().isCancelled();
   }

   public boolean isDone()
   {
      return this.getUnderlyingTarget().isDone();
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private void setTarget(Future<Future<V>> target)
   {
      // Precondition checks
      assert target != null : "target must be specified";

      // Set
      this.target = target;
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private Future<V> getUnderlyingTarget()
   {
      try
      {
         return target.get();
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      catch (ExecutionException e)
      {
         throw new RuntimeException(e);
      }
   }

}
