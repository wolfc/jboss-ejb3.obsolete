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
package org.jboss.ejb3.core.test.ejbthree1506.unit;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateful;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3DescriptorHandler;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.common.MockEjb3Deployment;
import org.jboss.ejb3.core.test.ejbthree1506.TestBean;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.SessionType;
import org.junit.Test;

/**
 * If I have a SFSB in the descriptor I should be able to find
 * the corresponding annotation.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DescriptorAnnotationTestCase extends AbstractEJB3TestCase
{
   @Test
   public void test1() throws Exception
   {
      JBossMetaData metaData = new JBossMetaData();
      JBossEnterpriseBeansMetaData enterpriseBeans = new JBossEnterpriseBeansMetaData();
      metaData.setEnterpriseBeans(enterpriseBeans);
      metaData.setAssemblyDescriptor(new JBossAssemblyDescriptorMetaData());
      JBossSessionBeanMetaData sessionBeanMetaData = new JBossSessionBeanMetaData();
      sessionBeanMetaData.setEnterpriseBeansMetaData(enterpriseBeans);
      sessionBeanMetaData.setEjbClass(TestBean.class.getName());
      sessionBeanMetaData.setEjbName("TestBean");
      sessionBeanMetaData.setSessionType(SessionType.Stateful);
      enterpriseBeans.add(sessionBeanMetaData);
      
      MockEjb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit());
      Ejb3DescriptorHandler handler = new Ejb3DescriptorHandler(deployment, metaData);
      List<Container> containers = handler.getContainers(deployment, new HashMap<String, Container>());
      
      Stateful annotation = ((EJBContainer) containers.get(0)).getAnnotation(Stateful.class);
      assertNotNull("Can't find annotation @Stateful on the container", annotation);
   }
}
