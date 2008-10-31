/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.session;

import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.proxy.ClassProxy;
import org.jboss.aop.proxy.ProxyMixin;

/**
 * Don't ask. Try observing a volcano eruption from 1 mile or outrun
 * a lightning bolt. It's safer.
 *
 * org.jboss.aop.Dispatcher can handle only certain types.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
class ClassProxyHack implements ClassProxy
{
   private SessionContainer container;
   
   ClassProxyHack(SessionContainer container)
   {
      assert container != null : "container is null";
      
      this.container = container;
   }
   
   public InvocationResponse _dynamicInvoke(Invocation invocation) throws Throwable
   {
      return container.dynamicInvoke(null, invocation);
   }

   public void setMixins(ProxyMixin[] mixins)
   {
      throw new RuntimeException("Go away, stop bothering me");
   }

   public InstanceAdvisor _getInstanceAdvisor()
   {
      throw new RuntimeException("Go away, stop bothering me");
   }

   public void _setInstanceAdvisor(InstanceAdvisor newAdvisor)
   {
      throw new RuntimeException("Go away, stop bothering me");
   }

}
