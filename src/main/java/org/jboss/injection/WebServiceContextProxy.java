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
package org.jboss.injection;

import java.security.Principal;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;

/**
 * WebServiceContext proxy that delegates to a ThreadLocal.
 *
 * @see org.jboss.ejb3.stateless.StatelessContainer
 *
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class WebServiceContextProxy implements WebServiceContext
{     
   private static ThreadLocal<WebServiceContext> msgContextAssoc = new ThreadLocal<WebServiceContext>();

   private static final WebServiceContext NOOP = new DefaultDelagate();

   public static void associateMessageContext(WebServiceContext messageContext)
   {
      msgContextAssoc.set(messageContext);
   }

   public MessageContext getMessageContext()
   {
      return delegate().getMessageContext();
   }

   public Principal getUserPrincipal()
   {
      return delegate().getUserPrincipal();
   }

   public boolean isUserInRole(String string)
   {
      return delegate().isUserInRole(string);
   }

   public EndpointReference getEndpointReference(Element... elements)
   {
      return delegate().getEndpointReference(elements);
   }

   public <T extends EndpointReference> T getEndpointReference(Class<T> aClass, Element... elements)
   {
      return delegate().getEndpointReference(aClass, elements);
   }

   private WebServiceContext delegate()
   {
      return (msgContextAssoc.get() != null) ? msgContextAssoc.get() : NOOP;
   }

   private static final class DefaultDelagate implements WebServiceContext
   {
      private final RuntimeException EX = new IllegalStateException("WebServiceContext not available");

      public MessageContext getMessageContext()
      {
         throw EX;
      }

      public Principal getUserPrincipal()
      {
         throw EX;
      }

      public boolean isUserInRole(String string)
      {
         throw EX;
      }

      public EndpointReference getEndpointReference(Element... elements)
      {
         throw EX;
      }

      public <T extends EndpointReference> T getEndpointReference(Class<T> aClass, Element... elements)
      {
         throw EX;
      }
   }
}
