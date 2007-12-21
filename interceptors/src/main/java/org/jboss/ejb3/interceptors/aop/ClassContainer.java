/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptors.aop;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.jboss.aop.AspectManager;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructorInvocation;
import org.jboss.logging.Logger;

/**
 * A hack to get to a proper class container.
 * 
 * @deprecated use DomainClassLoader
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Deprecated
public class ClassContainer extends ClassAdvisor
{
   private static final Logger log = Logger.getLogger(ClassContainer.class);
   
   public ClassContainer(String name, Class<?> clazz, AspectManager manager) throws ClassNotFoundException
   {
      super(clazz, manager);
      
      // Wickedness: we load up a weaved class, so we must call interceptors ourselves
      //clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
      
//      super.setChainOverridingForInheritedMethods(true);
      
//      initializeClassContainer();
      
      attachClass(clazz);
      
      rebuildInterceptors();
   }
   
   public Object construct()
   {
      assert constructionInfos.length == 1 : "FIXME: Need to find the default constructor";
      int defaultConstructorIndex = 0; // FIXME
      
      log.debug("constructInfos = " + Arrays.toString(constructorInfos));
      log.debug("constructionInfos = " + Arrays.toString(constructionInfos));
      log.debug(Arrays.toString(constructionInfos[0].getInterceptors()));
      
      Interceptor[] cInterceptors = constructionInfos[0].getInterceptors();
      if (cInterceptors == null)
      {
         try
         {
            return constructors[defaultConstructorIndex].newInstance();
         }
         catch (InstantiationException e)
         {
            throw new RuntimeException(e);
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException(e);
         }
         catch (InvocationTargetException e)
         {
            if(e.getCause() instanceof Error)
               throw (Error) e.getCause();
            if(e.getCause() instanceof RuntimeException)
               throw (RuntimeException) e.getCause();
            throw new RuntimeException(e);
         }
      }
      
      log.debug("fire constructor invocation");
      
      ConstructorInvocation invocation = new ConstructorInvocation(cInterceptors);
      invocation.setAdvisor(this);
      invocation.setConstructor(constructors[defaultConstructorIndex]);
      try
      {
         return invocation.invokeNext();
      }
      catch (Throwable t)
      {
         if(t instanceof Error)
            throw (Error) t;
         if(t instanceof RuntimeException)
            throw (RuntimeException) t;
         throw new RuntimeException(t);
      }
   }
}
