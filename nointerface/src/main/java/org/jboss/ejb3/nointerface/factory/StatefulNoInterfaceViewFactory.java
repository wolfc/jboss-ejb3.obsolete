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
package org.jboss.ejb3.nointerface.factory;

import java.io.Serializable;

import org.jboss.ejb3.nointerface.NoInterfaceEJBViewCreator;
import org.jboss.ejb3.nointerface.invocationhandler.NoInterfaceViewInvocationHandler;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.container.StatefulSessionInvokableContext;
import org.jboss.logging.Logger;

/**
 * StatefulNoInterfaceViewFactory
 *
 * Responsible for (not necessarily in the following order)
 * - Creating a session from the stateful container
 * - Creating the no-interface view for a stateful session bean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class StatefulNoInterfaceViewFactory
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(StatefulNoInterfaceViewFactory.class);

   /**
    * The bean class
    */
   protected Class<?> beanClass;

   /**
    * The container corresponding to the bean for which
    * the no-interface view is to be created by this factory
    */
   protected InvokableContext container;

   /**
    * Constructor
    * @param beanClass
    * @param container
    */
   public StatefulNoInterfaceViewFactory(Class<?> beanClass, InvokableContext container)
   {
      this.beanClass = beanClass;
      this.container = container;
   }

   /**
    * Creates the no-interface view and other necessary steps including (session creation)
    * for the bean
    *
    * @return
    * @throws Exception
    */
   public Object createNoInterfaceView() throws Exception
   {
      logger.debug("Creating no-interface view for " + this.beanClass);

      StatefulSessionInvokableContext statefulContainer = (StatefulSessionInvokableContext) container;
      Serializable session = statefulContainer.createSession();
      logger.debug("Created session " + session + " for " + this.beanClass);

      NoInterfaceViewInvocationHandler invocationHandler = new NoInterfaceViewInvocationHandler(container);
      invocationHandler.setProxy(session);
      // Now create the view for this bean class and the newly created invocation handler
      NoInterfaceEJBViewCreator noInterfaceViewCreator = new NoInterfaceEJBViewCreator();
      Object noInterfaceView = noInterfaceViewCreator.createView(new NoInterfaceViewInvocationHandler(container),
            beanClass);

      if (logger.isTraceEnabled())
      {
         logger.trace("Created no-interface view " + noInterfaceView + " for bean " + beanClass);
      }
      return noInterfaceView;
   }

}
