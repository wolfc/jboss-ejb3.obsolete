/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.proxy.remoteaccess.unit;

import org.jboss.ejb3.test.proxy.common.Utils;
import org.jboss.ejb3.test.proxy.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulBean;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessBean;
import org.jboss.ejb3.testremote.server.MockServer;
import org.jboss.logging.Logger;

/**
 * ProxyMockServer
 * 
 * Mock Remotable Server used in proxy testing
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyMockServer extends MockServer
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ProxyMockServer.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public ProxyMockServer(final Class<?> testClass, final String serverHost, final int port)
   {
      super(testClass, serverHost, port);
   }

   /**
    * Overridden implementation to create the requisite EJB Containers for testing
    */
   @Override
   protected void initialize() throws Throwable
   {
      // Call super implementation
      super.initialize();

      // Create a SLSB Container
      final StatelessContainer slsbContainer = Utils.createSlsb(MyStatelessBean.class);
      log.info("Created SLSB Container: " + slsbContainer.getName());

      // Create a SFSB Container
      final StatefulContainer sfsbContainer = Utils.createSfsb(MyStatefulBean.class);
      log.info("Created SFSB Container: " + sfsbContainer.getName());

      // Install into MC
      this.getBootstrap().installInstance(slsbContainer.getName(), slsbContainer);
      this.getBootstrap().installInstance(sfsbContainer.getName(), sfsbContainer);

   }

}
