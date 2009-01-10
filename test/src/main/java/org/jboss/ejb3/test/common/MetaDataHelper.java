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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashSet;

import org.jboss.ejb3.common.metadata.MetadataUtil;
import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.process.chain.ProcessorChain;

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
    * specified implementation classes
    * 
    * @param beanImplClasses
    * @return
    */
   public static JBossMetaData getMetaDataFromBeanImplClasses(Collection<Class<?>> beanImplClasses)
   {
      return getMetaDataFromBeanImplClasses(beanImplClasses.toArray(new Class<?>[]
      {}));
   }

   /**
    * Mock the appropriate deployers and populate metadata for the EJB with the
    * specified implementation classes
    * 
    * @param beanImplClasses
    * @return
    */
   public static JBossMetaData getMetaDataFromBeanImplClasses(Class<?>[] beanImplClasses)
   {
      /*
       * Create the metadata
       */

      // emulate annotation deployer
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      Collection<Class<?>> classes = new HashSet<Class<?>>();
      for (Class<?> beanImplClass : beanImplClasses)
      {
         boolean unique = classes.add(beanImplClass);
         if (!unique)
         {
            log.warn("Specified class " + beanImplClass + " was not unique, skipping...");
         }
      }
      JBossMetaData metadata = new JBoss50Creator(finder).create(classes);

      /*
       * Mock the EjbMetadataJndiPolicyDecoratorDeployer
       */

      // Decorate w/ JNDI Policy
      log.debug("Decorating EJB3 EJBs in " + metadata + " with JNDI Policy");
      MetadataUtil.decorateEjbsWithJndiPolicy(metadata, AccessController
            .doPrivileged(new PrivilegedAction<ClassLoader>()
            {

               public ClassLoader run()
               {
                  return Thread.currentThread().getContextClassLoader();
               }

            }));

      /*
       * Mock the post-merge processing deployers
       */
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      ProcessorChain<JBossMetaData> chain = MetadataUtil.getPostMergeMetadataProcessorChain(cl);
      chain.process(metadata);

      // Return
      return metadata;
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
      // Create metadata
      JBossMetaData metadata = getMetaDataFromBeanImplClasses(new Class<?>[]
      {beanImplClass});

      // Get bean metadata
      String ejbName = beanImplClass.getSimpleName();
      JBossSessionBeanMetaData beanMetaData = (JBossSessionBeanMetaData) metadata.getEnterpriseBean(ejbName);
      assert beanMetaData != null : "Bean metadata for " + ejbName + " could not be found";

      // Return
      return beanMetaData;
   }

}
