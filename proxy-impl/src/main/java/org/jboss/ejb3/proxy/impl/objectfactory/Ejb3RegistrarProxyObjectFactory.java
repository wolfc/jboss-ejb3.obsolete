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
package org.jboss.ejb3.proxy.impl.objectfactory;

import java.io.Serializable;

import javax.naming.spi.ObjectFactory;

import org.jboss.logging.Logger;

/**
 * Ejb3RegistrarProxyObjectFactory
 *
 * A Proxy Object Factory using an underlying 
 * Proxy Factory Registry intended to be obtained
 * as a managed object from the Object Store
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class Ejb3RegistrarProxyObjectFactory extends ProxyObjectFactory implements ObjectFactory, Serializable
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(Ejb3RegistrarProxyObjectFactory.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public Ejb3RegistrarProxyObjectFactory()
   {

   }

}
