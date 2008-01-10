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
package org.jboss.ejb3;

import java.util.Hashtable;

import javax.ejb.EJBException;

import org.jnp.interfaces.MarshalledValuePair;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import org.jboss.logging.Logger;

/**
 * Responsible for creating an EJB proxy
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class JndiProxyFactory implements ObjectFactory
{
   private static final Logger log = Logger.getLogger(JndiProxyFactory.class);

   public static final String FACTORY = "FACTORY";

   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
   {
      Reference ref = (Reference) obj;
      String factoryName = (String) ref.get(FACTORY).getContent();
     
      try
      {
         ProxyFactory factory = (ProxyFactory) nameCtx.lookup(factoryName);
         
         Object proxy = factory.createProxy();
         MarshalledValuePair marshalledProxy = new MarshalledValuePair(proxy);
         return marshalledProxy.get();
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (ClassCastException e)
      {
         throw new EJBException("Invalid invocation of local interface", e);
      }
   }
}
