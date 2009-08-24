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
import org.jboss.logging.Logger;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessBeanContext extends SessionSpecBeanContext<StatelessContainer>
{
   private static final Logger log = Logger.getLogger(StatelessBeanContext.class);
   
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

   /**
    * As of EJBTHREE-1337 this method is deprecated, it'll be removed. The
    * WebServiceContext should already have been injected by the WebServiceContextPropertyInjector.
    * @return the bean property which holds the WebServiceContext
    */
   @Deprecated
   public BeanProperty getWebServiceContextProperty()
   {
      // Also see, https://jira.jboss.org/jira/browse/EJBTHREE-1847
      log.debug("EJBTHREE-1337: do not get WebServiceContext property from stateless bean context, it should already have been injected");
      return webServiceContextProperty;
   }

   @Deprecated
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
