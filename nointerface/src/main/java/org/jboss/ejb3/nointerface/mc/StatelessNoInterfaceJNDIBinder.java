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

import javax.naming.Name;

import org.jboss.ejb3.nointerface.NoInterfaceEJBViewCreator;
import org.jboss.ejb3.nointerface.invocationhandler.NoInterfaceViewInvocationHandler;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.util.naming.NonSerializableFactory;
import org.jnp.interfaces.NamingParser;

/**
 * StatelessNoInterfaceJNDIBinder
 *
 *  Responsible for binding the appropriate objects corresponding to the
 *  no-interface view of a stateless session bean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class StatelessNoInterfaceJNDIBinder extends NoInterfaceViewJNDIBinder
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(StatelessNoInterfaceJNDIBinder.class);

   protected StatelessNoInterfaceJNDIBinder(Class<?> beanClass, JBossSessionBeanMetaData sessionBeanMetadata)
   {
      super(beanClass, sessionBeanMetadata);
   }

   /**
    * Creates the no-interface view for the bean and binds it to the JNDI
    * under the no-interface view jndi name obtained from <code>sessionBeanMetadata</code>.
    *
    * @see NoInterfaceEJBViewCreator#createView(java.lang.reflect.InvocationHandler, Class)
    */
   @Override
   public void bindNoInterfaceView() throws Exception
   {
      logger.debug("Creating no-interface view for bean " + this.beanClass);

      // Create the view and bind to jndi
      NoInterfaceEJBViewCreator noInterfaceViewCreator = new NoInterfaceEJBViewCreator();
      NoInterfaceViewInvocationHandler invocationHandler = new NoInterfaceViewInvocationHandler(this.container);
      invocationHandler.createSessionProxy(null);
      Object noInterfaceView = noInterfaceViewCreator.createView(invocationHandler, beanClass);
      // bind
      // TODO: Again, the jndi-names for the no-interface view are a mess now. They need to come from
      // the metadata. Let's just go ahead temporarily
      String noInterfaceJndiName = sessionBeanMetadata.getEjbName() + "/no-interface";
      // Bind a reference to nonserializable using NonSerializableFactory as the ObjectFactory
      NamingParser namingParser = new NamingParser();
      Name jndiName = namingParser.parse(noInterfaceJndiName);
      NonSerializableFactory.rebind(jndiName, noInterfaceView, true);

      logger.info("Bound the no-interface view for bean " + beanClass + " to jndi at " + noInterfaceJndiName);

   }

}
