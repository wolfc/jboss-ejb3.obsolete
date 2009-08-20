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
package org.jboss.ejb3;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.interceptors.aop.LifecycleCallbacks;

/**
 * EnterpriseBeanContext
 *
 * Represents a {@link BeanContext}  for an {@link EJBContainer}
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class EnterpriseBeanContext<T extends EJBContainer> extends BaseContext<T>
{

   /**
    * Maintains a array of interceptors applicable to this bean context
    * for each of the lifecycle callbacks
    */
   protected transient Map<Class<? extends Annotation>, Interceptor[]> lifecycleCallbackInterceptors = new HashMap<Class<? extends Annotation>, Interceptor[]>();

   /**
    * 
    * @param container Container instance associated with this bean
    *                   context
    */
   protected EnterpriseBeanContext(T container)
   {
      super(container);
   }

   /**
    * 
    * @param container Container instance associated with this bean context
    * @param bean The instance of the bean implementation
    */
   protected EnterpriseBeanContext(T container, Object bean)
   {
      super(container, bean);
   }

   /**
    * Only for externalization use by subclass StatefulBeanContext; do not use elsewhere.
    *
    * @deprecated
    */
   protected EnterpriseBeanContext()
   {

   }

   /**
    * Returns the interceptor instances (which is a combination of our internal
    * AOP interceptors and bean developer defined {@link javax.interceptor.Interceptors}),
    * corresponding to the <code>lifecycleCallbackAnnotation</code>.
    * 
    * Internally caches the interceptor instances corresponding to each of the lifecycle 
    * callbacks, for this bean context 
    *  
    * @param lifecycleCallbackAnnotation Lifecycle callback annotations like {@link PrePassivate},
    *       {@link PostActivate}, {@link PreDestroy}, {@link PostConstruct} 
    *       
    * @return Returns an empty array if there are no interceptor instances associated with this
    *       bean context, for the <code>lifecycleCallbackAnnotation</code>. Else, returns the
    *       array of interceptors applicable to this bean context for the 
    *       <code>lifecycleCallbackAnnotation</code>
    */
   public Interceptor[] getLifecycleInterceptors(Class<? extends Annotation> lifecycleCallbackAnnotation)
   {
      Interceptor[] interceptors = this.lifecycleCallbackInterceptors.get(lifecycleCallbackAnnotation);
      // If null then we haven't yet initialized the lifecycle callback interceptors, since
      // we intentionally do a lazy initialization per lifecycle callback. The initialization
      // happens only once, when this method is called for the first time on this bean context,
      // for the specific lifecycle callback annotation.
      if (interceptors == null)
      {
         interceptors = this.createLifecycleInterceptors(lifecycleCallbackAnnotation);
         if (interceptors == null)
         {
            // No interceptors available, so create an empty chain and maintain in the map,
            // to avoid trying to init again the next time
            // this method is called for this specifc lifecycle callback
            interceptors = new Interceptor[0];
         }
         this.lifecycleCallbackInterceptors.put(lifecycleCallbackAnnotation, interceptors);
      }
      return this.lifecycleCallbackInterceptors.get(lifecycleCallbackAnnotation);
   }

   /**
    * Creates an AOP interceptor chain out of the lifecycle interceptors for the
    * <code>lifecycleCallbackAnnotation</code>
    * 
    * @param lifecycleCallbackAnnotation The lifecycle callback annotation
    * @return
    */
   protected Interceptor[] createLifecycleInterceptors(Class<? extends Annotation> lifecycleCallbackAnnotation)
   {
      // Get the lifecycle interceptor classes of the bean 
      List<Class<?>> lifecycleInterceptorClasses = this.getContainer().getLifecycleInterceptorClasses();
      Advisor advisor = this.getContainer().getAdvisor();
      Interceptor interceptors[];
      try
      {
         // Create a AOP interceptor chain out of the lifecycle interceptor classes
         interceptors = LifecycleCallbacks.createLifecycleCallbackInterceptors(advisor, lifecycleInterceptorClasses,
               this, lifecycleCallbackAnnotation);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create lifecycle interceptor for lifecycle "
               + lifecycleCallbackAnnotation + " on bean context " + this);
      }

      return interceptors;
   }

}
