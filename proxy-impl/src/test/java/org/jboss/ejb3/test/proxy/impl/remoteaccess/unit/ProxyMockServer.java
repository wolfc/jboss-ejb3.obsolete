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
package org.jboss.ejb3.test.proxy.impl.remoteaccess.unit;

import java.net.URL;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessBean;
import org.jboss.ejb3.testremote.server.JndiPropertiesToJnpserverPropertiesHackCl;
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

   private static final String FILENAME_EJB3_INTERCEPTORS_AOP = "ejb3-interceptors-aop.xml";

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public ProxyMockServer()
   {
      super();
   }

   /**
    * Overridden implementation to create the requisite EJB Containers for testing
    */
   @Override
   protected void initialize() throws Throwable
   {
      // Call super implementation
      super.initialize();

      // Switch up to the hacky CL so that "jndi.properties" is not loaded
      ClassLoader olderLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(new JndiPropertiesToJnpserverPropertiesHackCl());

         // Deploy *-beans.xml
         String testClassName = null;
         try
         {
            testClassName = this.getCommandLineArgs()[3];
         }
         catch (ArrayIndexOutOfBoundsException aioobe)
         {
            throw new RuntimeException("3rd argument to " + this.getClass().getName()
                  + " command launcher must be the FQN of the Test Class");
         }
         assert testClassName != null : "Test Class Name was not specified";
         Class<?> testClass = Class.forName(testClassName, false, Thread.currentThread().getContextClassLoader());
         this.getBootstrap().deploy(testClass);

         // Load ejb3-interceptors-aop.xml into AspectManager
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         URL url = cl.getResource(FILENAME_EJB3_INTERCEPTORS_AOP);
         if (url == null)
         {
            throw new RuntimeException("Could not load " + AspectManager.class.getSimpleName()
                  + " with definitions from XML as file " + FILENAME_EJB3_INTERCEPTORS_AOP + " could not be found");
         }
         AspectXmlLoader.deployXML(url);
      }
      finally
      {
         // Restore old CL
         Thread.currentThread().setContextClassLoader(olderLoader);
      }

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
