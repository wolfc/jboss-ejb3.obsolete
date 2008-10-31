/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3;

import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.logging.Logger;

import javax.management.ObjectName;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class MCClientKernelAbstraction implements ClientKernelAbstraction
{
   private static final Logger log = Logger.getLogger(MCClientKernelAbstraction.class);

   protected Kernel kernel;

   public MCClientKernelAbstraction(Kernel kernel)
   {
      assert kernel != null : "kernel is null";
      
      this.kernel = kernel;
   }

   public Object invoke(ObjectName objectName, String operationName, Object[] params, String[] signature) throws Exception
   {
      String name = objectName.getCanonicalName();
      KernelRegistryEntry entry = kernel.getRegistry().getEntry(name);
      if (entry != null)
      {
         Object target = entry.getTarget();
         Class[] types = new Class[signature.length];
         for (int i = 0; i < signature.length; ++i)
         {
            types[i] = Thread.currentThread().getContextClassLoader().loadClass(signature[i]);
         }
         Method method = target.getClass().getMethod(operationName, types);
         return method.invoke(target, params);
      }
      return null;
   }

   public Object getAttribute(ObjectName objectName, String attribute) throws Exception
   {
      String name = objectName.getCanonicalName();
      KernelRegistryEntry entry = kernel.getRegistry().getEntry(name);
      if (entry != null)
      {
         Object target = entry.getTarget();
         Field field = target.getClass().getField(attribute);
         return field.get(target);
      }
      return null;
   }
   
   public Set getMBeans(ObjectName query) throws Exception
   {
      Object target = kernel.getRegistry().getEntry(query);
      Set set = new HashSet();
      set.add(target);
      return set;
   }
}
