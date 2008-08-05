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
package org.jboss.ejb3.interceptors.test.common;

import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.interceptors.direct.AbstractDirectContainer;
import org.jboss.ejb3.interceptors.metadata.BeanInterceptorMetaDataBridge;
import org.jboss.ejb3.interceptors.metadata.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.interceptors.metadata.InterceptorMetaDataBridge;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;

/**
 * Emulates an EJB container to the best of its abilities.
 * 
 * Meta data bridges for interceptors are setup, so meta data can be processed.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ManagedObjectContainer<T> extends AbstractDirectContainer<T, ManagedObjectContainer<T>>
{
   /**
    * Create a new managed object container.
    * 
    * @param name           the name of the managed object (/ EJB)
    * @param domainName     the domain name to get interceptors from
    * @param beanClass      the managed object class
    * @param beanMetaData   the associated meta data
    */
   public ManagedObjectContainer(String name, String domainName, Class<? extends T> beanClass, JBossEnterpriseBeanMetaData beanMetaData)
   {
      super();
      
      assert name != null : "name is null";
      assert domainName != null : "domainName is null";
      assert beanClass != null : "beanClass is null";
      assert beanMetaData != null : "beanMetaData is null";
      
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      AnnotationRepositoryToMetaData annotations = new AnnotationRepositoryToMetaData(beanClass, beanMetaData, name, classLoader);
      List<MetaDataBridge<InterceptorMetaData>> interceptorBridges = new ArrayList<MetaDataBridge<InterceptorMetaData>>();
      interceptorBridges.add(new InterceptorMetaDataBridge());
      annotations.addComponentMetaDataLoaderFactory(new InterceptorComponentMetaDataLoaderFactory(interceptorBridges));
      annotations.addMetaDataBridge(new BeanInterceptorMetaDataBridge(beanClass, classLoader, beanMetaData));
      
      initializeAdvisor(name, getDomain(domainName), beanClass, annotations);
   }
}
