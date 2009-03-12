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
package org.jboss.ejb3.test.proxy.impl.common;

import java.net.URL;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.logging.Logger;

/**
 * SessionTestCaseBase
 * 
 * Operations common to Proxy Session Test Cases
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SessionTestCaseBase.class);

   protected static EmbeddedTestMcBootstrap bootstrap;

   protected static final String FILENAME_EJB3_INTERCEPTORS_AOP = "ejb3-interceptors-aop.xml";

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public static void setUpBeforeClass() throws Throwable
   {
      bootstrap = new EmbeddedTestMcBootstrap();
      bootstrap.run();

      // Bind the Registrar
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));

      // Load ejb3-interceptors-aop.xml into AspectManager
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      String ejb3InterceptorsAopFilename = getEjb3InterceptorsAopFilename();
      URL url = cl.getResource(ejb3InterceptorsAopFilename);
      if (url == null)
      {
         throw new RuntimeException("Could not load " + AspectManager.class.getSimpleName()
               + " with definitions from XML as file " + ejb3InterceptorsAopFilename + " could not be found");
      }
      AspectXmlLoader.deployXML(url);
      log.info("Deployed AOP XML: " + ejb3InterceptorsAopFilename);
   }

   // --------------------------------------------------------------------------------||
   // Accessors ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected static String getEjb3InterceptorsAopFilename()
   {
      return SessionTestCaseBase.FILENAME_EJB3_INTERCEPTORS_AOP;
   }

}
