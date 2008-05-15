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
package org.jboss.ejb3.proxy.handler.session;

/**
 * SessionSpecProxyInvocationHandlerBase
 * 
 * Abstract base from which all Session Proxy InvocationHandlers
 * adhering to the EJB3 specification may extend
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionSpecProxyInvocationHandlerBase extends SessionProxyInvocationHandlerBase
      implements
         SessionSpecProxyInvocationHandler
{
   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Fully-qualified name of the class targeted either for injection
    * or casting to support getInvokedBusinessInterface.  May be
    * null to denote non-deterministic invocation
    */
   private String businessInterfaceType;

   // ------------------------------------------------------------------------------||
   // Constructors -----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param businessInterfaceType The possibly null businessInterfaceType
    *   marking this invocation hander as specific to a given
    *   EJB3 Business Interface
    */
   protected SessionSpecProxyInvocationHandlerBase(String businessInterfaceType)
   {
      super();
      this.setBusinessInterfaceType(businessInterfaceType);
   }

   // ------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public String getBusinessInterfaceType()
   {
      return businessInterfaceType;
   }

   protected void setBusinessInterfaceType(String businessInterfaceType)
   {
      this.businessInterfaceType = businessInterfaceType;
   }
}
