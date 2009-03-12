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
package org.jboss.ejb3.singleton;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.ejb.Handle;
import javax.ejb.TimerService;

import org.jboss.aop.Domain;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SingletonContainer extends SessionSpecContainer
{
   private BeanContext<SingletonContainer> instance;
   
   public SingletonContainer(ClassLoader cl, String beanClassName, String ejbName, Domain domain,
         Hashtable ctxProperties, Ejb3Deployment deployment, JBossSessionBeanMetaData beanMetaData)
      throws ClassNotFoundException
   {
      super(cl, beanClassName, ejbName, domain, ctxProperties, deployment, beanMetaData);
   }

   @Override
   public Serializable createSession(Class<?>[] initParameterTypes, Object[] initParameterValues)
   {
      assert initParameterTypes == null || initParameterTypes.length == 0;
      assert initParameterValues == null || initParameterValues.length == 0;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.session.SessionContainer#dynamicInvoke(org.jboss.aop.joinpoint.Invocation)
    */
   @Override
   public InvocationResponse dynamicInvoke(Invocation invocation) throws Throwable
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   protected synchronized BeanContext<SingletonContainer> getInstance()
   {
      if(instance != null)
         return instance;
      instance = createBeanContext();
      return instance;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.session.SessionContainer#getJndiRegistrarBindName()
    */
   @Override
   protected String getJndiRegistrarBindName()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.session.SessionContainer#localHomeInvoke(java.lang.reflect.Method, java.lang.Object[])
    */
   @Override
   public Object localHomeInvoke(Method method, Object[] args) throws Throwable
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.session.SessionContainer#localInvoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    */
   @Override
   public Object localInvoke(Object id, Method method, Object[] args) throws Throwable
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.session.SessionContainer#removeHandle(javax.ejb.Handle)
    */
   @Override
   protected void removeHandle(Handle handle) throws Exception
   {
      // TODO Auto-generated method stub
      //
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.EJBContainer#createBeanContext()
    */
   @Override
   public SingletonBeanContext createBeanContext()
   {
      SingletonBeanContext ctx = new SingletonBeanContext(this, construct());
      pushContext(ctx);
      try
      {
         injectBeanContext(ctx);

         ctx.initialiseInterceptorInstances();
      }
      finally
      {
         popContext();
      }
      
      invokePostConstruct(ctx);
      
      return ctx;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.Container#getMBean()
    */
   public Object getMBean()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.Container#getTimerService()
    */
   public TimerService getTimerService()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.Container#getTimerService(java.lang.Object)
    */
   public TimerService getTimerService(Object key)
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

}
