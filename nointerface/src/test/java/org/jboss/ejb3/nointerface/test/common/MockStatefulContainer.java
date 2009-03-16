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
import java.util.HashMap;
import java.util.Map;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.ejb3.proxy.spi.container.StatefulSessionFactory;
import org.jboss.ejb3.proxy.spi.intf.SessionProxy;

/**
 * MockStatefulContainer
 *
 * A mock stateful container, used for testing. The functionality is very
 * minimal. It just creates bean instances for a session and uses those instances
 * to pass on the method invocation.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MockStatefulContainer implements InvokableContext, StatefulSessionFactory

{
   /**
    * The bean class represented by this container
    */
   private Class<?> beanClass;

   /**
    * Maintain the sessions
    */
   private static Map<Serializable,Object> sessions = new HashMap<Serializable,Object>();

   /**
    * Each session is represented by an id
    */
   private static Long currentSessionId = new Long(0);

   /**
    * Constructor
    * @param beanClass
    */
   public MockStatefulContainer(Class<?> beanClass)
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
    * @see InvokableContext#invoke(SessionProxy, SerializableMethod, Object[])
    */
   public Object invoke(SessionProxy proxy, SerializableMethod method, Object[] args) throws Throwable
   {
      // get the bean instance using the target (=sessionId) from the SessionProxy
      Object target = proxy.getTarget();
      assert target instanceof Long : "Unexcepted target in SessionProxy. Expected a session id of type Long";
      Long sessionid = (Long) target;
      Object beanInstance = sessions.get(sessionid);
      Method invokedMethod = method.toMethod();
      return invokedMethod.invoke(beanInstance, args);

   }

   /**
    * Creates a session
    * @see StatefulSessionFactory#createSession()
    */
   public Serializable createSession()
   {
      synchronized (currentSessionId)
      {
         currentSessionId ++;
         try
         {
            sessions.put(currentSessionId, beanClass.newInstance());
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not create a session for bean " + beanClass, e);

         }
         return currentSessionId;
      }


   }

   /**
    * @see InvokableContext#removeTarget(Object)
    */
   public void removeTarget(Object target) throws UnsupportedOperationException
   {
      synchronized (sessions)
      {
         sessions.remove(target);
      }


   }

}
