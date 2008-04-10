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
package org.jboss.ejb3.test.interceptors.proxyinstanceadvisor;

import org.jboss.ejb3.interceptors.ManagedObject;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 68647 $
 */
//@Interceptors(ProxiedInterceptor.class)
@ManagedObject
public class ProxiedBean implements MyInterface
{
   private static final Logger log = Logger.getLogger(ProxiedBean.class);
   
   public static ProxiedBean instance;
   
//   public static int constructors = 0, aroundInvokes = 0;
   
   public ProxiedBean()
   {
      log.debug("ProxiedBean");
//      constructors++;
   }
   
//   @AroundInvoke
//   Object aroundInvoke(InvocationContext ctx) throws Exception
//   {
//      log.debug("aroundInvoke " + ctx);
//      if(ctx.getTarget() != this)
//         throw new IllegalStateException("target is not this");
//      if(ctx.getMethod().getDeclaringClass() != getClass())
//         throw new IllegalStateException("method " + ctx.getMethod() + " not of this class (" + ctx.getMethod().getDeclaringClass() + " != " +  getClass() + ")");
//      aroundInvokes++;
//      return ctx.proceed();
//   }
   
   public String sayHi(String name)
   {
      instance = this;
      log.debug("sayHi");
      return "Hi " + name;
   }

   public String sayBye(String name)
   {
      instance = this;
      log.debug("sayBye");
      return "Bye " + name;
   }
}
