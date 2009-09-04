/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.interceptors.test.container;

import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.test.container.unit.LifecycleMethodInterceptorsInvocationTestCase;

/**
 * DummyBeanContext
 * 
 * Dummy beancontext used in {@link LifecycleMethodInterceptorsInvocationTestCase}. 
 * Does not do anything useful
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DummyBeanContext<T> implements BeanContext<T>
{
   
   /**
    * Instance
    */
   private T instance;
   
   /**
    * Returns the instance
    */
   public T getInstance()
   {
      return this.instance;
   }

   /**
    * Creates an returns a new instance of the <code>interceptorClasss</code>.
    * 
    * Note: The <code>interceptorClass</code> is expected to have a default
    * constructor.
    */
   public Object getInterceptor(Class<?> interceptorClass) throws IllegalArgumentException
   {
      try
      {
         return interceptorClass.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Sets the bean instance
    * @param beanInstance
    */
   public void setBeanInstance(T beanInstance)
   {
      this.instance = beanInstance;
   }

}
