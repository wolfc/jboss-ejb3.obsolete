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

import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.logging.Logger;

/**
 * Injects a single property.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class AbstractInjector implements Injector
{
   private static final Logger log = Logger.getLogger(AbstractInjector.class);
   
   private BeanProperty property;
   
   protected AbstractInjector(BeanProperty property)
   {
      assert property != null;
      
      this.property = property;
   }
   
   public void inject(Object instance)
   {
      Object value = getValue();
      if(log.isTraceEnabled())
         log.trace("injecting property " + property + " with value " + value);
      property.set(instance, value);
   }
   
   protected abstract Object getValue();
}