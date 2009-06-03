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
package org.jboss.ejb3.core.test.ejbthree1558.unit;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Properties;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.jboss.aop.Domain;
import org.jboss.deployers.structure.spi.ClassLoaderFactory;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.common.deployers.spi.AttachmentNames;
import org.jboss.ejb3.core.resolvers.ScopedEJBReferenceResolver;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.common.MockEjb3Deployment;
import org.jboss.ejb3.core.test.ejbthree1558.TXNotSupportedMDB;
import org.jboss.ejb3.mdb.MDB;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.ejb3.test.common.MetaDataHelper;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class JBossMessageEndpointFactoryTestCase extends AbstractEJB3TestCase
{
   private static class MyMDB extends MDB
   {
      public MyMDB(String ejbName, Domain domain, ClassLoader cl, String beanClassName, Hashtable ctxProperties,
            Ejb3Deployment deployment, JBossMessageDrivenBeanMetaData beanMetaData) throws ClassNotFoundException
      {
         super(ejbName, domain, cl, beanClassName, ctxProperties, deployment, beanMetaData);
      }
      
      protected MessageEndpointFactory getMessageEndpointFactory()
      {
         return messageEndpointFactory;
      }
   }
   
   private static MyMDB container;
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      AbstractDeploymentUnit deploymentUnit = new AbstractDeploymentUnit(new AbstractDeploymentContext("ejbthree1558", ""));
      deploymentUnit.createClassLoader(new ClassLoaderFactory() {
         public ClassLoader createClassLoader(org.jboss.deployers.structure.spi.DeploymentUnit unit) throws Exception
         {
            return Thread.currentThread().getContextClassLoader();
         }

         public void removeClassLoader(org.jboss.deployers.structure.spi.DeploymentUnit unit) throws Exception
         {
         }
      });
      DeploymentUnit unit = new MockDeploymentUnit(deploymentUnit);
      Ejb3Deployment deployment = new MockEjb3Deployment(unit, deploymentUnit);

      deployment.setEJBReferenceResolver(new ScopedEJBReferenceResolver());
      
      Class<?> beanImplementationClasses[] = { TXNotSupportedMDB.class };
      JBossMetaData jbossMetaData = MetaDataHelper.getMetaDataFromBeanImplClasses(beanImplementationClasses);
      unit.addAttachment(AttachmentNames.PROCESSED_METADATA, jbossMetaData);
      
      String ejbName = "TXNotSupportedMDB";
      JBossMessageDrivenBeanMetaData beanMetaData = (JBossMessageDrivenBeanMetaData) jbossMetaData.getEnterpriseBean(ejbName);
      Domain domain = getDomain("Message Driven Bean");
      ClassLoader cl = deploymentUnit.getClassLoader();
      String beanClassName = beanMetaData.getEjbClass();
      Properties ctxProperties = null;
      
      container = new MyMDB(ejbName, domain, cl, beanClassName, ctxProperties, deployment, beanMetaData);
   }
   
   @Test
   public void testIsDeliveryTransacted() throws Exception
   {
      Method method = MessageListener.class.getDeclaredMethod("onMessage", Message.class);
      boolean isDeliveryTransacted = container.getMessageEndpointFactory().isDeliveryTransacted(method);
      assertFalse("TXNotSupportedMDB must not have delivery transacted", isDeliveryTransacted);
   }
}
