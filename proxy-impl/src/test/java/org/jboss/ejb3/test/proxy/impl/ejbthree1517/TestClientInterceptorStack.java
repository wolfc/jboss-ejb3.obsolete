/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.proxy.impl.ejbthree1517;

/**
 * TestClientInterceptorStack
 * 
 * A Remote Business interface to test the remote client 
 * interceptor stack explicitly defined by @RemoteBinding.interceptorStack
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface TestClientInterceptorStack
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * Default Return Value
    */
   String DEFAULT_RETURN_VALUE = "Standard Return Value from the Bean";

   /*
    * JNDI Bindings
    */

   String JNDI_BINDING_NORMAL_STACK_SFSB = "SFSB-Normal";

   String JNDI_BINDING_OVERRIDDEN_STACK_SFSB = "SFSB-Overridden";

   String JNDI_BINDING_NORMAL_STACK_SLSB = "SLSB-Normal";

   String JNDI_BINDING_OVERRIDDEN_STACK_SLSB = "SLSB-Overridden";

   /*
    * Interceptor Stacks
    */

   String INTERCEPTOR_STACK_OVERRIDE = "OverrideInterceptors_EJBTHREE-1517";

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns the value, which will either be TestClientInterceptorStack.DEFAULT_RETURN_VALUE
    * or something defined by an interceptor in an overridden stack
    */
   String getValue();
}
