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
package org.jboss.ejb3.core.test.ejbthree1914.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;

import javax.ejb.ApplicationException;
import javax.ejb.Stateful;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3DescriptorHandler;
import org.jboss.ejb3.annotation.SerializedConcurrentAccess;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.common.MockEjb3Deployment;
import org.jboss.ejb3.core.test.ejbthree1506.TestBean;
import org.jboss.ejb3.core.test.ejbthree1914.DummyException;
import org.jboss.ejb3.interceptors.aop.ExtendedAdvisorHelper;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.ApplicationExceptionMetaData;
import org.jboss.metadata.ejb.spec.ApplicationExceptionsMetaData;
import org.jboss.metadata.ejb.spec.SessionType;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class OverrideAnnotationTestCase extends AbstractEJB3TestCase
{
   @Test
   public void test1() throws Exception
   {
      JBossMetaData metaData = new JBossMetaData();
      JBossEnterpriseBeansMetaData enterpriseBeans = new JBossEnterpriseBeansMetaData();
      metaData.setEnterpriseBeans(enterpriseBeans);
      JBossAssemblyDescriptorMetaData assemblyDescriptor = new JBossAssemblyDescriptorMetaData();
      ApplicationExceptionsMetaData applicationExceptions = new ApplicationExceptionsMetaData();
      ApplicationExceptionMetaData applicationExceptionMD = new ApplicationExceptionMetaData();
      applicationExceptionMD.setExceptionClass(DummyException.class.getName());
      applicationExceptions.add(applicationExceptionMD );
      assemblyDescriptor.setApplicationExceptions(applicationExceptions );
      metaData.setAssemblyDescriptor(assemblyDescriptor);
      JBossSessionBeanMetaData sessionBeanMetaData = new JBossSessionBeanMetaData();
      sessionBeanMetaData.setEnterpriseBeansMetaData(enterpriseBeans);
      sessionBeanMetaData.setEjbClass(TestBean.class.getName());
      sessionBeanMetaData.setEjbName("TestBean");
      sessionBeanMetaData.setSessionType(SessionType.Stateful);
      enterpriseBeans.add(sessionBeanMetaData);
      
      MockEjb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit());
      Ejb3DescriptorHandler handler = new Ejb3DescriptorHandler(deployment, metaData);
      List<Container> containers = handler.getContainers(deployment, new HashMap<String, Container>());
      
      EJBContainer container = (EJBContainer) containers.get(0);
      Stateful annotation = ((EJBContainer) containers.get(0)).getAnnotation(Stateful.class);
      assertNotNull("Can't find annotation @Stateful on the container", annotation);
      
      //ApplicationException ex = container.getAnnotation(ApplicationException.class, DummyException.class);
      ApplicationException ex = ExtendedAdvisorHelper.getExtendedAdvisor(container.getAdvisor()).resolveAnnotation(DummyException.class, ApplicationException.class);
      assertNotNull("Can't find annotation @ApplicationException in the meta data", ex);
   }

   @Test
   public void testDisableAnnotation() throws Exception
   {
      JBossMetaData metaData = new JBossMetaData();
      JBossEnterpriseBeansMetaData enterpriseBeans = new JBossEnterpriseBeansMetaData();
      metaData.setEnterpriseBeans(enterpriseBeans);
      JBossAssemblyDescriptorMetaData assemblyDescriptor = new JBossAssemblyDescriptorMetaData();
      metaData.setAssemblyDescriptor(assemblyDescriptor);
      JBossSessionBeanMetaData sessionBeanMetaData = new JBossSessionBeanMetaData();
      sessionBeanMetaData.setEnterpriseBeansMetaData(enterpriseBeans);
      sessionBeanMetaData.setEjbClass(TestBean.class.getName());
      sessionBeanMetaData.setEjbName("TestBean");
      sessionBeanMetaData.setSessionType(SessionType.Stateful);
      sessionBeanMetaData.setConcurrent(false);
      enterpriseBeans.add(sessionBeanMetaData);
      
      MockEjb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit());
      Ejb3DescriptorHandler handler = new Ejb3DescriptorHandler(deployment, metaData);
      List<Container> containers = handler.getContainers(deployment, new HashMap<String, Container>());
      
      EJBContainer container = (EJBContainer) containers.get(0);
      Stateful annotation = ((EJBContainer) containers.get(0)).getAnnotation(Stateful.class);
      assertNotNull("Can't find annotation @Stateful on the container", annotation);
      
      assertFalse("Stateful bean incorrectly has been marked with " + SerializedConcurrentAccess.class.getName()
            + " annotation", container.getAdvisor().hasAnnotation(SerializedConcurrentAccess.class.getName()));
   }
}
