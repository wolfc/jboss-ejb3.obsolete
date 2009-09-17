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
package org.jboss.ejb3.core.test.ejbthree1925.unit;

import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.Method;

import javax.interceptor.Interceptors;

import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.ejb3.aop.annotation.CachingAnnotationRepository;
import org.jboss.ejb3.core.test.ejbthree1582.GreeterBean;
import org.jboss.ejb3.core.test.ejbthree1925.DummyInterceptor;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AddAnnotationTestCase
{
   @Test
   public void testAddAnnotationWithString() throws Exception
   {
      Class<?> beanClass = GreeterBean.class;
      JBossEnterpriseBeanMetaData beanMetaData = null;      
      String canonicalObjectName = "GreeterBean";
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      
      AnnotationRepositoryToMetaData delegate = new AnnotationRepositoryToMetaData(beanClass, beanMetaData, canonicalObjectName, classLoader);
      AnnotationRepository repository = new CachingAnnotationRepository(delegate, classLoader);
      
      Method member = beanClass.getDeclaredMethod("sayHi", String.class);
      String annotation = "javax.interceptor.Interceptors";
      String value = "@javax.interceptor.Interceptors (value={" + DummyInterceptor.class.getName() + ".class})";
      repository.addAnnotation(member, annotation, value);
      
      Interceptors interceptors = (Interceptors) repository.resolveAnnotation(member, Interceptors.class);
      assertArrayEquals(new Class[] { DummyInterceptor.class }, interceptors.value());
   }
}
