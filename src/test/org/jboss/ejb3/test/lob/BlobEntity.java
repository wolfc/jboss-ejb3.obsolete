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
package org.jboss.ejb3.test.lob;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
@Entity
public class BlobEntity implements Serializable
{
   private long id;
   private Blob blobby;
   private Clob clobby;

   @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   @Lob
   public Blob getBlobby()
   {
      return blobby;
   }

   public void setBlobby(Blob blobby)
   {
      this.blobby = blobby;
   }

   @Lob
   public Clob getClobby()
   {
      return clobby;
   }

   public void setClobby(Clob clobby)
   {
      this.clobby = clobby;
   }


}
