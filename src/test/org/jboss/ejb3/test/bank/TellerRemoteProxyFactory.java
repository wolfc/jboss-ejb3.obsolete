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
package org.jboss.ejb3.test.bank;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision: 67628 $
 */
public class TellerRemoteProxyFactory extends org.jboss.ejb3.stateless.StatelessRemoteProxyFactory
{
   private static final Logger log = Logger.getLogger(TellerRemoteProxyFactory.class);
   
   public TellerRemoteProxyFactory(SessionContainer container, RemoteBinding binding)
   {
      super(container, binding);
   }

   protected Class[] getInterfaces()
   {
      Class[] remoteInterfaces = super.getInterfaces();

      Class[] interfaces = new Class[remoteInterfaces.length + 1];

      System.arraycopy(remoteInterfaces, 0, interfaces, 0, remoteInterfaces.length);
      interfaces[remoteInterfaces.length] = org.jboss.ejb3.test.bank.ProxyFactoryInterface.class;

      return interfaces;
   }
}
