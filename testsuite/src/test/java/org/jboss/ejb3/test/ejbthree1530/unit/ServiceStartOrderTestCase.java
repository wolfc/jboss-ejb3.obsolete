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
package org.jboss.ejb3.test.ejbthree1530.unit;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1530.Service2IsADependency;
import org.jboss.ejb3.test.ejbthree1530.Service1HasADependency;
import org.jboss.ejb3.test.ejbthree1530.StartLifecycleReporterBean;
import org.jboss.ejb3.test.ejbthree1530.StartLifecycleReporterRemoteBusiness;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * ServiceStartOrderTestCase
 * 
 * Test Cases to ensure @Service lifecycle start() 
 * is called in proper order
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ServiceStartOrderTestCase extends JBossTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   private static final Logger log = Logger.getLogger(ServiceStartOrderTestCase.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   public ServiceStartOrderTestCase(String name)
   {
      super(name);
   }

   // --------------------------------------------------------------------------------||
   // Suite --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   public static Test suite() throws Exception
   {
      /*
       * Get the deploy setup 
       */
      return getDeploySetup(ServiceStartOrderTestCase.class, "ejbthree1530.jar");
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /**
    * Tests that the dependent service is started before the service declaring
    * the dependency
    */
   public void testDependentServiceStartedFirst() throws Throwable
   {

      // Lookup the reporter
      Context context = this.getInitialContext();
      Object obj = context.lookup(StartLifecycleReporterBean.class.getSimpleName() + "/remote");
      StartLifecycleReporterRemoteBusiness reporter = (StartLifecycleReporterRemoteBusiness) obj;

      // Initialize the expected start order
      List<String> servicesStartedAsExpected = new ArrayList<String>();
      servicesStartedAsExpected.add(Service2IsADependency.OBJECT_NAME);
      servicesStartedAsExpected.add(Service1HasADependency.OBJECT_NAME);
      log.info("Services Start Expected Order: " + servicesStartedAsExpected);

      // Get the actual start order
      List<String> servicesStarted = reporter.getServicesStarted();
      log.info("Services Start Actual Order: " + servicesStarted);

      // Test all is as expected
      assertEquals("Wrong number of services reported as started", servicesStartedAsExpected.size(), servicesStarted
            .size());
      for (int i = 0; i < servicesStarted.size(); i++)
      {
         assertEquals("Service start order did not match expected", servicesStartedAsExpected.get(i), servicesStarted
               .get(i));
      }

   }

}
