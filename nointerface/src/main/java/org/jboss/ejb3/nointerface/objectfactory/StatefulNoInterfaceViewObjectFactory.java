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
package org.jboss.ejb3.nointerface.objectfactory;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.apache.log4j.Logger;
import org.jboss.ejb3.nointerface.factory.StatefulNoInterfaceViewFactory;

/**
 * StatefulNoInterfaceViewObjectFactory
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class StatefulNoInterfaceViewObjectFactory implements ObjectFactory
{

   private static Logger logger = Logger.getLogger(StatefulNoInterfaceViewObjectFactory.class);

   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
         throws Exception
   {

      if (logger.isTraceEnabled())
      {
         logger.trace("Creating object instance through object instance factory for name " + name);
      }
      logger.debug("Creating object instance through object instance factory for name " + name);
      assert obj instanceof Reference : StatefulNoInterfaceViewObjectFactory.class
            + " cannot create view from obj of type " + obj.getClass().getName();

      Reference reference = (Reference) obj;
      String jndiNameOfStatefulProxyFactory = this.getProxyFactoryJNDINameFromReference(reference);
      assert jndiNameOfStatefulProxyFactory != null : "Stateful proxy factory for creating no-interface view, not found in reference";

      // now lookup the factory
      Object proxyFactory = nameCtx.lookup(jndiNameOfStatefulProxyFactory);

      assert proxyFactory instanceof StatefulNoInterfaceViewFactory : "Unexpected type found at jndi name "
            + jndiNameOfStatefulProxyFactory + " Expected type " + StatefulNoInterfaceViewFactory.class.getName();

      StatefulNoInterfaceViewFactory statefulNoInterfaceViewFactory = (StatefulNoInterfaceViewFactory) proxyFactory;

      return statefulNoInterfaceViewFactory.createNoInterfaceView();
      // TODO: Should not throw an exception, return null instead. see objectfactory reference
   }

   protected String getProxyFactoryJNDINameFromReference(Reference ref)
   {
      Enumeration<RefAddr> refAddrs = ref.getAll();
      while (refAddrs.hasMoreElements())
      {
         RefAddr refAddr = refAddrs.nextElement();
         String type = refAddr.getType();
         if (type
               .equals(NoInterfaceViewProxyFactoryRefAddrTypes.STATEFUL_NO_INTERFACE_VIEW_PROXY_FACTORY_JNDI_LOCATION))
         {
            Object jndiNameOfStatefulProxyFactory = refAddr.getContent();
            assert jndiNameOfStatefulProxyFactory instanceof String : "Unexpected type for "
                  + NoInterfaceViewProxyFactoryRefAddrTypes.STATEFUL_NO_INTERFACE_VIEW_PROXY_FACTORY_JNDI_LOCATION;

            return (String) jndiNameOfStatefulProxyFactory;
         }
      }
      return null;
   }

}
