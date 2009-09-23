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
package org.jboss.ejb3.test.changexml;

import java.net.URL;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.management.MBeanServer;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 61136 $
 */
public class TesterBean implements TesterRemote
{
   @EJB private SessionLocal local;

   public void runTest(URL url) throws Exception
   {
      if (local == null)
         throw new RuntimeException("local not injected");

      // No security domain should work
      if ("Ok".equals(local.doSomething("Ok")) == false)
         throw new RuntimeException("doSomething didn't work");

      // Add a security domain
      MBeanServer server = MBeanServerLocator.locateJBoss();
      DeploymentUnit unit = (DeploymentUnit) server.invoke(MainDeployerMBean.OBJECT_NAME, "getDeploymentUnit", new Object[] { url }, new String[] { URL.class.getName() });
      if (unit == null)
         throw new RuntimeException("Not deployed: " + url);
      JBossMetaData jbossMetaData = unit.getAttachment(JBossMetaData.class);
      if (jbossMetaData == null)
         throw new RuntimeException("No JBossMetaData");
      JBossEnterpriseBeanMetaData bean = jbossMetaData.getEnterpriseBean(SessionBean.class.getSimpleName());
      if (bean == null)
         throw new RuntimeException("No bean");
      bean.setSecurityDomain("changexml-security-domain");

      // Now this shouldn't work
      try
      {
         local.doSomething("bad");
      }
      catch (EJBAccessException expected)
      {
         return;
      }
      throw new ShouldNotBeHereException("Should not be here!");
   }
}
