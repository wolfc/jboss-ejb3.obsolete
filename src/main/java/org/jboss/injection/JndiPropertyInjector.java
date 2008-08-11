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

import java.lang.reflect.Proxy;
import java.util.Arrays;

import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NamingException;

import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.JndiUtil;
import org.jboss.ejb3.session.SessionSpecContainer;
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
         dependency = JndiUtil.lookup(ctx, jndiName);
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
      
//      Class<?> valueClass = value.getClass();
//      
//      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
//      ClassLoader classcl = property.getType().getClassLoader();
//      ClassLoader valueCl = valueClass.getClassLoader();
//      ClassLoader proxyHandlerCl = null;
//      if(Proxy.isProxyClass(value.getClass()))
//      {
//         Object proxyHandler = Proxy.getInvocationHandler(value);
//         proxyHandlerCl = proxyHandler.getClass().getClassLoader();
//         
//      }
//      ClassLoader containerCl = SessionSpecContainer.TMP_CL;
//      boolean equalsCl = classcl.equals(valueCl);
      
      log.trace("injecting " + value + " from " + jndiName + " into " + property + " of " + instance);
      try
      {
         property.set(instance, value);
      }
      catch(IllegalArgumentException e)
      {
         // We found something to inject, but it happened to be the wrong thing
         String realJndiName;
         try
         {
            // TODO: check whether it's a real link beforehand
            Object link = JndiUtil.lookupLink(ctx, jndiName);
            realJndiName = jndiName + (link instanceof LinkRef ? " (link -> " + ((LinkRef) link).getLinkName() + ")" : "");
         }
         catch(NamingException ne)
         {
            log.trace("Failed to obtain the real JNDI name", ne);
            realJndiName = jndiName;
         }
         Class<?> interfaces[] = value.getClass().getInterfaces();
         String interfacesStr = (interfaces.length > 0 ? " (implements " + Arrays.toString(interfaces) + ")" : "");
         String msg = "failed to inject " + value + interfacesStr + " from " + realJndiName + " into " + property + " of " + instance;
         throw new IllegalArgumentException(msg, e);
      }
   }
}
