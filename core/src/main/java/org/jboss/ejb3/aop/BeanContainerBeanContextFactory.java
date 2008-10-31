/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.aop;

import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.container.BeanContextFactory;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class BeanContainerBeanContextFactory implements BeanContextFactory<Object, BeanContainer>
{
   private BeanContainer container;
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.interceptors.container.BeanContextFactory#createBean()
    */
   public BeanContext<Object> createBean() throws Exception
   {
      return container.getEJBContainer().createBeanContext();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.interceptors.container.BeanContextFactory#destroyBean(org.jboss.ejb3.interceptors.container.BeanContext)
    */
   public void destroyBean(BeanContext<Object> bean)
   {
      container.getEJBContainer().destroyBeanContext(bean);
   }

   public void setContainer(BeanContainer container)
   {
      this.container = container;
   }
}
