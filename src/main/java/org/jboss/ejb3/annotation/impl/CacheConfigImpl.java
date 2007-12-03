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
package org.jboss.ejb3.annotation.impl;

import java.lang.annotation.Annotation;

/**
 * Implementation of CacheConfig Annotation
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class CacheConfigImpl implements CacheConfig
{

   // Instance Members

   private String name;

   private int maxSize = CacheConfig.DEFAULT_NONCLUSTERED_MAX_SIZE;

   private long idleTimeoutSeconds = CacheConfig.DEFAULT_IDLE_TIMEOUT_SECONDS;

   private long removalTimeoutSeconds = CacheConfig.DEFAULT_REMOVAL_TIMEOUT_SECONDS;

   private boolean replicationIsPassivation = CacheConfig.DEFAULT_REPL_IS_PASV;

   // Constructor

   public CacheConfigImpl()
   {
   }

   // Accessors / Mutators

   public String name()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public int maxSize()
   {
      return maxSize;
   }

   public void setMaxSize(int maxSize)
   {
      this.maxSize = maxSize;
   }

   public long idleTimeoutSeconds()
   {
      return idleTimeoutSeconds;
   }

   public void setIdleTimeoutSeconds(long idleTimeoutSeconds)
   {
      this.idleTimeoutSeconds = idleTimeoutSeconds;
   }

   public long removalTimeoutSeconds()
   {
      return removalTimeoutSeconds;
   }

   public void setRemovalTimeoutSeconds(long removalTimeoutSeconds)
   {
      this.removalTimeoutSeconds = removalTimeoutSeconds;
   }

   public boolean replicationIsPassivation()
   {
      return replicationIsPassivation;
   }

   public void setReplicationIsPassivation(boolean replicationIsPassivation)
   {
      this.replicationIsPassivation = replicationIsPassivation;
   }

   public void merge(CacheConfig annotation)
   {
      if (maxSize == CacheConfig.DEFAULT_NONCLUSTERED_MAX_SIZE)
      {
         maxSize = annotation.maxSize();
      }

      if (idleTimeoutSeconds == CacheConfig.DEFAULT_IDLE_TIMEOUT_SECONDS)
      {
         idleTimeoutSeconds = annotation.idleTimeoutSeconds();
      }

      if (removalTimeoutSeconds == CacheConfig.DEFAULT_REMOVAL_TIMEOUT_SECONDS)
      {
         removalTimeoutSeconds = annotation.removalTimeoutSeconds();
      }

      if (replicationIsPassivation == CacheConfig.DEFAULT_REPL_IS_PASV)
      {
         replicationIsPassivation = annotation.replicationIsPassivation();
      }

   }

   public Class<? extends Annotation> annotationType()
   {
      return CacheConfig.class;
   }
}
