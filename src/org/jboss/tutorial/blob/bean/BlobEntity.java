/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.blob.bean;

import javax.ejb.Entity;
import javax.ejb.FetchType;
import javax.ejb.GeneratorType;
import javax.ejb.Id;
import javax.ejb.Lob;
import javax.ejb.LobType;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Entity
public class BlobEntity implements Serializable
{
   private long id;
   private Blob blobby;
   private Clob clobby;

   @Id(generate = GeneratorType.IDENTITY)
   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   @Lob(fetch = FetchType.EAGER)
   public Blob getBlobby()
   {
      return blobby;
   }

   public void setBlobby(Blob blobby)
   {
      this.blobby = blobby;
   }

   @Lob(type = LobType.CLOB, fetch = FetchType.EAGER)
   public Clob getClobby()
   {
      return clobby;
   }

   public void setClobby(Clob clobby)
   {
      this.clobby = clobby;
   }


}
