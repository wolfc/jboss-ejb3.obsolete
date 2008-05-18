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
package org.jboss.ejb3.proxy.spi.registry;

import org.jboss.ejb3.proxy.factory.ProxyFactory;

/**
 * ProxyFactoryRegistry
 * 
 * A Proxy Factory Registry implementation is responsible for
 * maintaining a Collection of Proxy Factories, each bound 
 * to a unique key.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 * @deprecated To be replaced by using the MC itself as the ProxyFactoryRegistry
 */
@Deprecated
public interface ProxyFactoryRegistry
{
   /**
    * Determines whether a ProxyFactory is registered at the specified key
    * 
    * @param key
    * @return
    */
   boolean isRegistered(String key);

   /**
    * Obtains the ProxyFactory registered with the specified key
    * 
    * @param key
    * @return
    * @throws ProxyFactoryNotRegisteredException If no Proxy Factory is registered under the specified key
    */
   ProxyFactory getProxyFactory(String key) throws ProxyFactoryNotRegisteredException;

   /**
    * Register the specified Proxy Factory with the 
    * specified key.  Is additionally responsible for calling the
    * ProxyFactory.start()
    * 
    * @param key
    * @param value
    * @throws ProxyFactoryAlreadyRegisteredException When a proxy factory is already registered under the
    *       specified key
    */
   void registerProxyFactory(String key, ProxyFactory value) throws ProxyFactoryAlreadyRegisteredException;

   /**
    * Register the specified Proxy Factory with the 
    * specified key.  Is additionally responsible for calling the
    * ProxyFactory.stop()
    * 
    * @param key
    * @throws ProxyFactoryNotRegisteredException If no Proxy Factory is registered under the specified key
    */
   void deregisterProxyFactory(String key) throws ProxyFactoryNotRegisteredException;

}
