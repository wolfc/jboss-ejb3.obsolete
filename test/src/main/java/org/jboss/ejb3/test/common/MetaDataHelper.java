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
package org.jboss.ejb3.test.common;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.creator.ejb.EjbJar30Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionPolicyDecorator;
import org.jboss.metadata.ejb.jboss.RemoteBindingMetaData;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;
import org.jboss.metadata.ejb.spec.EjbJar30MetaData;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MetaDataHelper
{
   private static final Logger log = Logger.getLogger(MetaDataHelper.class);

   /**
    * Create an empty JBossSessionBeanMetaData to satisfy BeanInterceptorMetaDataBridge.
    * 
    * @return
    */
   public static JBossSessionBeanMetaData createMockBeanMetaData()
   {
      JBossMetaData metaData = new JBossMetaData();
      JBossEnterpriseBeansMetaData enterpriseBeans = new JBossEnterpriseBeansMetaData();
      metaData.setEnterpriseBeans(enterpriseBeans);
      metaData.setAssemblyDescriptor(new JBossAssemblyDescriptorMetaData());
      JBossSessionBeanMetaData sessionBeanMetaData = new JBossSessionBeanMetaData();
      sessionBeanMetaData.setEnterpriseBeansMetaData(enterpriseBeans);
      return sessionBeanMetaData;
   }

   /**
    * Mock the appropriate deployers and populate metadata for the EJB with the
    * specified implementation class
    * 
    * @param beanImplClass
    * @return
    */
   public static JBossSessionBeanMetaData getMetadataFromBeanImplClass(Class<?> beanImplClass)
   {
      //TODO
      /*
       * EjbJar30Creator will be replaced by JBossSessionBeanMetaDataCreator
       * http://www.jboss.com/index.html?module=bb&op=viewtopic&t=136416
       */

      // emulate annotation deployer
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      Collection<Class<?>> classes = new HashSet<Class<?>>();
      classes.add(beanImplClass);
      EjbJar30MetaData metaData = new EjbJar30Creator(finder).create(classes);

      // emulate merge deployer
      JBossMetaData mergedMetaData = new JBossMetaData();
      mergedMetaData.merge(null, metaData);

      // Get delegate
      JBossSessionBeanMetaData beanMetaDataDelegate = (JBossSessionBeanMetaData) mergedMetaData
            .getEnterpriseBean(beanImplClass.getSimpleName());

      //TODO When JBossSessionBeanMetaDataCreator is done, remove this
      // Add Remote Binding manually
      if (beanMetaDataDelegate.getBusinessRemotes() != null)
      {
         List<RemoteBindingMetaData> remoteBindings = new ArrayList<RemoteBindingMetaData>();
         RemoteBindingMetaData remoteBinding = new RemoteBindingMetaData();
         remoteBinding.setClientBindUrl("socket://localhost:3874"); //TODO Hardcoded bad
         remoteBindings.add(remoteBinding);
         beanMetaDataDelegate.setRemoteBindings(remoteBindings);
      }

      // Use a Session JNDI Binding Policy for the metadata
      JBossSessionPolicyDecorator beanMetaData = new JBossSessionPolicyDecorator(beanMetaDataDelegate);

      /*
       * Log Out JNDI Names
       */

      // Business Remotes
      BusinessRemotesMetaData businessRemotes = beanMetaData.getBusinessRemotes();
      if (businessRemotes != null)
      {
         log.info("Business Remote JNDI Name: " + beanMetaData.determineJndiName()); // [beanName]/remote
         for (String businessInterface : beanMetaData.getBusinessRemotes())
         {
            log.info("Business Remote JNDI Name for " + businessInterface + ": "
                  + beanMetaData.determineResolvedJndiName(businessInterface));
         }
      }

      // Business Locals
      BusinessLocalsMetaData businessLocals = beanMetaData.getBusinessLocals();
      if (businessLocals != null)
      {
         log.info("Local JNDI Name: " + beanMetaData.determineLocalJndiName()); // [beanName]/local
         for (String businessInterface : beanMetaData.getBusinessLocals())
         {
            log.info("Business Local JNDI Name for " + businessInterface + ": "
                  + beanMetaData.determineResolvedJndiName(businessInterface));
         }
      }

      // Local Home
      String localHome = beanMetaData.getLocalHome();
      if (localHome != null && !localHome.trim().equals(""))
      {
         log.info("Local Home JNDI Name: " + beanMetaData.determineResolvedJndiName(localHome));
      }

      // Home
      String home = beanMetaData.getHome();
      if (home != null && !home.trim().equals(""))
      {
         log.info("Home JNDI Name: " + beanMetaData.determineResolvedJndiName(home));
      }

      return beanMetaData;
   }
}
