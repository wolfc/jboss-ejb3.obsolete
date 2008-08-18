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
package org.jboss.ejb3.metadata.test.mutable.unit;

import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.jboss.ejb3.annotation.impl.ResourceImpl;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.metadata.test.metadatacomplete.TwoLifecycleMethodsBean;
import org.jboss.ejb3.metadata.test.mutable.Dummy;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.junit.Test;

/**
 * To allow core to dynamically add annotations it must be mutable.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MutableMetaDataTestCase
{
   @Test
   public void test1() throws Exception
   {
      JBossEnterpriseBeanMetaData beanMetaData = null;
      
      // Bootstrap meta data bridge
      String canonicalObjectName = "Not important";
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      AnnotationRepositoryToMetaData repository = new AnnotationRepositoryToMetaData(TwoLifecycleMethodsBean.class, beanMetaData, canonicalObjectName, classLoader);
      
      Method method = Dummy.class.getDeclaredMethod("dummyMethod");
      Annotation annotation = new ResourceImpl();
      repository.addAnnotation(method, Resource.class, annotation);
      
      assertTrue("Failed to find annotation @Resource on " + method, repository.hasAnnotation(method, Resource.class));
   }
}
