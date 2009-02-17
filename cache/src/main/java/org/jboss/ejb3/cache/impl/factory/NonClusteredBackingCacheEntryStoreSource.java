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

package org.jboss.ejb3.cache.impl.factory;

import java.io.File;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.impl.backing.SimpleBackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;
import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;
import org.jboss.ejb3.cache.spi.impl.FilePersistentObjectStore;

/**
 * {@link BackingCacheEntryStoreSource} for a non-clustered cache. Uses
 * a {@link FilePersistentObjectStore} store for persistence.
 * 
 * @author Brian Stansberry
 */
public class NonClusteredBackingCacheEntryStoreSource<T extends CacheItem> 
   implements BackingCacheEntryStoreSource<T>
{
   /**
    * The default session store directory name ("<tt>ejb3/sessions</tt>").
    */
   String DEFAULT_SESSION_DIRECTORY_NAME = "ejb3" + File.separatorChar + "sessions";
   
   /**
    * The default session group store directory name ("<tt>ejb3/sfsbgroups</tt>").
    */
   String DEFAULT_GROUP_DIRECTORY_NAME = "ejb3" + File.separatorChar + "sfsbgroups";
   
   public static final int DEFAULT_SUBDIRECTORY_COUNT = 100;
   
   private String sessionDirectoryName = DEFAULT_SESSION_DIRECTORY_NAME;
   private String groupDirectoryName = DEFAULT_GROUP_DIRECTORY_NAME;
   private String baseDirectoryName;
   private int subdirectoryCount = DEFAULT_SUBDIRECTORY_COUNT;
   
   public BackingCacheEntryStore<T, SerializationGroup<T>> createGroupIntegratedObjectStore(String containerName, String cacheConfigName,
         CacheConfig cacheConfig, TransactionManager transactionManager, SynchronizationCoordinator synchronizationCoordinator)
   {
      FilePersistentObjectStore<SerializationGroup<T>> objectStore = new FilePersistentObjectStore<SerializationGroup<T>>();
      objectStore.setStorageDirectory(getFullGroupDirectoryName(containerName));
      objectStore.setSubdirectoryCount(subdirectoryCount);
      
      String storeNameSuffix = (cacheConfig.name().length() == 0) ? "" : "-" + cacheConfig;
      String storeName = "StdGroupStore" + storeNameSuffix;
      SimpleBackingCacheEntryStore<T, SerializationGroup<T>> store = 
         new SimpleBackingCacheEntryStore<T, SerializationGroup<T>>(objectStore, cacheConfig, storeName, true);
      
      return store;
   }

   public BackingCacheEntryStore<T, SerializationGroupMember<T>> createIntegratedObjectStore(String containerName, String cacheConfigName,
         CacheConfig cacheConfig, TransactionManager transactionManager, SynchronizationCoordinator synchronizationCoordinator)
   {
      FilePersistentObjectStore<SerializationGroupMember<T>> objectStore = new FilePersistentObjectStore<SerializationGroupMember<T>>();
      objectStore.setStorageDirectory(getFullSessionDirectoryName(containerName));
      objectStore.setSubdirectoryCount(subdirectoryCount);
      
      SimpleBackingCacheEntryStore<T, SerializationGroupMember<T>> store = 
         new SimpleBackingCacheEntryStore<T, SerializationGroupMember<T>>(objectStore, cacheConfig, containerName, false);
      
      return store;
   }
   
   protected String getFullSessionDirectoryName(String containerName)
   {
      File base = new File(getBaseDirectoryName());
      File child = new File(base, getSessionDirectoryName());
      File full = new File(child, containerName);
      return full.getAbsolutePath();
   }
   
   protected String getFullGroupDirectoryName(String containerName)
   {
      File base = new File(getBaseDirectoryName());
      File child = new File(base, getGroupDirectoryName());
      File full = new File(child, containerName);
      return full.getAbsolutePath();      
   }

   /**
    * Gets the name of the base directory under which sessions and 
    * groups should be stored. Default is the java.io.tmpdir.
    */
   public synchronized String getBaseDirectoryName()
   {
      if (baseDirectoryName == null)
      {
         setBaseDirectoryName(System.getProperty("java.io.tmpdir"));
      }
      return baseDirectoryName;
   }

   /**
    * Sets the name of the base directory under which sessions and 
    * groups should be stored.
    */
   public void setBaseDirectoryName(String baseDirectoryName)
   {
      this.baseDirectoryName = baseDirectoryName;
   }

   /**
    * Gets the name of the subdirectory under the 
    * {@link #getBaseDirectoryName() base directory} under which sessions 
    * should be stored. Default is {@link #DEFAULT_SESSION_DIRECTORY_NAME}.
    */
   public String getSessionDirectoryName()
   {
      return sessionDirectoryName;
   }

   /**
    * Sets the name of the subdirectory under the 
    * {@link #getBaseDirectoryName() base directory} under which sessions 
    * should be stored.
    */
   public void setSessionDirectoryName(String directoryName)
   {
      this.sessionDirectoryName = directoryName;
   }

   /**
    * Gets the name of the subdirectory under the 
    * {@link #getBaseDirectoryName() base directory} under which session groups 
    * should be stored. Default is {@link #DEFAULT_GROUP_DIRECTORY_NAME}.
    */
   public String getGroupDirectoryName()
   {
      return groupDirectoryName;
   }

   /**
    * Sets the name of the subdirectory under the 
    * {@link #getBaseDirectoryName() base directory} under which session groups 
    * should be stored.
    */
   public void setGroupDirectoryName(String groupDirectoryName)
   {
      this.groupDirectoryName = groupDirectoryName;
   }

   /**
    * Gets the number of subdirectories under the session directory or the
    * group directory into which the sessions/groups should be divided. Using
    * subdirectories helps overcome filesystem limits on the number of items
    * that can be stored. Default is {@link #DEFAULT_SUBDIRECTORY_COUNT}.
    */
   public int getSubdirectoryCount()
   {
      return subdirectoryCount;
   }

   /**
    * Sets the number of subdirectories under the session directory or the
    * group directory into which the sessions/groups should be divided.
    */
   public void setSubdirectoryCount(int subdirectoryCount)
   {
      this.subdirectoryCount = subdirectoryCount;
   }
   
}
