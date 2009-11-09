/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.proxy.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * An ObjectInputStream that allows a custom class loader.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ObjectInputStream extends java.io.ObjectInputStream
{
   /** table mapping primitive type names to corresponding class objects */
   private static final HashMap<String, Class<?>> primClasses = new HashMap<String, Class<?>>(8, 1.0F);
   static
   {
      primClasses.put("boolean", boolean.class);
      primClasses.put("byte", byte.class);
      primClasses.put("char", char.class);
      primClasses.put("short", short.class);
      primClasses.put("int", int.class);
      primClasses.put("long", long.class);
      primClasses.put("float", float.class);
      primClasses.put("double", double.class);
      primClasses.put("void", void.class);
   }

   private ClassLoader classLoader;
   
   /**
    * @param in
    * @throws IOException
    */
   public ObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException
   {
      super(in);
      
      assert classLoader != null : "classLoader is null";
      
      this.classLoader = classLoader;
   }
   
   protected ClassLoader getClassLoader()
   {
      return classLoader;
   }
   
   @Override
   protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
   {
      String name = desc.getName();
      try
      {
         return Class.forName(name, false, getClassLoader());
      }
      catch (ClassNotFoundException ex)
      {
         Class<?> cl = primClasses.get(name);
         if (cl != null)
         {
            return cl;
         }
         else
         {
            throw ex;
         }
      }
   }
   
   @Override
   protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException
   {
      ClassLoader latestLoader = getClassLoader();
      ClassLoader nonPublicLoader = null;
      boolean hasNonPublicInterface = false;

      // define proxy in class loader of non-public interface(s), if any
      Class<?>[] classObjs = new Class[interfaces.length];
      for (int i = 0; i < interfaces.length; i++)
      {
         Class<?> cl = Class.forName(interfaces[i], false, latestLoader);
         if ((cl.getModifiers() & Modifier.PUBLIC) == 0)
         {
            if (hasNonPublicInterface)
            {
               if (nonPublicLoader != cl.getClassLoader())
               {
                  throw new IllegalAccessError("conflicting non-public interface class loaders");
               }
            }
            else
            {
               nonPublicLoader = cl.getClassLoader();
               hasNonPublicInterface = true;
            }
         }
         classObjs[i] = cl;
      }
      try
      {
         return Proxy.getProxyClass(hasNonPublicInterface ? nonPublicLoader : latestLoader, classObjs);
      }
      catch (IllegalArgumentException e)
      {
         throw new ClassNotFoundException(null, e);
      }
   }

}
