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
package org.jboss.ejb3.test.proxy.impl.common.ejb.slsb;

import javax.ejb.LocalHome;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.LocalHomeBinding;
import org.jboss.ejb3.annotation.RemoteHomeBinding;

/**
 * MyStateless2xOnlyWithBindingsBean
 * 
 * SLSB with only EJB 2.x Views, and explicit JNDI Bindings
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
@LocalHome(MyStatelessLocalHome.class)
@LocalHomeBinding(jndiBinding = MyStateless2xOnlyWithBindingsBean.JNDI_BINDING_LOCAL_HOME)
@RemoteHome(MyStatelessRemoteHome.class)
@RemoteHomeBinding(jndiBinding = MyStateless2xOnlyWithBindingsBean.JNDI_BINDING_REMOTE_HOME)
public class MyStateless2xOnlyWithBindingsBean
{
   public static final String JNDI_BINDING_LOCAL_HOME = "MyStateless2xOnlyWithBindingBeanLocalHome";

   public static final String JNDI_BINDING_REMOTE_HOME = "MyStateless2xOnlyWithBindingBeanRemoteHome";

   public String sayHi(String name)
   {
      return "Hi " + name;
   }
}
