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

import java.lang.reflect.Method;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;

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
public class MockStatelessContainer implements InvokableContext
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

   /**
    * @see InvokableContext#dynamicInvoke(Invocation)
    */
   public InvocationResponse dynamicInvoke(Invocation invocation) throws Throwable
   {
      // TODO : We don't do anything related to remoting right now in these tests.
      // Let's ignore this for now
      return null;
   }

   /**
    * @see InvokableContext#invoke(Object, SerializableMethod, Object[])
    */
   public Object invoke(Object proxy, SerializableMethod method, Object[] args) throws Throwable
   {
      // nothing fancy, just pass on the invocation to the bean class instance
      Method invokedMethod = method.toMethod();
      // since this is for testing, creation of new bean instance on each invocation
      // should be manageable
      Object beanInstance = beanClass.newInstance();
      return invokedMethod.invoke(beanInstance, args);
   }

   /**
    * @see InvokableContext#removeTarget(Object)
    */
   public void removeTarget(Object arg0) throws UnsupportedOperationException
   {
      // no-op for stateless container

   }

}
