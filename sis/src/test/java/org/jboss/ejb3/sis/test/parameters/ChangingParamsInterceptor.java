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
package org.jboss.ejb3.sis.test.parameters;

import javax.interceptor.InvocationContext;

import org.jboss.ejb3.sis.Interceptor;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ChangingParamsInterceptor implements Interceptor
{
   public Object invoke(InvocationContext context) throws Exception
   {
      // just to get some coverage
      assert context.getContextData() != null;
      assert context.getMethod().getName().equals("sayHi");
      assert context.getTarget() != null;
      
      Object params[] = context.getParameters();
      Object newParams[] = { "*" + params[0].toString() + "*" };
      context.setParameters(newParams);
      return context.proceed();
   }
}
