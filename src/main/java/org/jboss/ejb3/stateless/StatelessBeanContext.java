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
package org.jboss.ejb3.stateless;

import javax.ejb.EJBContext;

import org.jboss.ejb3.session.SessionSpecBeanContext;
import org.jboss.injection.lang.reflect.BeanProperty;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessBeanContext extends SessionSpecBeanContext<StatelessContainer>
{
   private javax.xml.rpc.handler.MessageContext jaxrpcMessageContext;
   private BeanProperty webServiceContextProperty;
   
   protected StatelessBeanContext(StatelessContainer container, Object bean)
   {
      super(container, bean);
   }

   public javax.xml.rpc.handler.MessageContext getMessageContextJAXRPC()
   {
      return jaxrpcMessageContext;
   }

   public void setMessageContextJAXRPC(javax.xml.rpc.handler.MessageContext rpcMessageContext)
   {
      this.jaxrpcMessageContext = rpcMessageContext;
   }

   public BeanProperty getWebServiceContextProperty()
   {
      return webServiceContextProperty;
   }

   public void setWebServiceContextProperty(BeanProperty webServiceContextProperty)
   {
      this.webServiceContextProperty = webServiceContextProperty;
   }

   public void remove()
   {
   }
   
   @Override
   public EJBContext getEJBContext()
   {
      if(ejbContext == null)
         ejbContext = new StatelessSessionContextImpl(this);
      return ejbContext;
   }
}
