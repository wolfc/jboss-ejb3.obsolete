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
import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.JndiUtil;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 * @deprecated  use JndiPropertyInjector
 */
public class JndiFieldInjector implements Injector, PojoInjector
{
   private static final Logger log = Logger.getLogger(JndiFieldInjector.class);
   
   private Field field;
   private String jndiName;
   private Context ctx;

   public JndiFieldInjector(Field field, String jndiName, Context ctx)
   {
      this.field = field;
      this.field.setAccessible(true);
      this.jndiName = jndiName;
      this.ctx = ctx;
   }

   public JndiFieldInjector(Field field, Context ctx)
   {
      this(field, field.getName(), ctx);
   }

   public void inject(BeanContext bctx)
   {
      inject(bctx, bctx.getInstance());
   }

   public Class getInjectionClass()
   {
      return field.getType();
   }

   public Field getField()
   {
      return field;
   }

   protected Object lookup(String jndiName, Class field)
   {
      Object dependency = null;

      try
      {
         dependency = JndiUtil.lookup(ctx, jndiName);
      }
      catch (NamingException e)
      {
         e.printStackTrace();
         throw new RuntimeException("Unable to inject jndi dependency: " + jndiName + " into field " + field, e);
      }
      
      return dependency;
   }
   
   public void inject(BeanContext bctx, Object instance)
   {
      inject(instance);
   }

   public void inject(Object instance)
   {
      
      Object dependency = lookup(jndiName, field.getType());
      
      try
      {
         field.set(instance, dependency);
      }
      catch (IllegalArgumentException e)
      {
         String type = "UNKNOWN";
         String interfaces = "";
         if (dependency != null)
         {
            type = dependency.getClass().getName();
            Class[] intfs = dependency.getClass().getInterfaces();
            for (Class intf : intfs) interfaces += ", " + intf.getName();
         }
         throw new RuntimeException("Non matching type for inject of field: " + field + " for type: " + type + " of jndiName " + jndiName + "\nintfs: " + interfaces, e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public String toString()
   {
      return super.toString() + "{field=" + field + ",jndiName=" + jndiName + "}";
   }
}
