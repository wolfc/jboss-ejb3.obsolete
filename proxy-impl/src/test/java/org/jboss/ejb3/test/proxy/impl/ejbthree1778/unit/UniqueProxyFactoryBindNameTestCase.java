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
package org.jboss.ejb3.test.proxy.impl.ejbthree1778.unit;

import junit.framework.TestCase;

import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiStatelessSessionRegistrar;
import org.jboss.ejb3.proxy.impl.objectfactory.session.stateless.StatelessSessionProxyObjectFactory;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessBean;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.DeploymentSummary;
import org.junit.Test;

/**
 * UniqueProxyFactoryBindNameTestCase
 * 
 * Test Cases to ensure that Proxy Factory target bind names are unique
 * 
 * EJBTHREE-1778
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class UniqueProxyFactoryBindNameTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(UniqueProxyFactoryBindNameTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that the EAR scoping is taken into account when creating unique
    * Proxy Factory registration names
    *  
    * @throws Throwable
    */
   @Test
   public void testSameEjbNameInDifferentApplications() throws Throwable
   {
      // Initialize
      String commonJndiName = "commonJndiName";

      // Define the common implementation class
      Class<?> ejbImplClass = MyStatelessBean.class;

      // Create Containers
      JBossSessionBeanMetaData mdWithApplicationName1 = Utils.createSlsb(ejbImplClass).getMetaData();
      JBossSessionBeanMetaData mdWithApplicationName2 = Utils.createSlsb(ejbImplClass).getMetaData();
      JBossSessionBeanMetaData mdWithoutApplicationName = Utils.createSlsb(ejbImplClass).getMetaData();

      // Set application name manually on 2 containers
      mdWithApplicationName1.getEnterpriseBeansMetaData().getEjbJarMetaData().setDeploymentSummary(
            new DeploymentSummary());
      mdWithApplicationName1.getEnterpriseBeansMetaData().getEjbJarMetaData().getDeploymentSummary()
            .setDeploymentScopeBaseName("EAR1");
      mdWithApplicationName2.getEnterpriseBeansMetaData().getEjbJarMetaData().setDeploymentSummary(
            new DeploymentSummary());
      mdWithApplicationName2.getEnterpriseBeansMetaData().getEjbJarMetaData().getDeploymentSummary()
            .setDeploymentScopeBaseName("EAR2");

      // Create a registrar
      JndiStatelessSessionRegistrar reg = new JndiStatelessSessionRegistrar(StatelessSessionProxyObjectFactory.class
            .getName());

      // Get the target ProxyFactory Names
      String regNameWithApplicationName1 = reg.getProxyFactoryRegistryKey(commonJndiName, mdWithApplicationName1, true);
      String regNameWithApplicationName2 = reg.getProxyFactoryRegistryKey(commonJndiName, mdWithApplicationName2, true);
      String regNameWithoutApplicationName = reg.getProxyFactoryRegistryKey(commonJndiName, mdWithoutApplicationName,
            true);

      // Log
      log.info("regNameWithApplicationName1: " + regNameWithApplicationName1);
      log.info("regNameWithApplicationName2: " + regNameWithApplicationName2);
      log.info("regNameWithoutApplicationName: " + regNameWithoutApplicationName);

      // Test
      TestCase.assertTrue("Proxy Factory Registration Names must be different for EJBs in different EARs",
            !regNameWithApplicationName1.equals(regNameWithApplicationName2));
      TestCase.assertTrue(
            "Proxy Factory Registration Names must be different for EJBs in an EAR vs. outside of an EAR",
            !regNameWithApplicationName1.equals(regNameWithoutApplicationName));

   }

}
