/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.proxy;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.jboss.logging.Logger;
import org.jnp.interfaces.MarshalledValuePair;

/**
 * JndiProxyFactory
 * 
 * Responsible for delegating to the appropriate Proxy Factory
 * for creation of a Proxy, and setting appropriately 
 * the target "
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class JndiSessionProxyObjectFactory implements ObjectFactory
{
   // -------------------------------------------------------------------------------------------||
   // Class Members -----------------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------------||

   private static final Logger logger = Logger.getLogger(JndiSessionProxyObjectFactory.class);

   public static final String REF_ADDR_NAME_JNDI_BINDING_DELEGATE_PROXY_FACTORY = "FACTORY";
   
   public static final String REF_ADDR_NAME_BUSINESS_INTERFACE_TYPE = "BUSINESS_INTERFACE_TYPE";

   // -------------------------------------------------------------------------------------------||
   // Required Implementations ------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------------||

   /**
    * Using the ProxyFactory located at the RefAddr(REF_ADDR_NAME_JNDI_BINDING_DELEGATE_PROXY_FACTORY)
    * specified by obj, create a new Proxy, set the target business interface
    * (if specified by RefAddr(REF_ADDR_NAME_BUSINESS_INTERFACE_TYPE)), and return to the client.
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
         throws Exception
   {
      // Initialize
      Reference ref = null;
      String proxyFactoryJndiBinding = null;
      ProxyFactory proxyFactory = null;

      // Obtain the reference
      try
      {
         ref = Reference.class.cast(obj);
         logger.trace("Obtained " + Reference.class.getName() + " from " + obj);
      }
      catch (ClassCastException cce)
      {
         // Throw descriptive exception
         throw new RuntimeException("Object at JNDI at " + nameCtx + name.toString() + " was not of expected type "
               + Reference.class.getName(), cce);
      }

      // Obtain the JNDI Binding of the factory to use
      try
      {
         RefAddr refAddr = ref.get(JndiSessionProxyObjectFactory.REF_ADDR_NAME_JNDI_BINDING_DELEGATE_PROXY_FACTORY);
         Object content = refAddr.getContent();
         proxyFactoryJndiBinding = String.class.cast(content);
         logger.trace(ProxyFactory.class.getName() + " is located in JNDI at " + proxyFactoryJndiBinding);
      }
      catch (ClassCastException cce)
      {
         // Throw descriptive exception
         throw new RuntimeException("Content for Reference name "
               + JndiSessionProxyObjectFactory.REF_ADDR_NAME_JNDI_BINDING_DELEGATE_PROXY_FACTORY + " bound in JNDI at "
               + nameCtx + name.toString() + " was not of expected type " + String.class.getName(), cce);
      }

      // Get the Proxy Factory from JNDI
      Object proxyFactoryObj = nameCtx.lookup(proxyFactoryJndiBinding);
      try
      {
         proxyFactory = ProxyFactory.class.cast(proxyFactoryObj);
         logger.trace("Found " + ProxyFactory.class.getName() + " instance at " + proxyFactoryJndiBinding);
      }
      catch (ClassCastException e)
      {
         throw new RuntimeException("Expected " + ProxyFactory.class.getName() + " instance in JNDI at "
               + proxyFactoryJndiBinding + ", instead got " + proxyFactoryObj);
      }

      // Create a business (EJB30 View) Proxy via the Factory
      Object proxy = proxyFactory.createProxyBusiness();
      
      // Marshall and return
      MarshalledValuePair marshalledProxy = new MarshalledValuePair(proxy);
      return marshalledProxy.get();
   }
}
