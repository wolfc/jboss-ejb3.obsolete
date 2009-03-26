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
package org.jboss.ejb3.remoting2.test.common;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting.endpoint.RemotableEndpoint;
import org.jboss.ejb3.remoting.endpoint.RemotableEndpointReference;
import org.jboss.ejb3.remoting.spi.Remotable;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MockRemotable implements MockInterface, Remotable, RemotableEndpoint
{
   public MockInterface getBusinessObject()
   {
      Class<?> businessInterface = MockInterface.class;
      ClassLoader loader = getClassLoader();
      Class<?> interfaces[] = { businessInterface };
      Serializable session = null;
      InvocationHandler handler = new RemotableEndpointReference(this, session, businessInterface);
      MockInterface self = (MockInterface) Proxy.newProxyInstance(loader, interfaces, handler);
      
      String result = self.sayHi("self");
      assertEquals("Hi self", result);
      
      return self;
   }
   
   public ClassLoader getClassLoader()
   {
      return MockRemotable.class.getClassLoader();
   }
   
   public Serializable getId()
   {
      return "MockRemotableID";
   }
   
   public Remotable getRemotable()
   {
      return this;
   }
   
   public Object getTarget()
   {
      return this;
   }
   
   public String sayHi(String name)
   {
      return "Hi " + name;
   }

   public Object invoke(Serializable session, Map<String, Object> contextData, SerializableMethod method, Object[] args)
      throws Throwable
   {
      Method realMethod = method.toMethod(getClassLoader());
      try
      {
         return realMethod.invoke(this, args);
      }
      catch(IllegalArgumentException e)
      {
         throw new IllegalArgumentException("failed to invoke " + realMethod + " with " + Arrays.toString(args));
      }
      catch(InvocationTargetException e)
      {
         throw e.getCause();
      }
   }
}
