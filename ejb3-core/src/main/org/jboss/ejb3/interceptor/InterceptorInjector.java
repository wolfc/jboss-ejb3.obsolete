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
package org.jboss.ejb3.interceptor;

import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.injection.Injector;
import org.jboss.injection.PojoInjector;
import org.jboss.ejb3.metamodel.Interceptor;

import java.lang.reflect.AccessibleObject;
import java.util.Map;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
public class InterceptorInjector
{
   EJBContainer container;
   InterceptorInfo info;
   PojoInjector[] injectors;
   protected Map<AccessibleObject, Injector> encInjections;
   
   public InterceptorInjector(Container container, InterceptorInfo info, Map<AccessibleObject, Injector> injections)
   {
      this.container = (EJBContainer)container;
      this.info = info;
      injectors = injections.values().toArray(new PojoInjector[injections.size()]);
   }

   public Interceptor getXml()
   {
      return info.getXml();
   }
   
   public Class getClazz()
   {
      return info.getClazz();
   }
   
   public Container getContainer()
   {
      return container;
   }
   
   public void inject(BeanContext ctx, Object instance)
   {
      for (PojoInjector injector : injectors)
      {
         injector.inject(ctx, instance);
      }
   }
}
