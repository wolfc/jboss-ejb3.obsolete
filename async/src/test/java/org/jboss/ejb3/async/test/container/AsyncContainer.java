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
package org.jboss.ejb3.async.test.container;

import java.util.concurrent.ExecutorService;

import org.jboss.ejb3.async.container.AsyncInvocationProcessor;
import org.jboss.ejb3.async.hack.DevelopmentHacks;
import org.jboss.ejb3.interceptors.direct.DirectContainer;

/**
 * AsyncContainer
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AsyncContainer<T> extends DirectContainer<T> implements AsyncInvocationProcessor
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * To be used for asynchronous invocations
    */
   private ExecutorService asynchronousExecutor;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public AsyncContainer(String name, String domainName, Class<? extends T> beanClass)
   {
      this(name, domainName, beanClass, DevelopmentHacks.getDefaultAsyncExecutorService());
   }

   public AsyncContainer(String name, String domainName, Class<? extends T> beanClass,
         ExecutorService asynchronousExecutor)
   {
      super(name, domainName, beanClass);
      this.setAsynchronousExecutor(asynchronousExecutor);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public ExecutorService getAsynchronousExecutor()
   {
      return asynchronousExecutor;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public void setAsynchronousExecutor(ExecutorService asynchronousExecutor)
   {
      this.asynchronousExecutor = asynchronousExecutor;
   }

}
