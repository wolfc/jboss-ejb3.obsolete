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
package org.jboss.ejb3.security.test.runas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.annotation.security.RunAs;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.security.bridge.RunAsMetaDataBridge;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.SecurityIdentityMetaData;
import org.jboss.metadata.javaee.spec.EmptyMetaData;
import org.jboss.metadata.javaee.spec.RunAsMetaData;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RunAsMetaDataBridgeUnitTest
{
   private static ClassLoader classLoader = null; // laziness
   private static RunAsMetaDataBridge bridge;
   
   @BeforeClass
   public static void beforeClass()
   {
      bridge = new RunAsMetaDataBridge();
   }
   
   @Test
   public void testNoSecurityIdentity()
   {
      JBossEnterpriseBeanMetaData bean = new JBossSessionBeanMetaData();
      RunAs runAs = bridge.retrieveAnnotation(RunAs.class, bean, classLoader);
      assertNull(runAs);
   }
   
   @Test
   public void testOtherAnnotation()
   {
      RunAsMetaData runAsMetaData = new RunAsMetaData();
      runAsMetaData.setRoleName("test");
      SecurityIdentityMetaData securityIdentity = new SecurityIdentityMetaData();
      securityIdentity.setRunAs(runAsMetaData);
      JBossEnterpriseBeanMetaData bean = new JBossSessionBeanMetaData();
      bean.setSecurityIdentity(securityIdentity);
      SecurityDomain securityDomain = bridge.retrieveAnnotation(SecurityDomain.class, bean, classLoader);
      assertNull(securityDomain);
   }
   
   @Test
   public void testRunAs()
   {
      RunAsMetaData runAsMetaData = new RunAsMetaData();
      runAsMetaData.setRoleName("test");
      SecurityIdentityMetaData securityIdentity = new SecurityIdentityMetaData();
      securityIdentity.setRunAs(runAsMetaData);
      JBossEnterpriseBeanMetaData bean = new JBossSessionBeanMetaData();
      bean.setSecurityIdentity(securityIdentity);
      RunAs runAs = bridge.retrieveAnnotation(RunAs.class, bean, classLoader);
      assertEquals("test", runAs.value());
   }
   
   @Test
   public void testUseCallerIdentity()
   {
      SecurityIdentityMetaData securityIdentity = new SecurityIdentityMetaData();
      securityIdentity.setUseCallerIdentity(new EmptyMetaData());
      JBossEnterpriseBeanMetaData bean = new JBossSessionBeanMetaData();
      bean.setSecurityIdentity(securityIdentity);
      RunAs runAs = bridge.retrieveAnnotation(RunAs.class, bean, classLoader);
      assertNull(runAs);
   }
}
