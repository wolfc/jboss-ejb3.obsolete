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
package org.jboss.ejb3.proxy.factory.session.stateful;

import java.io.Serializable;

import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;

/**
 * StatefulSessionProxyFactory
 * 
 * Extension for SFSB Proxy Factories in which case methods defined
 * by SessionProxyFactory result in creation of a new Session, while
 * the overloaded methods provided here may create a Proxy for a specified
 * Session ID
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface StatefulSessionProxyFactory extends SessionProxyFactory
{
   /**
    * Create an EJB2.x Home Proxy
    * 
    * @param sessionId
    * @return
    */
   Object createProxyHome(Serializable sessionId);

   /**
    * Create an EJB3 Business proxy with no 
    * specific target business interface.  The 
    * returned proxy will implement all appropriate
    * business interfaces.  Additionally, if
    * the Home interface is bound alongside 
    * the Default (same JNDI Name), this 
    * Proxy will implement the Home interface as well. 
    * 
    * @param sessionId
    * @return
    */
   Object createProxyDefault(Serializable sessionId);

   /**
    * Create an EJB3 Business Proxy specific to the specified
    * target business interface name (expressed as 
    * a fully-qualified class name)
    * 
    * @param sessionId
    * @param businessInterfaceName
    * @return
    */
   Object createProxyBusiness(Serializable sessionId, String businessInterfaceName);

   /**
    * Create an EJB2.x Proxy 
    * 
    * @param sessionId
    * @return
    */
   Object createProxyEjb2x(Serializable sessionId);

}
