/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3;

import java.util.HashMap;
import java.util.HashSet;

import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.ejb3.interceptor.InterceptorInfo;
import org.jboss.ejb3.interceptor.InterceptorInjector;
import org.jboss.logging.Logger;
import org.jboss.security.RealmMapping;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class BaseContext<T extends Container> implements BeanContext<T>
{
   protected static Logger log = Logger.getLogger(BaseContext.class);
   protected T container;
   protected Object bean;
   protected RealmMapping rm;
   protected SimpleMetaData metadata;
   
   protected HashMap<Class, Object> interceptorInstances;
   
   /**
    * Use with extreme caution, must not break getInstance post condition.
    * 
    * @param container
    */
   protected BaseContext(T container)
   {
      assert container != null : "container is null";
      
      this.container = container;
   }
   
   protected BaseContext(T container, Object bean)
   {
      this(container);
      
      assert bean != null : "bean is null";
      
      this.bean = bean;
   }
   
   public Object getId()
   {
      return null;
   }

   /**
    * Returns the enterprise bean, never returns null.
    */
   public Object getInstance()
   {
      return bean;
   }

   public T getContainer()
   {
      return container;
   }

   public SimpleMetaData getMetaData()
   {
      if (metadata == null) metadata = new SimpleMetaData();
      return metadata;
   }

   public void initialiseInterceptorInstances()
   {
      HashSet<InterceptorInfo> interceptors = ((EJBContainer)container).getApplicableInterceptors();
      if (interceptors != null && interceptors.size() > 0 && interceptorInstances == null)
      {
         HashMap<Class,InterceptorInjector> interceptorInjectors = ((EJBContainer)container).getInterceptorInjectors();
         interceptorInstances = new HashMap<Class, Object>();
         for (InterceptorInfo info : interceptors)
         {
            try
            {
               Object instance = info.getClazz().newInstance();
               interceptorInstances.put(info.getClazz(), instance);
               interceptorInjectors.get(info.getClazz()).inject(this, instance);
            }
            catch (Exception e)
            {
               log.warn("Interceptors must have a public noargs constructor: " + info.getClazz().getName());
            }
         }
      }
   }

   public Object[] getInterceptorInstances(InterceptorInfo[] interceptorInfos)
   {
      Object[] interceptors = new Object[interceptorInfos.length];
      int i = 0;
      for (InterceptorInfo info : interceptorInfos)
      {
         interceptors[i++] = interceptorInstances.get(info.getClazz());
      }
      return interceptors;
   }
   
   public Object getInvokedMethodKey()
   {
      return container;
   }
}
