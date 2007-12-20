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
package org.jboss.ejb3.test.dependency.unit;

import org.jboss.ejb3.test.dependency.NoDependencies;
import org.jboss.ejb3.test.dependency.Stateless;
import org.jboss.ejb3.test.dependency.HasMBeanDependency;
import org.jboss.ejb3.test.dependency.HasXmlMBeanDependency;
import org.jboss.ejb3.test.dependency.Stateless2;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: DependencyUnitTestCase.java 61136 2007-03-06 09:24:20Z wolfc $
 */

public class DependencyUnitTestCase
        extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public DependencyUnitTestCase(String name)
   {

      super(name);

   }

   public void testNonDependencies() throws Exception
   {
      NoDependencies nada = (NoDependencies) getInitialContext().lookup("dependency-test/NoDependenciesBean/remote");
      nada.noop();
   }

   public void testDatasourceDependencies() throws Exception
   {
      Stateless test = null;
      test = (Stateless) getInitialContext().lookup("dependency-test/StatelessBean/remote");
      test.createCustomer();
   }

   public void testPUDependencies() throws Exception
   {
      try
      {
         super.deploy("ejbdepends.jar");
      }
      catch (Exception e)
      {
         // ignored
      }
      try
      {
         Stateless2 test = null;
         boolean exceptionThrown = false;
         try
         {
            test = (Stateless2) getInitialContext().lookup("Stateless2Bean/remote");
            test.createCustomer();
         }
         catch (Exception ex)
         {
            exceptionThrown = true;
         }
         assertTrue(exceptionThrown);

         super.deploy("yetanother.sar");
         try
         {
            test = (Stateless2) getInitialContext().lookup("Stateless2Bean/remote");
            test.createCustomer();
         }
         finally
         {
            super.undeploy("yetanother.sar");
         }
      }
      finally
      {
         super.undeploy("ejbdepends.jar");
      }

   }

   public void testDepends() throws Exception
   {
      boolean exceptionThrown = false;
      try
      {
         HasMBeanDependency dependency = (HasMBeanDependency) getInitialContext().lookup("dependency-test/HasMBeanDependencyBean/remote");
      }
      catch (Exception e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);
      exceptionThrown = false;
      try
      {
         HasXmlMBeanDependency dependency = (HasXmlMBeanDependency) getInitialContext().lookup("dependency-test/HasXmlMBeanDependencyBean/remote");
      }
      catch (Exception e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);
      exceptionThrown = false;
      super.deploy("dependedon.sar");
      try
      {
         try
         {
            HasMBeanDependency dependency = (HasMBeanDependency) getInitialContext().lookup("dependency-test/HasMBeanDependencyBean/remote");
         }
         catch (Exception e)
         {
            exceptionThrown = true;
         }
         assertTrue(exceptionThrown);

         // should pass now
         HasXmlMBeanDependency dependency = (HasXmlMBeanDependency) getInitialContext().lookup("dependency-test/HasXmlMBeanDependencyBean/remote");
         dependency.noop();

         super.deploy("anotherdependedon.sar");
         try
         {
            HasMBeanDependency dependency2 = (HasMBeanDependency) getInitialContext().lookup("dependency-test/HasMBeanDependencyBean/remote");
            dependency2.testNotNull();
         }
         finally
         {
            super.undeploy("anotherdependedon.sar");
         }
      }
      finally
      {
         super.undeploy("dependedon.sar");
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(DependencyUnitTestCase.class, "dependency-test.ear");
   }

}
