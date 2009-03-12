/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.proxy.common.container;

import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.proxy.ClassProxy;
import org.jboss.aop.proxy.ProxyMixin;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.util.NotImplementedException;

/**
 * ProxyTestClassProxyHack
 *
 * Adapted from ClassProxyHack in EJB3 Core
 * (ie. Carlo).
 * 
 * Used to expose the dynamic invocation handling
 * of an InvokableContext to Remoting
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
class ProxyTestClassProxyHack implements ClassProxy
{
   private InvokableContext container;

   ProxyTestClassProxyHack(InvokableContext container)
   {
      assert container != null : "Specified " + InvokableContext.class.getSimpleName() + " may not be null.";
      this.container = container;
   }

   public InvocationResponse _dynamicInvoke(Invocation invocation) throws Throwable
   {
      return container.dynamicInvoke(invocation);
   }

   public void setMixins(ProxyMixin[] mixins)
   {
      throw new NotImplementedException();
   }

   public InstanceAdvisor _getInstanceAdvisor()
   {
      throw new NotImplementedException();
   }

   public void _setInstanceAdvisor(InstanceAdvisor newAdvisor)
   {
      throw new NotImplementedException();
   }

}
