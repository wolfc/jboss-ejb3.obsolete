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
package org.jboss.tutorial.blob.bean;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue; import javax.persistence.GenerationType;
import javax.persistence.Lob;
import javax.persistence.FetchType;
import javax.persistence.LobType;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Entity
public class BlobEntity2 implements Serializable
{
   private long id;
   private byte[] blobby;
   private String clobby;

   @Id @GeneratedValue(strategy=GenerationType.AUTO)
   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   @Lob(fetch = FetchType.EAGER)
   public byte[] getBlobby()
   {
      return blobby;
   }

   public void setBlobby(byte[] blobby)
   {
      this.blobby = blobby;
   }

   @Lob(type = LobType.CLOB, fetch = FetchType.EAGER)
   public String getClobby()
   {
      return clobby;
   }

   public void setClobby(String clobby)
   {
      this.clobby = clobby;
   }


}
