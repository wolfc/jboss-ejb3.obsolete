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
package org.jboss.ejb3.test.proxy.common;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashSet;

import org.jboss.ejb3.test.proxy.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.common.container.StatelessContainer;
import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.creator.ejb.EjbJar30Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionPolicyDecorator;
import org.jboss.metadata.ejb.spec.EjbJar30MetaData;

/**
 * Utils
 * 
 * Utilities for Generating Test EJBs
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Utils
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(Utils.class);

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates and returns a SLSB Container for the SLSB Implementation Class specified
    * 
    * @param slsbImplementationClass
    * @return
    * @throws Throwable
    */
   public static StatelessContainer createSlsb(Class<?> slsbImplementationClass) throws Throwable
   {
      // Get Metadata
      JBossSessionBeanMetaData beanMetaData = Utils.getMetadataFromBeanImplClass(slsbImplementationClass);

      // Make a Container
      StatelessContainer container = new StatelessContainer(beanMetaData, Thread.currentThread()
            .getContextClassLoader());

      // Return
      return container;
   }

   /**
    * Creates and returns a SLSB Container for the SLSB Implementation Class specified
    * 
    * @param sfsbImplementationClass
    * @return
    * @throws Throwable
    */
   public static StatefulContainer createSfsb(Class<?> sfsbImplementationClass) throws Throwable
   {
      // Get Metadata
      JBossSessionBeanMetaData beanMetaData = Utils.getMetadataFromBeanImplClass(sfsbImplementationClass);

      // Make a Container
      StatefulContainer container = new StatefulContainer(beanMetaData, Thread.currentThread().getContextClassLoader());

      // Return
      return container;
   }

   /**
    * Mock the appropriate deployers and populate metadata for the EJB with the
    * specified implementation class
    * 
    * @param beanImplClass
    * @return
    */
   private static JBossSessionBeanMetaData getMetadataFromBeanImplClass(Class<?> beanImplClass)
   {
      // emulate annotation deployer
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      Collection<Class<?>> classes = new HashSet<Class<?>>();
      classes.add(beanImplClass);
      EjbJar30MetaData metaData = new EjbJar30Creator(finder).create(classes);

      // emulate merge deployer
      JBossMetaData mergedMetaData = new JBossMetaData();
      mergedMetaData.merge(null, metaData);

      JBossSessionBeanMetaData beanMetaDataDelegate = (JBossSessionBeanMetaData) mergedMetaData
            .getEnterpriseBean(beanImplClass.getSimpleName());

      // Use a Session JNDI Binding Policy for the metadata
      JBossSessionPolicyDecorator beanMetaData = new JBossSessionPolicyDecorator(beanMetaDataDelegate);

      // Log out JNDI Names
      log.info("Business Remote JNDI Name: " + beanMetaData.determineJndiName()); // MyStatefulBean/remote
      for (String businessInterface : beanMetaData.getBusinessRemotes())
      {
         log.info("Business Remote JNDI Name for " + businessInterface + ": "
               + beanMetaData.determineResolvedJndiName(businessInterface));
      }
      log.info("Local JNDI Name: " + beanMetaData.determineLocalJndiName()); // MyStatefulBean/local
      for (String businessInterface : beanMetaData.getBusinessLocals())
      {
         log.info("Business Local JNDI Name for " + businessInterface + ": "
               + beanMetaData.determineResolvedJndiName(businessInterface));
      }
      log.info("Local Home JNDI Name: " + beanMetaData.determineResolvedJndiName(beanMetaData.getLocalHome()));
      log.info("Home JNDI Name: " + beanMetaData.determineResolvedJndiName(beanMetaData.getHome()));

      return beanMetaData;
   }

}
