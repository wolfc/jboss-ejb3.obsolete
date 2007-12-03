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
package org.jboss.ejb3.annotation.impl;

import java.lang.annotation.Annotation;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.defaults.RemoteBindingDefaults;
import org.jboss.ejb3.util.Debugger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class RemoteBindingImpl implements RemoteBinding
{
   private String jndi;

   private String stack;

   private String bindUrl;

   private String proxyFactory;

   private StackTraceElement constructionPoint;

   public RemoteBindingImpl()
   {
      jndi = "";
      stack = "";
      bindUrl = "";
      proxyFactory = RemoteBindingDefaults.PROXY_FACTORY_DEFAULT;

      this.constructionPoint = Debugger.constructionPoint();
   }

   public RemoteBindingImpl(String jndi, String stack, String bindUrl, String proxyFactory)
   {
      this.jndi = jndi;
      this.stack = stack;
      this.bindUrl = bindUrl;
      this.proxyFactory = proxyFactory;

      this.constructionPoint = Debugger.constructionPoint();
   }

   public void setStack(String stack)
   {
      this.stack = stack;
   }

   public void setFactory(String factory)
   {
      this.proxyFactory = factory;
   }

   public void setJndiBinding(String jndi)
   {
      this.jndi = jndi;
   }

   public void setBindUrl(String bindUrl)
   {
      this.bindUrl = bindUrl;
   }

   public String jndiBinding()
   {
      return jndi;
   }

   public String interceptorStack()
   {
      return stack;
   }

   public String clientBindUrl()
   {
      return bindUrl;
   }

   public String factory()
   {
      return proxyFactory;
   }

   public void merge(RemoteBinding annotation)
   {
      if (jndi.length() == 0)
         jndi = annotation.jndiBinding();

      if (stack.length() == 0)
         stack = annotation.interceptorStack();

      if (bindUrl.length() == 0)
         bindUrl = annotation.clientBindUrl();

      if (proxyFactory.equals(RemoteBindingDefaults.PROXY_FACTORY_DEFAULT))
         proxyFactory = annotation.factory();

   }

   public Class<? extends Annotation> annotationType()
   {
      return RemoteBinding.class;
   }

   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      sb.append("[RemoteBindingImpl:");
      sb.append(", jndi=" + jndi);
      sb.append(", stack=" + stack);
      sb.append(", bindUrl=" + bindUrl);
      sb.append(", proxyFactory=" + proxyFactory);
      sb.append(", constructionPoint=" + constructionPoint);
      sb.append(']');
      return sb.toString();
   }
}
