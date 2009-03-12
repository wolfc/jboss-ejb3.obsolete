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
package org.jboss.ejb3.test.proxy.impl.jndiregistrar;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * RemoteJndiBindingTest
 * 
 * Remote Business Interface defining three overridden
 * JNDI Bindings, as well as access to test a local binding
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@RemoteBinding(jndiBinding = RemoteJndiBindingTest.JNDI_BINDING_DECLARED_BY_BUSINESS_INTERFACE)
public interface RemoteJndiBindingTest extends BindingTest
{
   /*
    * Define some overridden JNDI Bindings to be declared by the EJB Impl Class
    */
   String JNDI_BINDING_1 = "MyOverriddenBinding1/remote";

   String JNDI_BINDING_2 = "MyOverriddenBinding2/remote";

   /**
    * A JNDI Binding to be declared here on the business interface 
    */
   String JNDI_BINDING_DECLARED_BY_BUSINESS_INTERFACE = "MyOverriddenBindingDeclaredByBusinessInterface/remote";
}
