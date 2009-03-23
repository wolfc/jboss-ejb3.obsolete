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
import java.lang.reflect.InvocationHandler;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.ejb3.endpoint.SessionFactory;
import org.jboss.ejb3.nointerface.NoInterfaceEJBViewCreator;
import org.jboss.ejb3.nointerface.invocationhandler.MCAwareNoInterfaceViewInvocationHandler;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
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
public class MCAwareStatefulNoInterfaceViewFactory
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(MCAwareStatefulNoInterfaceViewFactory.class);

   /**
    * The bean class
    */
   protected Class<?> beanClass;

   /**
    * The KernelControllerContext corresponding to the container of a bean for which
    * the no-interface view is to be created by this factory. This context
    * may <i>not</i> be in INSTALLED state. This factory is responsible
    * for pushing it to INSTALLED state whenever necessary. 
    * 
    */
   protected KernelControllerContext containerContext;

   /**
    * The KernelControllerContext corresponding StatefulSessionFactory. This context
    * may <i>not</i> be in INSTALLED state. This factory is responsible
    * for pushing it to INSTALLED state whenever necessary. 
    * 
    */
   protected KernelControllerContext statefulSessionFactoryContext;

   /**
    * Constructor
    * @param beanClass
    * @param container
    * @param statefulSessionFactory
    */
   public MCAwareStatefulNoInterfaceViewFactory(Class<?> beanClass, KernelControllerContext containerContext,
         KernelControllerContext statefulSessionFactoryContext)
   {
      this.beanClass = beanClass;
      this.containerContext = containerContext;
      this.statefulSessionFactoryContext = statefulSessionFactoryContext;
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
      try
      {
         // first push the statefulSessionFactoryContext to INSTALLED
         if (logger.isTraceEnabled())
         {
            logger.trace("Changing the context " + this.statefulSessionFactoryContext.getName() + " to state "
                  + ControllerState.INSTALLED.getStateString() + " from current state "
                  + this.statefulSessionFactoryContext.getState().getStateString());
         }
         this.statefulSessionFactoryContext.getController().change(this.statefulSessionFactoryContext,
               ControllerState.INSTALLED);
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Could not push the context " + this.statefulSessionFactoryContext.getName()
               + " from its current state " + this.statefulSessionFactoryContext.getState().getStateString()
               + " to INSTALLED", t);
      }

      // now get hold of the StatefulSessionFactory from the context
      Object statefulSessionFactory = this.statefulSessionFactoryContext.getTarget();
      assert statefulSessionFactory instanceof SessionFactory : "Unexpected object type found "
            + statefulSessionFactory + " - expected a " + SessionFactory.class;

      // create the session
      Serializable session = ((SessionFactory) statefulSessionFactory).createSession(null, null);
      logger.debug("Created session " + session + " for " + this.beanClass);

      // create an invocation handler
      InvocationHandler invocationHandler = new MCAwareNoInterfaceViewInvocationHandler(this.containerContext, session);
      // Now create the view for this bean class and the newly created invocation handler
      // TODO: Incorrect cardinality
      NoInterfaceEJBViewCreator noInterfaceViewCreator = new NoInterfaceEJBViewCreator();
      Object noInterfaceView = noInterfaceViewCreator.createView(invocationHandler, beanClass);

      if (logger.isTraceEnabled())
      {
         logger.trace("Created no-interface view " + noInterfaceView + " for bean " + beanClass);
      }
      return noInterfaceView;
   }

}
