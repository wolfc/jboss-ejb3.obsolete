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
package org.jboss.ejb3.core.test.ejbthree1150.unit;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.InitialContext;
import javax.xml.ws.WebServiceContext;

import org.jboss.aop.Domain;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.common.MockEjb3Deployment;
import org.jboss.ejb3.core.test.ejbthree1150.AnnotatedWebServiceContextInjectedBean;
import org.jboss.ejb3.core.test.ejbthree1150.OverrideWebServiceContextInjectedBean;
import org.jboss.ejb3.core.test.ejbthree1150.WebServiceContextInjected;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.ejb3.test.common.MetaDataHelper;
import org.jboss.metadata.ejb.jboss.JBossEnvironmentRefsGroupMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class WebServiceContextInjectionTestCase extends AbstractEJB3TestCase
{
   @Test
   public void testCheckWebServiceContext() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class<?> beanClass = AnnotatedWebServiceContextInjectedBean.class;
      String beanClassname = beanClass.getName();
      String ejbName = beanClass.getSimpleName();
      Domain domain = getDomain("Stateless Bean");
      Hashtable<?,?> ctxProperties = null;
      Ejb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit());
      JBossSessionBeanMetaData beanMetaData = MetaDataHelper.getMetadataFromBeanImplClass(beanClass);
      StatelessContainer container = new StatelessContainer(cl, beanClassname, ejbName, domain, ctxProperties, deployment, beanMetaData);
      
      // TODO: wickedness
      container.instantiated();
      
      container.processMetadata();
      
      // Register the Container in ObjectStore (MC)
      String containerName = container.getObjectName().getCanonicalName();
      Ejb3RegistrarLocator.locateRegistrar().bind(containerName, container);

      InitialContext ctx = new InitialContext();
      System.out.println("ctx = " + ctx);
      //System.out.println("  " + container.getInitialContext().list("MyStatelessBean").next());
      WebServiceContextInjected bean = (WebServiceContextInjected) ctx.lookup("AnnotatedWebServiceContextInjectedBean/local");
      
      bean.checkWebServiceContext();
      
      Ejb3RegistrarLocator.locateRegistrar().unbind(containerName);
   }

   @Test
   public void testCheckWebServiceContextWithOverride() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class<?> beanClass = OverrideWebServiceContextInjectedBean.class;
      String beanClassname = beanClass.getName();
      String ejbName = beanClass.getSimpleName();
      Domain domain = getDomain("Stateless Bean");
      Hashtable<?,?> ctxProperties = null;
      Ejb3Deployment deployment = new MockEjb3Deployment(new MockDeploymentUnit());
      JBossSessionBeanMetaData beanMetaData = MetaDataHelper.getMetadataFromBeanImplClass(beanClass);
      
      // add an override
      
      ResourceInjectionTargetMetaData injectionTarget = new ResourceInjectionTargetMetaData();
      injectionTarget.setInjectionTargetClass(OverrideWebServiceContextInjectedBean.class.getName());
      injectionTarget.setInjectionTargetName("wsContext");
      
      Set<ResourceInjectionTargetMetaData> injectionTargets = new HashSet<ResourceInjectionTargetMetaData>();
      injectionTargets.add(injectionTarget);
      
      ResourceEnvironmentReferenceMetaData resEnvRef = new ResourceEnvironmentReferenceMetaData();
      resEnvRef.setName("wsContext");
      resEnvRef.setType(WebServiceContext.class.getName());
      resEnvRef.setInjectionTargets(injectionTargets);
      
      if(beanMetaData.getJndiEnvironmentRefsGroup() == null)
         beanMetaData.setJndiEnvironmentRefsGroup(new JBossEnvironmentRefsGroupMetaData());
      if(beanMetaData.getResourceEnvironmentReferences() == null)
         ((JBossEnvironmentRefsGroupMetaData) beanMetaData.getJndiEnvironmentRefsGroup()).setResourceEnvironmentReferences(new ResourceEnvironmentReferencesMetaData());
      beanMetaData.getResourceEnvironmentReferences().add(resEnvRef);
      //
      
      StatelessContainer container = new StatelessContainer(cl, beanClassname, ejbName, domain, ctxProperties, deployment, beanMetaData);
      
      // TODO: wickedness
      container.instantiated();
      
      container.processMetadata();
      
      // Register the Container in ObjectStore (MC)
      String containerName = container.getObjectName().getCanonicalName();
      Ejb3RegistrarLocator.locateRegistrar().bind(containerName, container);

      InitialContext ctx = new InitialContext();
      System.out.println("ctx = " + ctx);
      //System.out.println("  " + container.getInitialContext().list("MyStatelessBean").next());
      WebServiceContextInjected bean = (WebServiceContextInjected) ctx.lookup("OverrideWebServiceContextInjectedBean/local");
      
      bean.checkWebServiceContext();
      
      getBootstrap().getKernel().getController().uninstall(containerName);
   }
}
