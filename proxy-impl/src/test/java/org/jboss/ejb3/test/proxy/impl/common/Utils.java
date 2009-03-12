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
package org.jboss.ejb3.test.proxy.impl.common;

import org.jboss.ejb3.test.common.MetaDataHelper;
import org.jboss.ejb3.test.proxy.impl.common.container.ServiceContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatelessContainer;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossServiceBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.BasicJndiBindingPolicy;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.JBossSessionPolicyDecorator;

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
      JBossSessionBeanMetaData beanMetaData = MetaDataHelper.getMetadataFromBeanImplClass(slsbImplementationClass);

      // Decorate Metadata
      beanMetaData = new JBossSessionPolicyDecorator(beanMetaData, new BasicJndiBindingPolicy());

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
      JBossSessionBeanMetaData beanMetaData = MetaDataHelper.getMetadataFromBeanImplClass(sfsbImplementationClass);

      // Decorate Metadata
      beanMetaData = new JBossSessionPolicyDecorator(beanMetaData, new BasicJndiBindingPolicy());

      // Make a Container
      StatefulContainer container = new StatefulContainer(beanMetaData, Thread.currentThread().getContextClassLoader());

      // Return
      return container;
   }

   /**
    * Creates and returns a @Service Container for the Service Implementation Class specified
    * 
    * @param sfsbImplementationClass
    * @return
    * @throws Throwable
    */
   public static ServiceContainer createService(Class<?> serviceImplementationClass) throws Throwable
   {
      // Get Metadata
      JBossSessionBeanMetaData beanMetaData = MetaDataHelper.getMetadataFromBeanImplClass(serviceImplementationClass);

      // Ensure @Service
      if (!beanMetaData.isService())
      {
         throw new RuntimeException("Specified implementation class for @Service was not a Service Bean: "
               + serviceImplementationClass.getName());
      }

      // Cast
      JBossServiceBeanMetaData smd = (JBossServiceBeanMetaData) beanMetaData;

      // Decorate Metadata
      beanMetaData = new JBossSessionPolicyDecorator(smd, new BasicJndiBindingPolicy());

      // Make a Container
      ServiceContainer container = new ServiceContainer(smd, Thread.currentThread().getContextClassLoader());

      // Return
      return container;
   }

}
