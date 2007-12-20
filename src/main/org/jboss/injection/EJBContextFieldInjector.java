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

import java.lang.reflect.Field;
import org.jboss.ejb3.BeanContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 * @deprecated use EJBContextPropertyInjector
 */
public class EJBContextFieldInjector implements Injector, PojoInjector
{
   private Field field;

   public EJBContextFieldInjector(Field field)
   {
      this.field = field;
      field.setAccessible(true);
   }

   public void inject(BeanContext ctx)
   {
      inject(ctx, ctx.getInstance());
   }
   
   public void inject(BeanContext ctx, Object instance)
   {
      try
      {
         field.set(instance, ctx.getEJBContext());
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void inject(Object instance)
   {
      throw new RuntimeException("Illegal operation");
   }

   public Class getInjectionClass()
   {
      return field.getType();
   }
}
