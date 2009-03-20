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
package org.jboss.ejb3.annotation.finder.test.simple.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.AnnotatedElement;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.jboss.classloading.plugins.visitor.DefaultResourceContext;
import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.deployers.plugins.annotations.GenericAnnotationResourceVisitor;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.ejb3.annotation.finder.AnnotationFinderEnvironmentBridge;
import org.jboss.ejb3.annotation.finder.test.simple.MyStatefulBean;
import org.jboss.ejb3.annotation.finder.test.simple.MyStatelessBean;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleTestCase
{
   private static ResourceContext createResourceContext(String resourceName)
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      URL url = classLoader.getResource(resourceName);
      return new DefaultResourceContext(url, resourceName, classLoader);
   }
   
   private void runTest(JBoss50Creator creator, Collection<Class<?>> classes)
   {
      JBoss50MetaData metaData = creator.create(classes);
      
      assertEquals(2, metaData.getEnterpriseBeans().size());
      assertNotNull(metaData.getEnterpriseBean("MyStatefulBean"));
      assertNotNull(metaData.getEnterpriseBean("MyStatelessBean"));
      
      assertNotNull(metaData.getEnterpriseBean("MyStatefulBean").getEnvironmentEntryByName("org.jboss.ejb3.annotation.finder.test.simple.MyStatefulBean/s"));      
   }
   
   @Test
   public void test1()
   {
      Collection<Class<?>> classes = new HashSet<Class<?>>();
      classes.add(MyStatefulBean.class);
      classes.add(MyStatelessBean.class);
      
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      GenericAnnotationResourceVisitor visitor = new GenericAnnotationResourceVisitor(classLoader);
      for(Class<?> cls : classes)
      {
         visitor.visit(createResourceContext(cls.getName().replace('.', '/') + ".class"));
      }
      AnnotationEnvironment env = visitor.getEnv();
      AnnotationFinder<AnnotatedElement> finder = new AnnotationFinderEnvironmentBridge<AnnotatedElement>(env);
      JBoss50Creator creator = new JBoss50Creator(finder);
      runTest(creator, classes);
   }

   @Test
   public void testSpeedAnnotationEnvironment()
   {
      Collection<Class<?>> classes = new HashSet<Class<?>>();
      classes.add(MyStatefulBean.class);
      classes.add(MyStatelessBean.class);
      
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      GenericAnnotationResourceVisitor visitor = new GenericAnnotationResourceVisitor(classLoader);
      for(Class<?> cls : classes)
      {
         visitor.visit(createResourceContext(cls.getName().replace('.', '/') + ".class"));
      }
      AnnotationEnvironment env = visitor.getEnv();
      AnnotationFinder<AnnotatedElement> finder = new AnnotationFinderEnvironmentBridge<AnnotatedElement>(env);
      
      JBoss50Creator creator = new JBoss50Creator(finder);
      long end = System.currentTimeMillis() + 5000;
      long count = 0;
      while(System.currentTimeMillis() < end)
      {
         runTest(creator, classes);
         count++;
      }
      long delta = System.currentTimeMillis() - end + 5000;
      System.out.println("AnnotationFinderEnvironmentBridge does " + count + " passes in " + delta + " ms");
   }

   @Test
   public void testSpeedDefaultAnnotationFinder()
   {
      Collection<Class<?>> classes = new HashSet<Class<?>>();
      classes.add(MyStatefulBean.class);
      classes.add(MyStatelessBean.class);
      
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      
      JBoss50Creator creator = new JBoss50Creator(finder);
      long end = System.currentTimeMillis() + 5000;
      long count = 0;
      while(System.currentTimeMillis() < end)
      {
         runTest(creator, classes);
         count++;
      }
      long delta = System.currentTimeMillis() - end + 5000;
      System.out.println("DefaultAnnotationFinder does " + count + " passes in " + delta + " ms");
   }
}
