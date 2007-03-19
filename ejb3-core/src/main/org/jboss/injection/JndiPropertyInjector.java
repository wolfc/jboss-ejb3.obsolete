/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.injection;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.ejb3.BeanContext;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.logging.Logger;

/**
 * Injects a jndi dependency into a bean property.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class JndiPropertyInjector extends AbstractPropertyInjector implements PojoInjector
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(JndiPropertyInjector.class);
   
   private String jndiName;
   private Context ctx;

   public JndiPropertyInjector(BeanProperty property, String jndiName, Context ctx)
   {
      super(property);
      this.jndiName = jndiName;
      this.ctx = ctx;
   }

   public void inject(BeanContext bctx)
   {
      inject(bctx, bctx.getInstance());
   }
   
   public Class<?> getInjectionClass()
   {
      return property.getType();
   }
   
   protected Object lookup(String jndiName)
   {
      Object dependency = null;
      
      try
      {
         dependency = ctx.lookup(jndiName);
      }
      catch (NamingException e)
      {
         Throwable cause = e;
         while(cause.getCause() != null)
            cause = cause.getCause();
         throw new RuntimeException("Unable to inject jndi dependency: " + jndiName + " into property " + property + ": " + cause.getMessage(), e);
      }
      return dependency;
   }
   
   public void inject(BeanContext bctx, Object instance)
   {
      inject(instance);
   }

   public void inject(Object instance)
   {
      Object value = lookup(jndiName);
      property.set(instance, value);
   }
}
