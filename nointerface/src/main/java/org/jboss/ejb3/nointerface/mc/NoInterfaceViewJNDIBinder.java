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

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * NoInterfaceViewMCBean
 *
 * A {@link NoInterfaceViewJNDIBinder} corresponds to a EJB which is eligible
 * for a no-interface view
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class NoInterfaceViewJNDIBinder
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(NoInterfaceViewJNDIBinder.class);

   /**
    * The container for which this {@link NoInterfaceViewJNDIBinder} holds
    * an no-interface view
    */
   protected InvokableContext container;

   /**
    * The bean class for which the no-interface view corresponds
    */
   protected Class<?> beanClass;

   /**
    * The bean metadata
    */
   protected JBossSessionBeanMetaData sessionBeanMetadata;

   public static NoInterfaceViewJNDIBinder getNoInterfaceViewJndiBinder(Class<?> beanClass,
         JBossSessionBeanMetaData sessionBeanMetadata)
   {
      return sessionBeanMetadata.isStateful()
            ? new StatefulNoInterfaceJNDIBinder(beanClass, sessionBeanMetadata)
            : new StatelessNoInterfaceJNDIBinder(beanClass, sessionBeanMetadata);
   }

   /**
    * Constructor
    *
    * @param beanClass
    * @param sessionBeanMetadata
    */
   protected NoInterfaceViewJNDIBinder(Class<?> beanClass, JBossSessionBeanMetaData sessionBeanMetadata)
   {
      this.beanClass = beanClass;
      this.sessionBeanMetadata = sessionBeanMetadata;

   }

   public abstract void bindNoInterfaceView() throws Exception;

   /**
    * Will be called when the dependencies of this {@link NoInterfaceViewJNDIBinder} are
    * resolved and this MC bean reaches the START state.
    *
    * At this point, the <code>container</code> associated with this {@link NoInterfaceViewJNDIBinder}
    * is injected and is at a minimal of DESCRIBED state. We now create a no-interface view
    * for the corresponding bean.
    * Note: No validations (like whether the bean is eligible for no-interface view) is done at this
    * stage. It's assumed that the presence of a {@link NoInterfaceViewJNDIBinder} indicates that the
    * corresponding bean is eligible for no-interface view.
    *
    * @throws Exception
    */
   @Start
   public void onStart() throws Exception
   {
      if (logger.isTraceEnabled())
      {
         logger.trace("Creating no-interface view for container " + this.container);
      }

      this.bindNoInterfaceView();
   }

   @Stop
   public void onStop() throws Exception
   {

      //TODO need to unbind
   }

   // Bean name will be added to this Inject by the deployer.
   // We need not use the annotation here at all, since the deployer adds this
   // dynamically. But having this here provides a better understanding about how
   // this field is used
   @Inject(dependentState = "Described")
   public void setContainer(InvokableContext container) throws Exception
   {
      this.container = container;

   }

}
