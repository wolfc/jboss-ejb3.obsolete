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
package org.jboss.ejb3.interceptors.aop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

import org.jboss.aop.Domain;
import org.jboss.ejb3.interceptors.lang.BootstrapClassLoader;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DomainClassLoader extends ClassLoader
{
   private static final Logger log = Logger.getLogger(DomainClassLoader.class);
   
   /**
    * Load domain classes from here.
    */
   private ClassLoader delegate;
   
   /**
    * Load JDK classes from here.
    * 
    * Do not confuse this with interceptor bootstrapping
    */
   private ClassLoader bootstrapClassLoader = new BootstrapClassLoader();
   
   private Domain domain;
   
   public DomainClassLoader(ClassLoader parent, ClassLoader delegate, Domain domain)
   {
      super(parent);
      
      assert delegate != null : "delegate is null";
      assert domain != null : "domain is null";
      
      this.delegate = delegate;
      this.domain = domain;
      
      //domain.registerClassLoader(this); // TODO: What does this do?
   }
   
   /**
    * Copy of SystemClassLoader.defineClassFromBytes
    * @param name
    * @param bytes
    * @param resolve
    * @return
    */
   private Class<?> defineClassFromBytes(String name, ClassBytes bytes, boolean resolve)
   {
      definePackage(name);
      byte[] b = bytes.bytes;
      Class<?> clazz = defineClass(name, b, 0, b.length, bytes.protectionDomain);
      if (resolve) resolveClass(clazz);
      return clazz;
   }

   /**
    * Copy of SystemClassLoader.definePackage
    * 
    * TODO: do this properly
    * 
    * @param className
    */
   private void definePackage(String className)
   {
      int i = className.lastIndexOf('.');
      if (i == -1)
         return;

      // we are not allowed to synchronize on ClassLoader.packages
      try
      {
         definePackage(className.substring(0, i), null, null, null, null, null, null, null);
      }
      catch (IllegalArgumentException alreadyDone)
      {
      }
   }

   private ClassLoader getDelegate()
   {
      return delegate;
   }
   
   /**
    * Purely for scoping class loader policy
    * 
    * Do not use, it might be gone in later versions.
    * 
    * @return   the domain of this class loader
    */
   public Domain getDomain()
   {
      return domain;
   }
   
   /**
    * Load a class, overridden to transform aop enhanced classes
    * and load non jre classes through this classloader.
    *
    * @param name the class name
    * @param resolve whether to resolve the class
    * @return the class
    * @throws ClassNotFoundException when there is no class
    */
   protected synchronized Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException
   {
      if(log.isTraceEnabled()) log.trace("loadClass " + name);
      
      // Have we already loaded the class?
      Class<?> clazz = findLoadedClass(name);
      if (clazz != null)
      {
         if (resolve) resolveClass(clazz);
         return clazz;
      }

      // Is it a JRE class?
      try
      {
         clazz = bootstrapClassLoader.loadClass(name);
         if (resolve) resolveClass(clazz);
         return clazz;
      }
      catch(ClassNotFoundException e)
      {
         // it's not a jdk class
         log.trace("Can't find class '" + name + "' in bootstrap.");
      }
      
      final String classFileName = name.replace('.', '/') + ".class";
      final URL url = getDelegate().getResource(classFileName);
      if(url == null)
      {
         if(log.isTraceEnabled()) log.trace("Did not find an url for '" + name + "'.");
         clazz = getParent().loadClass(name);
         if(resolve) resolveClass(clazz);
         return clazz;
      }
      
      log.debug(name + " found on " + url);
      // Load the class
      try
      {
         ClassBytes origBytes = loadClassBytes(name, classFileName, url);
         ClassBytes classBytes = new ClassBytes();

         assert !name.startsWith("org.jboss.aop") : "We should not have found a valid url";
         
         classBytes.bytes = domain.transform(this, name, null, origBytes.protectionDomain, origBytes.bytes);
         classBytes.protectionDomain = origBytes.protectionDomain;
            
         if (classBytes.bytes == null)
            classBytes = origBytes;

         // Define the class
         Class<?> cls = defineClassFromBytes(name, classBytes, resolve);
         if(resolve) resolveClass(cls);
         //assert Advised.class.isAssignableFrom(cls) : "Class " + cls + " is not really transformed";
         return cls;
      }
      catch(ClassNotFoundException e)
      {
         // TODO: We can't find any bytes for it, let's just pass it over
         log.warn("Unable to instrument '" + name + "'", e);
         return super.loadClass(name, resolve);
      }
      catch (IOException ioe)
      {
         throw new ClassNotFoundException("Unable to load " + name, ioe);
      }
      catch (IllegalAccessException iae)
      {
         // TODO: this might kill a thread
         throw new Error(iae);
      }
      catch (Exception e)
      {
         // TODO: this might kill a thread
         throw new Error("Error transforming the class " + name, e);
      }
   }

   /**
    * Load the bytecode for a class
    */
   private ClassBytes loadClassBytes(String name, final String classFileName, final URL url)
      throws ClassNotFoundException, IOException
   {
//      final String classFileName = name.replace('.', '/') + ".class";
//      final URL url = AccessController.doPrivileged(new PrivilegedAction<URL>() 
//      {
//         public URL run()
//         {
//            return getParent().getResource(classFileName);
//         }
//      });
      ProtectionDomain protectionDomain = null;
      InputStream in = null;
      if (url != null)
      {
         try
         {
            in = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>()
            {
               public InputStream run() throws Exception
               {
                  return url.openStream();
               }
            });
         }
         catch (PrivilegedActionException e)
         {
            throw new ClassNotFoundException(name, e);
         }
         String urlstring = url.toExternalForm();
         URL urlCS = url;
         if (urlstring.startsWith("jar:"))
         {
            int i = urlstring.indexOf('!');
            String cs = urlstring.substring(4, i);
            urlCS = new URL(cs);
         }
         else
         {
            int i = urlstring.indexOf(classFileName);
            if (i != -1)
            {
               String cs = urlstring.substring(0, i);
               urlCS = new URL(cs);
            }
         }
         CodeSource codeSource = new CodeSource(urlCS, (Certificate[]) null);
         protectionDomain = new ProtectionDomain(codeSource, null, this, null);
      }
      else
      {
         /* Try the system tmpdir/aopdynclasses, the default location
         the AOPClassPool writes dynamic class files to.
         */
         try
         {
            in = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>()
            {
               public InputStream run() throws Exception
               {
                  String tmpdir = System.getProperty("java.io.tmpdir");
                  File aopdynclasses = new File(tmpdir, "aopdynclasses");
                  File classFile = new File(aopdynclasses, classFileName);
                  return new FileInputStream(classFile);
               }
            });
         }
         catch (PrivilegedActionException e)
         {
            throw new ClassNotFoundException(name, e);
         }
      }

      byte[][] bufs = new byte[8][];
      int bufsize = 4096;

      for (int i = 0; i < 8; ++i)
      {
         bufs[i] = new byte[bufsize];
         int size = 0;
         int len = 0;
         do
         {
            len = in.read(bufs[i], size, bufsize - size);
            if (len >= 0)
               size += len;
            else
            {
               byte[] result = new byte[bufsize - 4096 + size];
               int s = 0;
               for (int j = 0; j < i; ++j)
               {
                  System.arraycopy(bufs[j], 0, result, s, s + 4096);
                  s = s + s + 4096;
               }

               System.arraycopy(bufs[i], 0, result, s, size);
               ClassBytes classBytes = new ClassBytes();
               classBytes.bytes = result;
               classBytes.protectionDomain = protectionDomain;
               return classBytes;
            }
         }
         while (size < bufsize);
         bufsize *= 2;
      }

      throw new IOException("too much data loading class " + name);
   }
   
   /**
    * ClassBytes.
    */
   private static class ClassBytes
   {
      public ProtectionDomain protectionDomain;
      public byte[] bytes;
   }
}
