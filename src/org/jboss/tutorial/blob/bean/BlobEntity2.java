/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.blob.bean;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratorType;
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

   @Id(generate = GeneratorType.AUTO)
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
