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
package org.jboss.ejb3.test.interceptors.metadata;

import javax.interceptor.InvocationContext;

import org.jboss.ejb3.interceptors.ManagedObject;
import org.jboss.logging.Logger;

/**
 * All interceptors defined in metadata.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@ManagedObject
public class MetadataBean
{
   private static final Logger log = Logger.getLogger(MetadataBean.class);

   public static int constructors = 0, aroundInvokes = 0;
   
   Object aroundInvoke(InvocationContext ctx) throws Exception
   {
      log.debug("aroundInvoke " + ctx);
      if(ctx.getTarget() != this)
         throw new IllegalStateException("target is not this");
      if(ctx.getMethod().getDeclaringClass() != getClass())
         throw new IllegalStateException("method " + ctx.getMethod() + " not of this class (" + ctx.getMethod().getDeclaringClass() + " != " +  getClass() + ")");
      aroundInvokes++;
      return ctx.proceed();
   }
   
   public String sayHi(String name)
   {
      log.debug("sayHi");
      return "Hi " + name;
   }
   
   public void intercept()
   {
      log.debug("intercept");
   }
}
