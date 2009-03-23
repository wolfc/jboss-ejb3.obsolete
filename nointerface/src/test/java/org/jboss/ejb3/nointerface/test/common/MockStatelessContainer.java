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
package org.jboss.ejb3.nointerface.test.common;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.ejb3.endpoint.Endpoint;

/**
 * MockStatelessContainer
 *
 * A mock stateless container, used for testing. The functionality is very
 * minimal. It just creates bean instances of the beans and uses those instances
 * to pass on the method invocation.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MockStatelessContainer implements Endpoint
{

   /**
    * The bean class represented by this container
    */
   private Class<?> beanClass;

   /**
    * Constructor
    * @param beanClass
    */
   public MockStatelessContainer(Class<?> beanClass)
   {
      this.beanClass = beanClass;
   }

   @Override
   public Object invoke(Serializable session, Class<?> invokedBusinessInterface, Method method, Object[] args)
         throws Throwable
   {

      // since this is for testing, creation of new bean instance on each invocation
      // should be manageable
      Object beanInstance = beanClass.newInstance();
      return method.invoke(beanInstance, args);
   }

}
