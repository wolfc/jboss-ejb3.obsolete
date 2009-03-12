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
package org.jboss.ejb3.proxy.impl.objectfactory.session.stateless;

import java.util.List;
import java.util.Map;

import javax.naming.Name;

import org.jboss.ejb3.proxy.impl.factory.ProxyFactory;
import org.jboss.ejb3.proxy.impl.objectfactory.session.SessionProxyObjectFactory;

/**
 * StatelessSessionProxyObjectFactory
 * 
 * A JNDI ObjectFactory responsible for returning the
 * appropriate SLSB Proxy upon lookup
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatelessSessionProxyObjectFactory extends SessionProxyObjectFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members  -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * TODO
    * 
    * This implementation simply creates a new proxy upon each lookup.  We should
    * improve performance by providing a caching mechanism to cache:
    * 
    * Home
    * Business
    * One per interface-specific business
    * 
    * This cache will have to be done at the Server Side as ObjectFactory is a 
    * client-specific construct recreated upon each lookup
    */
   /**
    * SLSB Object Factories must always create a new SLSB Proxy if one is not
    * available in the internal cache, otherwise use the cached one and return
    * 
    * @param proxyFactory The ProxyFactory to use
    * @param name The JNDI name looked up
    * @param referenceAddresses
    */
   @Override
   protected Object getProxy(ProxyFactory proxyFactory, Name name, Map<String, List<String>> referenceAddresses)
   {
      //TODO Implement caching
      // Just create a new Proxy instance for now
      return this.createProxy(proxyFactory, name, referenceAddresses);
   }

}
