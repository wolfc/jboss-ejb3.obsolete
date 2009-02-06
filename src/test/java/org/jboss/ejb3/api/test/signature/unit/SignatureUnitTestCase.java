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
package org.jboss.ejb3.api.test.signature.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileVisitor;
import org.jboss.virtual.VisitorAttributes;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SignatureUnitTestCase
{
   private static final Logger log = Logger.getLogger(SignatureUnitTestCase.class);
   
   private static void assertFieldsEquals(String message, Field expectedFields[], Field actualFields[])
   {
      for(Field expected : expectedFields)
      {
         Field actual = findField(actualFields, expected);
         if(actual == null)
            throw new AssertionError(message + ": expected <" + expected + "> but is not found " + Arrays.asList(actualFields));
      }
      // TODO: check too many fields defined
   }
   
   private static void assertSameClass(Class<?> expected, Class<?> actual)
   {
      assertNotSame(expected.getProtectionDomain().getCodeSource(), actual.getProtectionDomain().getCodeSource());
      try
      {
         assertEquals(expected.getName(), asSet(expected.getAnnotations()), asSet(actual.getAnnotations()));
      }
      catch(AssertionError e)
      {
         log.warn(e);
      }
      assertSetEquals(expected.getName(), asSet(expected.getMethods()), asSet(actual.getMethods()));
      
      assertFieldsEquals(expected.getName(), expected.getFields(), actual.getFields());
   }
   
   private static void assertSetEquals(String message, Set<Method> expected, Set<Method> actual)
   {
      for(Method m1 : expected)
      {
         Method m2 = findMethod(actual, m1);
         if(m2 == null)
            throw new AssertionError(message + ": expected <" + m1 + "> but is not found " + actual);
      }
      // TODO: check for too many methods defined
   }
   
   private static <T> Set<T> asSet(T objs[])
   {
      Set<T> set = new HashSet<T>();
      for(T obj : objs)
         set.add(obj);
      return set;
   }
   
   private static ClassLoader createClassLoader(URL... urls)
   {
      return new URLClassLoader(urls, null);
   }
   
   private static boolean equalSignature(Method m1, Method m2)
   {
      if(m1 == m2)
         return true;
      if(m1 == null || m2 == null)
         return false;
      if(!m1.getName().equals(m2.getName()))
         return false;
      if(!m1.getReturnType().getName().equals(m2.getReturnType().getName()))
         return false;
      Class<?> m1p[] = m1.getParameterTypes();
      Class<?> m2p[] = m2.getParameterTypes();
      if(m1p.length != m2p.length)
         return false;
      for(int i = 0; i < m1p.length; i++)
      {
         if(!m1p[i].getName().equals(m2p[i].getName()))
            return false;
      }
      Class<?> m1e[] = m1.getExceptionTypes();
      Class<?> m2e[] = m2.getExceptionTypes();
      int j = 0;
      for(int i = 0; i < m1e.length; i++)
      {
         if(i >= m2e.length || !m1e[i].getName().equals(m2e[i].getName()))
         {
            if(RuntimeException.class.isAssignableFrom(m1e[i]))
               continue;
            return false;
         }
         j++;
      }
      if(j != m2e.length)
         return false;
      return true;
   }
   
   private static Field findField(Field candidates[], Field signature)
   {
      for(Field candidate : candidates)
      {
         if(!candidate.getName().equals(signature.getName()))
            continue;
         if(!candidate.getType().getName().equals(signature.getType().getName()))
            continue;
         if(candidate.getModifiers() != signature.getModifiers())
            continue;
         return candidate;
      }
      return null;
   }
   
   /**
    * Find the correct method based on the signature.
    * @param methods
    * @param signature
    * @return
    */
   private static Method findMethod(Set<Method> methods, Method signature)
   {
      for(Method candidate : methods)
      {
         if(equalSignature(candidate, signature))
         {
            return candidate;
         }
      }
      return null;
   }
   
   @Test
   public void testSignatures() throws Exception
   {
      String path = System.getProperty("javax.ejb.jar.path");
      assertNotNull("javax.ejb.jar.path not set", path);
      URI rootURI = new File(path).toURI();
      VirtualFile file = VFS.getRoot(rootURI);
      final Set<String> classNames = new HashSet<String>();
      VirtualFileVisitor visitor = new VirtualFileVisitor() {
         public VisitorAttributes getAttributes()
         {
            return VisitorAttributes.RECURSE_LEAVES_ONLY;
         }

         public void visit(VirtualFile virtualFile)
         {
            String name = virtualFile.getPathName();
            if(name.endsWith(".class"))
               classNames.add(name.substring(0, name.length() - 6).replace('/', '.'));
         }
         
      };
      file.visit(visitor);
      
      ClassLoader cl = createClassLoader(rootURI.toURL(), UserTransaction.class.getProtectionDomain().getCodeSource().getLocation());
      
      for(String className : classNames)
      {
         Class<?> expected = cl.loadClass(className);
         try
         {
            Class<?> actual = Class.forName(className);
            assertSameClass(expected, actual);
         }
         catch(ClassNotFoundException e)
         {
            log.warn("class not found " + expected);
         }
      }
   }
   
   @Test
   public void testFail()
   {
      fail("test");
   }
}
