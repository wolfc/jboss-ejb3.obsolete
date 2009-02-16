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
package org.jboss.ejb3.nointerface.test.viewcreator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.ejb3.nointerface.test.viewcreator.unit.NoInterfaceEJBViewCreatorTestCase;
import org.jboss.logging.Logger;

/**
 * MethodInvocationTrackingContainer
 *
 * Mock container which just tracks/maintains the methods which were
 * invoked on the bean through this container. This MethodInvocationTrackingContainer will be
 * used by the {@link NoInterfaceEJBViewCreatorTestCase} to create a no-interface view out of a
 * bean and associate this container with the bean. Any invocations on the public methods of the
 * no-interface view of the bean in, the {@link NoInterfaceEJBViewCreatorTestCase} test case,
 * will be routed through this MethodInvocationTrackingContainer
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MethodInvocationTrackingContainer implements InvocationHandler
{

   /**
    * Logger
    */
   private Logger logger = Logger.getLogger(MethodInvocationTrackingContainer.class);

   /**
    * The bean class to which this container corresponds to
    */
   private Class<?> beanClass;


   private List<String> invokedMethods;

   /**
    * Constructor
    * @param targetBean The bean class to which this container corresponds to
    */
   public MethodInvocationTrackingContainer(Class<?> targetBean)
   {
      this.beanClass = targetBean;
      invokedMethods = new ArrayList<String>();
   }

   /**
    * Maintains the method name invoked, in its internal list and then forwards
    * the method invocation to the instance of the bean class
    *
    *
    * @see InvocationHandler#invoke(Object, Method, Object[])
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      logger.debug("Tracking method " + method.getName() + " for bean " + beanClass.getName());
      // add to internal map for tracking
      this.invokedMethods.add(method.getName());

      Object target = beanClass.newInstance();
      return method.invoke(target, args);
   }

   /**
    * Clears the methods that were tracked
    */
   public void resetTracking()
   {
      this.invokedMethods.clear();
   }

   /**
    * Return the tracked method names
    * @return
    */
   public List<String> getTrackedMethodNames()
   {
      return Collections.unmodifiableList(this.invokedMethods);
   }

}
