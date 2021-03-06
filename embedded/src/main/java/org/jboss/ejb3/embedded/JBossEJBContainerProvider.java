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
package org.jboss.ejb3.embedded;

import java.util.Collection;
import java.util.Map;

import javax.ejb.EJBContainer;
import javax.ejb.EJBException;
import javax.ejb.spi.EJBContainerProvider;


/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class JBossEJBContainerProvider implements EJBContainerProvider
{
   public EJBContainer createEJBContainer(Map<?, ?> properties) throws EJBException
   {
      try
      {
         String modules[] = null;
         if(properties != null)
         {
            Object o = properties.get(EJBContainer.EMBEDDABLE_MODULES_PROPERTY);
            if(o != null)
            {
               if(o instanceof String)
                  modules = new String[] { (String) o };
               else if(o instanceof Collection)
                  modules = toStringArray(o);
               else
                  throw new EJBException("Illegal type of " + EJBContainer.EMBEDDABLE_MODULES_PROPERTY + " (" + o.getClass().getName() + ") (EJB 3.1 22.2.2.2)");
            }
         }
         return new JBossEJBContainer(properties, modules);
      }
      catch(Throwable t)
      {
         if(t instanceof Error)
            throw (Error) t;
         if(t instanceof RuntimeException)
            throw (RuntimeException) t;
         if(t instanceof Exception)
            throw new EJBException((Exception) t);
         throw new RuntimeException(t);
      }
   }
   
   @SuppressWarnings("unchecked")
   private static final String[] toStringArray(Object o)
   {
      return ((Collection<String>) o).toArray(new String[0]);
   }
}
