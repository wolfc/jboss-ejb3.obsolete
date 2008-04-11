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
package org.jboss.ejb3.session;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.xml.rpc.handler.MessageContext;

import org.jboss.ejb3.EJBContextImpl;
import org.jboss.ejb3.stateless.StatelessBeanContext;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class SessionContextDelegateBase<J extends SessionContainer> extends EJBContextImpl<J, SessionBeanContext<J>>
      implements
         SessionContext
{
   // Class Members
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(SessionContextDelegateBase.class);
   
   // Constructor
   public SessionContextDelegateBase(SessionBeanContext<J> beanContext)
   {
      super(beanContext);
   }
   
   // Specifications
   
   public abstract EJBLocalObject getEJBLocalObject() throws IllegalStateException;
   
   public abstract EJBObject getEJBObject() throws IllegalStateException;
   
   // Implementations
   
   public <T> T getBusinessObject(Class<T> businessInterface) throws IllegalStateException
   {
      if(businessInterface == null)
         throw new IllegalStateException("businessInterface is null");
      
      return container.getBusinessObject(beanContext, businessInterface); 
   }
   
   public Class<?> getInvokedBusinessInterface() throws IllegalStateException
   {
      return container.getInvokedBusinessInterface();
   }
   
   public MessageContext getMessageContext() throws IllegalStateException
   {
      // disallowed for stateful session beans (EJB3 FR 4.4.1 p 81)
      if(beanContext instanceof StatelessBeanContext)
      {
         MessageContext ctx = ((StatelessBeanContext) beanContext).getMessageContextJAXRPC();
         if(ctx == null)
            throw new IllegalStateException("No message context found");
         return ctx;
      }
      throw new UnsupportedOperationException("Only stateless beans can have a message context");
   }
}
