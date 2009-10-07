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

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.beans.metadata.api.model.FromContext;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * NoInterfaceViewJNDIBinder
 *
 * A {@link NoInterfaceViewJNDIBinder} corresponds to a EJB which is eligible
 * for a no-interface view
 * 
 * This MC bean has dependencies (like the container) injected as necessary.
 * During its START phase this NoInterfaceViewJNDIBinder creates a no-interface view
 * and binds it to the jndi. 
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
    * The endpoint for which this {@link NoInterfaceViewJNDIBinder} holds
    * an no-interface view
    */
   // Bean name will be added to this Inject by the deployer.
   // We need NOT use the annotation here at all, since the deployer adds this
   // dynamically. But having this here provides a better understanding about how
   // this field is used
   @Inject(dependentState = "Described", fromContext = FromContext.CONTEXT)
   protected KernelControllerContext endpointContext;

   /**
    * The bean class for which the no-interface view corresponds
    */
   protected Class<?> beanClass;

   /**
    * The bean metadata
    */
   protected JBossSessionBeanMetaData sessionBeanMetadata;

   /**
    * JNDI naming context
    */
   protected Context jndiCtx;

   /**
    * Suffix to be added to the ejb-name to form the jndi name of no-interface view
    * 
    * TODO: Until the no-interface jndi-name comes from metadata, we need to hardcode the jndi-name
    * 
    */
   protected static final String NO_INTERFACE_JNDI_SUFFIX = "/no-interface";

   /**
    * Returns an appropriate instance of {@link NoInterfaceViewJNDIBinder} based on the 
    * <code>sessionBeanMetadata</code>
    * 
    * @param ctx JNDI naming context into which this {@link NoInterfaceViewJNDIBinder} will be
    *           responsible for binding/unbinding objects
    * @param beanClass Bean class
    * @param sessionBeanMetadata Session bean metadata of the bean class
    * @return 
    */
   public static NoInterfaceViewJNDIBinder getNoInterfaceViewJndiBinder(Context ctx, Class<?> beanClass,
         JBossSessionBeanMetaData sessionBeanMetadata)
   {
      return sessionBeanMetadata.isStateful()
            ? new StatefulNoInterfaceJNDIBinder(ctx, beanClass, sessionBeanMetadata)
            : new StatelessNoInterfaceJNDIBinder(ctx, beanClass, sessionBeanMetadata);
   }

   /**
    * Constructor
    *
    * @param beanClass
    * @param sessionBeanMetadata
    */
   protected NoInterfaceViewJNDIBinder(Context ctx, Class<?> beanClass, JBossSessionBeanMetaData sessionBeanMetadata)
   {
      this.jndiCtx = ctx;
      this.beanClass = beanClass;
      this.sessionBeanMetadata = sessionBeanMetadata;

   }

   /**
    * Bind the no-interface view 
    * 
    * @throws NamingException If any exception while binding to JNDI
    */
   public abstract void bindNoInterfaceView() throws NamingException;

   /**
    * Unbind the no-interface view
    * 
    * @throws NamingException If any exception while unbinding from JNDI
    */
   public abstract void unbindNoInterfaceView() throws NamingException;

   /**
    * Will be called when the dependencies of this {@link NoInterfaceViewJNDIBinder} are
    * resolved and this MC bean reaches the START state.
    *
    * At this point, the {@link #endpointContext} associated with this {@link NoInterfaceViewJNDIBinder}
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
         logger.trace("Creating no-interface view for endpoint " + this.endpointContext);
      }

      this.bindNoInterfaceView();
   }

   /**
    * Does any relevant cleanup
    *  
    * @throws Exception
    */
   @Stop
   public void onStop() throws Exception
   {
      if (logger.isTraceEnabled())
      {
         logger.trace("Unbinding no-interface view from JNDI, for endpoint " + this.endpointContext);
      }
      this.unbindNoInterfaceView();
   }

   /**
    * 
    * @param endpointContext The KernelControllerContext corresponding to the endpoint
    * @throws Exception
    */
   public void setEndpointContext(KernelControllerContext endpointContext) throws Exception
   {
      this.endpointContext = endpointContext;

   }

}
