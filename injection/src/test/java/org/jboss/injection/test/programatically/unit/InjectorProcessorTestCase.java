/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.injection.test.programatically.unit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.injection.Injector;
import org.jboss.injection.InjectorProcessor;
import org.jboss.injection.SimpleValueInjector;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.injection.lang.reflect.FieldBeanProperty;
import org.jboss.injection.test.programatically.Counter;
import org.jboss.injection.test.programatically.InjectedBean;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InjectorProcessorTestCase extends TestCase
{
   public void test1() throws Exception
   {
      Counter.reset();
      
      InjectedBean bean = new InjectedBean();
      
      BeanProperty property = new FieldBeanProperty(bean.getClass().getDeclaredField("value"));
      
      List<Injector> injectors = new ArrayList<Injector>();
      injectors.add(new SimpleValueInjector(property, "Hello world"));
      
      List<Method> postConstructs = new ArrayList<Method>();
      postConstructs.add(bean.getClass().getDeclaredMethod("postConstruct", new Class[]{}));
      
      InjectorProcessor.process(bean, injectors, postConstructs);
      
      bean.check();
      
      assertEquals(1, Counter.postConstructs);
   }
}
