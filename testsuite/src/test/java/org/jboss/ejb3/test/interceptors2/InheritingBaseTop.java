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
package org.jboss.ejb3.test.interceptors2;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ejb.PostActivate;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PrePassivate;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
public class InheritingBaseTop
{
   @AroundInvoke
   public Object intercept3(InvocationContext ctx) throws Exception
   {
      StatusBean.addInterceptionStatic(new Interception(this, "intercept3"));
      return ctx.proceed();
   }

   @PostConstruct
   void postConstruct3()
   {
      StatusBean.addPostConstruct(new Interception(this, "postConstruct3"));
   }

   @PostActivate
   void postActivate3()
   {
      StatusBean.addPostActivate(new Interception(this, "postActivate3"));
   }
   
   @PrePassivate()
   void prePassivate3()
   {
      StatusBean.addPrePassivate(new Interception(this, "prePassivate3"));
   }

   @PreDestroy()
   void preDestroy3()
   {
      StatusBean.addPreDestroy(new Interception(this, "preDestroy3"));
   }


}
