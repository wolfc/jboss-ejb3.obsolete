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
package org.jboss.ejb3.test.interceptors.proxyinstanceadvisor;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;
import org.jboss.logging.Logger;

/**
 * Creates a new bean instance when told to
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class SimplePoolInterceptor implements Interceptor
{
   public static boolean createNewInstance;
   
   private static ProxiedBean pooledBean;
   
   Logger log = Logger.getLogger(SimplePoolInterceptor.class);

   public String getName()
   {
      return this.getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      ContainerMethodInvocation mi = (ContainerMethodInvocation)invocation;
      
      if (createNewInstance == false && pooledBean == null)
      {
         throw new RuntimeException("createNewInstance was false on an empty pool");
      }
      
      @SuppressWarnings("unchecked")
      SimpleContext ctx = (SimpleContext)mi.getBeanContext(); 
      ProxiedBean bean = createNewInstance ? new ProxiedBean() : pooledBean;
      pooledBean = bean;
      
      log.debug("Using instance " + bean);
      
      ctx.setInstance(bean);
      //mi.setTargetObject(bean);
      mi.setBeanContext(ctx);
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         ctx.setInstance(null);
         mi.setBeanContext(null);
      }
   }

}
