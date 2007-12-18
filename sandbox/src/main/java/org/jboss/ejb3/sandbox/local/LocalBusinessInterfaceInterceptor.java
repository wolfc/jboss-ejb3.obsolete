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
package org.jboss.ejb3.sandbox.local;

import java.lang.reflect.Proxy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Local;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;

import org.jboss.ejb3.sandbox.interceptorcontainer.InterceptorContainer;
import org.jboss.logging.Logger;
import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.util.naming.Util;

/**
 * Creates and binds a proxy for invoking beans over a local interface.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class LocalBusinessInterfaceInterceptor
{
   private static final Logger log = Logger.getLogger(LocalBusinessInterfaceInterceptor.class);
   
   @PostConstruct
   public void postConstruct(InterceptorContainer.BeanClassInvocationContext ctx) throws Exception
   {
      log.debug("postConstruct " + ctx);
      
      Class<?> businessInterfaces[];
      Local local = ctx.getTarget().getAnnotation(Local.class);
      if(local != null)
         businessInterfaces = local.value();
      else if(ctx.getTarget().getBeanClass().getInterfaces().length == 1)
         businessInterfaces = new Class<?>[] { ctx.getTarget().getBeanClass().getInterfaces()[0] };
      else
         throw new IllegalArgumentException("TODO");
      
      // TODO: determine JNDI name
      String jndiName = ctx.getTarget().getBeanClass().getSimpleName() + "/local";
      log.debug("jndiName = " + jndiName);

      Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), businessInterfaces, new LocalProxy(ctx.getTarget()));
      
      Util.createSubcontext(new InitialContext(), ctx.getTarget().getBeanClass().getSimpleName());
      NonSerializableFactory.rebind(new InitialContext(), jndiName, proxy);
      
      ctx.proceed();
   }
   
   @PreDestroy
   public void preDestroy(InvocationContext ctx) throws Exception
   {
      log.debug("preDestroy");
      
      ctx.proceed();
   }
}
