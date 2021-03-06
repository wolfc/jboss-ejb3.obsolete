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
package org.jboss.ejb3.nointerface.mc;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.ejb3.nointerface.factory.MCAwareStatefulNoInterfaceViewFactory;
import org.jboss.ejb3.nointerface.objectfactory.NoInterfaceViewProxyFactoryRefAddrTypes;
import org.jboss.ejb3.nointerface.objectfactory.StatefulNoInterfaceViewObjectFactory;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.util.naming.NonSerializableFactory;

/**
 * StatefulNoInterfaceJNDIBinder
 *
 * Responsible for creating and binding the appropriate objects
 * corresponding to the no-interface view of a stateful session bean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class StatefulNoInterfaceJNDIBinder extends NoInterfaceViewJNDIBinder
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(StatefulNoInterfaceJNDIBinder.class);
   
   /**
    * Suffix to be added to the ejb-name to form the jndi name of no-interface stateful proxyfactory
    * 
    * 
    */
   private static final String NO_INTERFACE_STATEFUL_PROXY_FACTORY_JNDI_NAME_SUFFIX = "/no-interface-stateful-proxyfactory";
   
   /**
    * Constructor
    * @param beanClass The bean class
    * @param sessionBeanMetadata Metadata of the bean
    */
   protected StatefulNoInterfaceJNDIBinder(Context ctx, Class<?> beanClass, JBossSessionBeanMetaData sessionBeanMetadata)
   {
      super(ctx, beanClass, sessionBeanMetadata);

   }

   /**
    * 1) Creates a {@link MCAwareStatefulNoInterfaceViewFactory} and binds it to JNDI (let's call
    * this jndi-name "A")
    *
    * 2) Creates a {@link StatefulNoInterfaceViewObjectFactory} objectfactory and binds a {@link Reference}
    * to this objectfactory into the JNDI (let's call it jndi-name "B").
    *
    * The objectfactory will have a reference to the jndi-name of the stateful factory (created in step#1).
    * This will then be used by the object factory to lookup the stateful factory for creating the no-interface
    * view when the client does a lookup.
    *
    *
    */
   @Override
   public void bindNoInterfaceView() throws NamingException
   {
      logger.debug("Binding no-interface view statefulproxyfactory and the objectfactory for bean " + this.beanClass);

      // This factory will be bound to JNDI and will be invoked (through an objectfactory) to create
      // the no-interface view for a SFSB
      MCAwareStatefulNoInterfaceViewFactory statefulNoInterfaceViewFactory = new MCAwareStatefulNoInterfaceViewFactory(
            this.beanClass, this.endpointContext);

      // TODO - Needs to be a proper jndi name for the factory
      String statefulProxyFactoryJndiName = sessionBeanMetadata.getEjbName() + NO_INTERFACE_STATEFUL_PROXY_FACTORY_JNDI_NAME_SUFFIX;
      // Bind the proxy factory to jndi
      NonSerializableFactory.rebind(this.jndiCtx, statefulProxyFactoryJndiName, statefulNoInterfaceViewFactory, true);

      // Create an Reference which will hold the jndi-name of the statefulproxyfactory which will
      // be responsible for creating the no-interface view for the stateful bean upon lookup
      Reference reference = new Reference(
            NoInterfaceViewProxyFactoryRefAddrTypes.STATEFUL_NO_INTERFACE_VIEW_OBJECT_FACTORY_KEY,
            StatefulNoInterfaceViewObjectFactory.class.getName(), null);
      RefAddr refAddr = new StringRefAddr(
            NoInterfaceViewProxyFactoryRefAddrTypes.STATEFUL_NO_INTERFACE_VIEW_PROXY_FACTORY_JNDI_LOCATION,
            statefulProxyFactoryJndiName);
      // add this refaddr to the reference which will be bound to jndi
      reference.add(refAddr);

      // TODO: Again, the jndi-names for the no-interface view are a mess now. They need to come from
      // the metadata. Let's just go ahead temporarily
      String noInterfaceJndiName = sessionBeanMetadata.getEjbName() + NO_INTERFACE_JNDI_SUFFIX;
      this.jndiCtx.bind(noInterfaceJndiName, reference);
      logger.info("Bound the no-interface view for bean " + beanClass + " to jndi at " + noInterfaceJndiName);

   }

   /**
    * Unbind the {@link MCAwareStatefulNoInterfaceViewFactory} and the {@link StatefulNoInterfaceViewObjectFactory}
    * from the jndi
    * 
    * @see org.jboss.ejb3.nointerface.mc.NoInterfaceViewJNDIBinder#unbindNoInterfaceView()
    */
   @Override
   public void unbindNoInterfaceView() throws NamingException
   {
      this.jndiCtx.unbind(this.sessionBeanMetadata.getEjbName() + NO_INTERFACE_JNDI_SUFFIX);
      this.jndiCtx.unbind(this.sessionBeanMetadata.getEjbName() + NO_INTERFACE_STATEFUL_PROXY_FACTORY_JNDI_NAME_SUFFIX);
      
   }

}
