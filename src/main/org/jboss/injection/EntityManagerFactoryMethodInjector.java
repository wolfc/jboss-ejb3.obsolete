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
package org.jboss.injection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jboss.ejb3.BeanContext;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class EntityManagerFactoryMethodInjector implements Injector, PojoInjector
{
   private static final Logger log = Logger.getLogger(EntityManagerFactoryMethodInjector.class);
   private Method setMethod;
   private Object factory;

   public EntityManagerFactoryMethodInjector(Method setMethod, Object factory)
   {
      this.setMethod = setMethod;
      setMethod.setAccessible(true);
      this.factory = factory;
   }

   public void inject(BeanContext ctx)
   {
      inject(ctx, ctx.getInstance());
   }
   
   public void inject(BeanContext ctx, Object instance)
   {
      inject(instance);
   }

   public void inject(Object instance)
   {
      try
      {
         Object[] args = {factory};
         setMethod.invoke(instance, args);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException("Failed in setting EntityManager on setter method: " + setMethod.toString());
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getCause());  //To change body of catch statement use Options | File Templates.
      }
   }

   public Class getInjectionClass()
   {
      return setMethod.getParameterTypes()[0];
   }
}
