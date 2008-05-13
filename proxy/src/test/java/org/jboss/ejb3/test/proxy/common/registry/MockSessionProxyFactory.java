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
package org.jboss.ejb3.test.proxy.common.registry;

import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;

/**
 * MockSessionProxyFactory
 *
 * A Mock Session Proxy Factory for use in testing
 * the Proxy Factory Registry only
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MockSessionProxyFactory implements SessionProxyFactory
{

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.factory.session.SessionProxyFactory#createProxyBusiness()
    */
   public Object createProxyBusiness()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.factory.session.SessionProxyFactory#createProxyBusiness(java.lang.String)
    */
   public Object createProxyBusiness(String businessInterfaceName)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.factory.session.SessionProxyFactory#createProxyBusinessAndHome()
    */
   public Object createProxyBusinessAndHome()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.factory.session.SessionProxyFactory#createProxyHome()
    */
   public Object createProxyHome()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.factory.ProxyFactory#start()
    */
   public void start() throws Exception
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.factory.ProxyFactory#stop()
    */
   public void stop() throws Exception
   {
      // TODO Auto-generated method stub

   }

}
