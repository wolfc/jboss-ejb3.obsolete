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
package org.jboss.ejb3.cache.spi.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.PersistentObjectStore;
import org.jboss.logging.Logger;
import org.jboss.serial.io.JBossObjectInputStream;
import org.jboss.serial.io.JBossObjectOutputStream;

/**
 * Stores objects in a directory via serialization.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 65339 $
 */
public class FilePersistentObjectStore<T extends CacheItem> 
   implements PersistentObjectStore<T>
{
   private static final Logger log = Logger.getLogger(FilePersistentObjectStore.class);
   
   private int subdirectoryCount;   
   private File baseDirectory;
   private File[] storageDirectories;
   
   private static class DeleteFileAction implements PrivilegedAction<Boolean>
   {
      File file;

      DeleteFileAction(File file)
      {
         this.file = file;
      }

      public Boolean run()
      {
         return file.delete();
      }

      static boolean delete(File file)
      {
         DeleteFileAction action = new DeleteFileAction(file);
         return AccessController.doPrivileged(action);
      }
   }

   private static class FISAction implements PrivilegedExceptionAction<FileInputStream>
   {
      File file;

      FISAction(File file)
      {
         this.file = file;
      }

      public FileInputStream run() throws FileNotFoundException
      {
         FileInputStream fis = new FileInputStream(file);
         return fis;
      }

      static FileInputStream open(File file) throws FileNotFoundException
      {
         FISAction action = new FISAction(file);
         FileInputStream fis = null;
         try
         {
            fis = AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException e)
         {
            throw (FileNotFoundException) e.getException();
         }
         return fis;
      }
   }

   private static class FOSAction implements PrivilegedExceptionAction<FileOutputStream>
   {
      File file;

      FOSAction(File file)
      {
         this.file = file;
      }

      public FileOutputStream run() throws FileNotFoundException
      {
         FileOutputStream fis = new FileOutputStream(file);
         return fis;
      }

      static FileOutputStream open(File file) throws FileNotFoundException
      {
         FOSAction action = new FOSAction(file);
         FileOutputStream fos = null;
         try
         {
            fos = AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException e)
         {
            throw (FileNotFoundException) e.getException();
         }
         return fos;
      }
   }

   private static class MkdirsFileAction implements PrivilegedAction<Boolean>
   {
      File file;

      MkdirsFileAction(File file)
      {
         this.file = file;
      }

      public Boolean run()
      {
         return file.mkdirs();
      }

      static boolean mkdirs(File file)
      {
         MkdirsFileAction action = new MkdirsFileAction(file);
         return AccessController.doPrivileged(action);
      }
   }

   protected File getFile(Object key)
   {
      File base = null;
      if (storageDirectories != null)
      {
         int hash = (key.hashCode()& 0x7FFFFFFF) % storageDirectories.length;
         base = storageDirectories[hash];
      }
      else
      {
         base = baseDirectory;
      }
      return new File(base, String.valueOf(key) + ".ser");
      
   }
   
   @SuppressWarnings("unchecked")
   public T load(Object key)
   {
      File file = getFile(key);
      if(!file.exists())
         return null;
      
      log.debug("loading state from " + file);
      try
      {
         FileInputStream fis = FISAction.open(file);
         ObjectInputStream in = new JBossObjectInputStream(fis);
//         ObjectInputStream in = new ObjectInputStream(fis);
         try
         {
            return (T) in.readObject();
         }
         finally
         {
            in.close();
            DeleteFileAction.delete(file);
         }
      }
      catch(ClassNotFoundException e)
      {
         throw new RuntimeException("failed to load object " + key, e);
      }
      catch(IOException e)
      {
         throw new RuntimeException("failed to load object " + key, e);
      }
   }

   public void setStorageDirectory(String dirName)
   {
      baseDirectory = new File(dirName);
   }   
   
   public void setSubdirectoryCount(int subdirectoryCount)
   {
      this.subdirectoryCount = subdirectoryCount;
   }

   public void start()
   {
      assert baseDirectory != null : "baseDirectory is null";
      assert subdirectoryCount > 0 : "subdirectoryCount is < 1";
      
      establishDirectory(baseDirectory);
      
      if (subdirectoryCount > 1)
      {
         storageDirectories = new File[subdirectoryCount];
         for (int i = 0; i < storageDirectories.length; i++)
         {
            File f = new File(baseDirectory, String.valueOf(i));
            establishDirectory(f);
            storageDirectories[i] = f;
         }
      }
   }
   
   private void establishDirectory(File dir)
   {      
      if(!dir.exists())
      {
         if(!MkdirsFileAction.mkdirs(dir))
            throw new RuntimeException("Unable to create storage directory " + dir);
         dir.deleteOnExit();
      }
      
      if(!dir.isDirectory())
         throw new RuntimeException("Storage directory " + dir + " is not a directory");
      
   }
   
   public void stop()
   {
      // TODO: implement
   }
   
   public void store(T obj)
   {
      File file = getFile(obj.getId());
      file.deleteOnExit();
      log.debug("saving state to " + file);
      try
      {
         FileOutputStream fos = FOSAction.open(file);
         ObjectOutputStream out = new JBossObjectOutputStream(fos);
//         ObjectOutputStream out = new ObjectOutputStream(fos);
         try
         {
            out.writeObject(obj);
            out.flush();
         }
         finally
         {
            out.close();
         }
      }
      catch(IOException e)
      {
         throw new RuntimeException("failed to store object " + obj.getId(), e);
      }
   }
}
