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
package org.jboss.ejb3.core.test.ejbthree1358;

import java.util.Hashtable;

import javax.ejb.TimerService;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.ThreadLocalStack;
import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.statistics.InvocationStatistics;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MockContainer implements Container
{
   private ThreadLocalStack<BeanContext<?>> currentBean = new ThreadLocalStack<BeanContext<?>>();
   
   public void create() throws Exception
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public BeanContext<?> createBeanContext()
   {
      return new MockBeanContext(this);
   }

   public void destroy() throws Exception
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public Class<?> getBeanClass()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public DependencyPolicy getDependencyPolicy()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public String getEjbName()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public Context getEnc()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public InitialContext getInitialContext()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   @SuppressWarnings("unchecked")
   public Hashtable getInitialContextProperties()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public InvocationStatistics getInvokeStats()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public Object getMBean()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public String getName()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public ObjectName getObjectName()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public Pool getPool()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public <T> T getSecurityManager(Class<T> type)
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public TimerService getTimerService()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public TimerService getTimerService(Object key)
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public void injectBeanContext(BeanContext<?> beanContext)
   {
   }

   @SuppressWarnings("unchecked")
   public void invokeInit(Object bean, Class[] initTypes, Object[] initValues)
   {
   }

   @SuppressWarnings("unchecked")
   public void invokePostActivate(BeanContext beanContext)
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   @SuppressWarnings("unchecked")
   public void invokePostConstruct(BeanContext beanContext, Object[] params)
   {
   }

   @SuppressWarnings("unchecked")
   public void invokePreDestroy(BeanContext beanContext)
   {
   }

   @SuppressWarnings("unchecked")
   public void invokePrePassivate(BeanContext beanContext)
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public boolean isClustered()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public BeanContext<?> peekContext()
   {
      BeanContext<?> ctx = currentBean.get();
      assert ctx != null : "ctx is null";
      return ctx;
   }

   public BeanContext<?> popContext()
   {
      return currentBean.pop();
   }

   public void processMetadata()
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public void pushContext(BeanContext<?> ctx)
   {
      currentBean.push(ctx);
   }

   public void start() throws Exception
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

   public void stop() throws Exception
   {
      // TODO Auto-generated method stub
      throw new RuntimeException("NYI");
   }

}
