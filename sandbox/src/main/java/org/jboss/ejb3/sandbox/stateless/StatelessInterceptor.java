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
package org.jboss.ejb3.sandbox.stateless;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.pool.StatelessObjectFactory;
import org.jboss.ejb3.pool.strictmax.StrictMaxPool;
import org.jboss.ejb3.sandbox.interceptorcontainer.InterceptorContainer;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class StatelessInterceptor
{
   private static final Logger log = Logger.getLogger(StatelessInterceptor.class);
   
   private Pool<Object> pool;
   
   public static volatile long invocations, accumelatedWaitingTime, accumelatedExecutionTime;
   
   public StatelessInterceptor()
   {
   }
   
   @AroundInvoke
   public Object invoke(InvocationContext ctx) throws Exception
   {
      /*
      // TODO: a lot, the pool should be here
      InterceptorContainer container = (InterceptorContainer) ctx.getTarget();
      Object instance = container.getBeanClass().newInstance();
      */
      long startWait = System.currentTimeMillis();
      Object instance = pool.get();
      long startExecution = System.currentTimeMillis();
      try
      {
         log.debug("ctx = " + ctx);
         Method method = (Method) ctx.getParameters()[0];
         Object args[] = (Object[]) ctx.getParameters()[1];
         Object result = method.invoke(instance, args);
         ctx.proceed();
         return result;
      }
      finally
      {
         long endExecution = System.currentTimeMillis();
         pool.release(instance);
         
         invocations++;
         accumelatedWaitingTime += (startExecution - startWait);
         accumelatedExecutionTime += (endExecution - startExecution);
      }
   }
   
   @PostConstruct
   public void postConstruct(InvocationContext ctx) throws Exception
   {
      log.info("Creating pool");
      
      InterceptorContainer container = (InterceptorContainer) ctx.getTarget();
      final Class<?> beanClass = container.getBeanClass();
      StatelessObjectFactory<Object> factory = new StatelessObjectFactory<Object>()
      {
         public Object create()
         {
            try
            {
               return beanClass.newInstance();
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

         public void destroy(Object obj)
         {
            // TODO Auto-generated method stub
            
         }
      };
      pool = new StrictMaxPool<Object>(factory, 5, 30, TimeUnit.SECONDS);
   }
}
