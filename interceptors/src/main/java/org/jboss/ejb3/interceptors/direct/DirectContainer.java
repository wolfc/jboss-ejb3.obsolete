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
package org.jboss.ejb3.interceptors.direct;

import org.jboss.aop.Domain;
import org.jboss.logging.Logger;

/**
 * The direct container invokes interceptors directly on an instance.
 * 
 * It's useful in an environment where we don't want to fiddle with the
 * classloader and still have control on how instances are called.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DirectContainer<T> extends AbstractDirectContainer<T, DirectContainer<T>>
{
   private static final Logger log = Logger.getLogger(DirectContainer.class);
   
   public DirectContainer(String name, Domain domain, Class<? extends T> beanClass)
   {
      super(name, domain, beanClass);
   }
   
   public DirectContainer(String name, String domainName, Class<? extends T> beanClass)
   {
      super(name, domainName, beanClass);
   }
}
