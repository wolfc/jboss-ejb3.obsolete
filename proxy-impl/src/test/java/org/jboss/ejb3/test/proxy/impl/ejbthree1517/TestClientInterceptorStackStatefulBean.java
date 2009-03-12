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

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteBindings;

/**
 * TestClientInterceptorStackStatefulBean
 * 
 * A test SFSB with 2 bindings, one for the standard
 * client interceptor stack and one for an overridden stack
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Remote(TestClientInterceptorStack.class)
@RemoteBindings(
{
      @RemoteBinding(jndiBinding = TestClientInterceptorStack.JNDI_BINDING_NORMAL_STACK_SFSB),
      @RemoteBinding(jndiBinding = TestClientInterceptorStack.JNDI_BINDING_OVERRIDDEN_STACK_SFSB, interceptorStack = TestClientInterceptorStack.INTERCEPTOR_STACK_OVERRIDE)})
public class TestClientInterceptorStackStatefulBean extends TestClientInterceptorStackBeanBase
      implements
         TestClientInterceptorStack
{

}
