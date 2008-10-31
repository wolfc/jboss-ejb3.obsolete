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
package org.jboss.ejb3.proxy;

/**
 * Contract for a generic EJB3 Proxy Factory
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision$
 */
public interface ProxyFactory
{
   /**
    * Create an EJB 2.1 Home Proxy
    * 
    * @return
    */
   public Object createHomeProxy();

   /**
    * Create an EJB3 Business proxy with no 
    * specific target business interface
    * 
    * @return
    */
   public Object createProxyBusiness();

   /**
    * Lifecycle Start 
    * 
    * @throws Exception
    */
   public void start() throws Exception;

   /**
    * Lifecycle Stop 
    * 
    * @throws Exception
    */
   public void stop() throws Exception;
}
