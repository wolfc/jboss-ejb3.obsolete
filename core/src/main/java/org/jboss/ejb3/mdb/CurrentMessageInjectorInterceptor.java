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
package org.jboss.ejb3.mdb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class CurrentMessageInjectorInterceptor implements Interceptor
{
   protected final static Logger log = Logger.getLogger(CurrentMessageInjectorInterceptor.class);
   
   private Field[] fields;
   private Method[] methods;

   public CurrentMessageInjectorInterceptor(Field[] fields, Method[] methods)
   {
      this.fields = fields;
      if (fields != null)
      {
         for (Field field : fields)
         {
            field.setAccessible(true);
         }
      }
      this.methods = methods;
      if (methods != null)
      {
         for (Method method : methods)
         {
            method.setAccessible(true);
         }
      }
   }


   public String getName()
   {
      return this.getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      Object target = invocation.getTargetObject();
      Object message = invocation.getMetaData().getMetaData(ConsumerContainer.CONSUMER_MESSAGE, ConsumerContainer.CONSUMER_MESSAGE);
      if (fields != null)
      {
         for (Field field : fields)
         {
            field.set(target, message);
         }
      }
      if (methods != null)
      {
         for (Method method : methods)
         {
            method.invoke(target, message);
         }

      }
      try
      {
      return invocation.invokeNext();
      }
      finally
      {
         // clear so we don't leak.
         if (fields != null)
         {
            for (Field field : fields)
            {
               field.set(target, null);
            }
         }
         if (methods != null)
         {
            for (Method method : methods)
            {
               Object[] args = new Object[method.getParameterTypes().length];
               for (int i = 0 ; i < args.length; ++i)
                  args[i] = null;
               method.invoke(target, args);
            }
         }
      }
   }
}
