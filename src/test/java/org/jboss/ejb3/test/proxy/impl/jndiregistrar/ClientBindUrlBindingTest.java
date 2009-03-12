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

/**
 * ClientBindUrlBindingTest
 * 
 * Remote Business Interface defining two client bind URLs
 * to be used in the Remoting InvokerLocator
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface ClientBindUrlBindingTest extends BindingTest
{
   /*
    * Define some overridden clientBindUrls
    */
   String CLIENT_BIND_URL_1 = "socket://localhost:4873";

   String CLIENT_BIND_URL_2 = "socket://127.0.0.1:4873";
   
   /*
    * Define the JNDI Bindings
    */
   
   String JNDI_BINDING_1 = "JndiBinding1";
   
   String JNDI_BINDING_2 = "JndiBinding2";

}
