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
import javax.naming.Context;
import javax.naming.NamingException;
import org.jboss.ejb3.BeanContext;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 * @deprecated use JndiPropertyInjector
 */
public class JndiMethodInjector implements Injector, PojoInjector
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(JndiMethodInjector.class);
   
   private Method setMethod;
   private String jndiName;
   private Context ctx;

   public JndiMethodInjector(Method setMethod, String jndiName, Context ctx)
   {
      this.setMethod = setMethod;
      setMethod.setAccessible(true);
      this.jndiName = jndiName;
      this.ctx = ctx;
   }

   public void inject(BeanContext bctx)
   {
      inject(bctx, bctx.getInstance());
   }
   
   public Class getInjectionClass()
   {
      return setMethod.getParameterTypes()[0];
   }
   
   protected Object lookup(String jndiName, Class param)
   {
      Object dependency = null;
      
      try
      {
         dependency = ctx.lookup(jndiName);
      }
      catch (NamingException e)
      {
         e.printStackTrace();
         throw new RuntimeException("Unable to @Inject jndi dependency: " + jndiName + " into method " + setMethod, e);
      }
      return dependency;
   }
   
   public void inject(BeanContext bctx, Object instance)
   {
      inject(instance);
   }

   public void inject(Object instance)
   {
      Object dependency = lookup(jndiName, setMethod.getParameterTypes()[0]);

      Object[] args = {dependency};
      try
      {
         setMethod.invoke(instance, args);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalArgumentException e)
      {
         String type = "UNKNOWN";
         if (dependency != null) type = dependency.getClass().getName();
         throw new RuntimeException("Non matching type for @Inject of setter: " + setMethod + " for type: " + type, e);  //To change body of catch statement use Options | File Templates.
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getCause());  //To change body of catch statement use Options | File Templates.
      }
   }
}
