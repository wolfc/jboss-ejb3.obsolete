/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.session;

import javax.ejb.EJBContext;

import org.jboss.ejb3.EnterpriseBeanContext;

/**
 * An instance of an enterprise bean link to its container.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public abstract class SessionBeanContext<T extends SessionContainer> extends EnterpriseBeanContext<T>
{
   protected EJBContext ejbContext;

   /**
    * Must not break getInstance post condition!
    * @param container
    */
   protected SessionBeanContext(T container)
   {
      super(container);
   }
   
   protected SessionBeanContext(T container, Object bean)
   {
      super(container, bean);
   }
   
   /**
    * Only for externalization use by subclass StatefulBeanContext; do not use elsewhere.
    *
    * @deprecated
    */
   protected SessionBeanContext()
   {
      
   }
   
   public abstract EJBContext getEJBContext();

}
