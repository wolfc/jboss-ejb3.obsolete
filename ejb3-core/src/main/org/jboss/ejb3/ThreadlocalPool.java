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
package org.jboss.ejb3;

import org.jboss.lang.ref.WeakThreadLocal;
import org.jboss.logging.Logger;


/**
 * Pools EJBs within a ThreadLocal.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ThreadlocalPool extends AbstractPool
{
   private static final Logger log = Logger.getLogger(ThreadlocalPool.class);
   
   protected WeakThreadLocal<BeanContext> pool = new WeakThreadLocal<BeanContext>();

   public ThreadlocalPool()
   {
   }

   public void destroy()
   {
      log.trace("destroying pool");
      
      // This really serves little purpose, because we want the whole thread local map to die
      pool.remove();
   }
   
   public BeanContext get()
   {
      BeanContext ctx = pool.get();
      if (ctx != null)
      {
         pool.set(null);
         return ctx;
      }

      ctx = create();
      return ctx;
   }

   public BeanContext get(Class[] initTypes, Object[] initValues)
   {
      BeanContext ctx = pool.get();
      if (ctx != null)
      {
         pool.set(null);
         return ctx;
      }

      ctx = create(initTypes, initValues);
      return ctx;
   }

   public void release(BeanContext ctx)
   {
      if (pool.get() != null)
         remove(ctx);
      else
         pool.set(ctx);
   }

}
