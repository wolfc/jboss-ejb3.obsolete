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
package org.jboss.ejb3.metadata.jpa.spec;

import java.net.URL;
import java.io.Serializable;

import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.jboss.metadata.javaee.support.MappableMetaData;

/**
 * A wrapper around Hibernate's PersistenceMetadata.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PersistenceUnitMetaData implements MappableMetaData, Serializable
{
   private static final long serialVersionUID = 1L;
   
   private PersistenceUnitsMetaData parent;

   private transient PersistenceMetadata delegate;

   private String name;

   public PersistenceUnitMetaData(PersistenceUnitsMetaData parent, PersistenceMetadata old)
   {
      assert parent != null : "parent is null";
      assert old != null : "old is null";
      
      this.parent = parent;
      this.delegate = old;
      this.name = old.getName();
   }

   public String getKey()
   {
      return name;
   }

   /**
    * Return the legacy metadata. Do not use, it will be removed soon.
    * 
    * @return
    */
   @Deprecated
   public PersistenceMetadata getLegacyMetadata()
   {
      if (delegate == null)
         throw new IllegalArgumentException("Null delegate, unit meta data deserialized with transient delegate.");
      return delegate;
   }
   
   public String getName()
   {
      return name;
   }
   
   /**
    * @return The URL for the jar file or directory that is the
    *         root of the persistence unit. (If the persistence unit is
    *         rooted in the WEB-INF/classes directory, this will be the
    *         URL of that directory.)
    * @see javax.persistence.PersistenceUnitInfo#getPersistenceUnitRootUrl
    */
   public URL getPersistenceUnitRootUrl()
   {
      return parent.getPersistenceUnitRootUrl();
   }
   
   public String toString()
   {
      return super.toString() + "{name=" + getName() + "}";
   }
}
