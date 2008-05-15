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
package org.jboss.ejb3.proxy.mc;

/**
 * MicrocontainerBindings
 * 
 * Defines constants used in binding POJOs to
 * Microcontainer
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface MicrocontainerBindings
{
   // --------------------------------------------------------------------------------||
   // Constants ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   String MC_EJB3_NAMESPACE = "org.jboss.ejb3.";

   /*
    * The following are Bindings used as MC Bean Names
    */

   String MC_BEAN_NAME_PROXY_FACTORY_REGISTRY = MicrocontainerBindings.MC_EJB3_NAMESPACE + "ProxyFactoryRegistry";

   String MC_BEAN_NAME_JNDI_REGISTRAR = MicrocontainerBindings.MC_EJB3_NAMESPACE + "JndiRegistrar";
   
}
