/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.blob.bean;

import org.hibernate.Hibernate;

import javax.ejb.EntityManager;
import javax.ejb.Inject;
import javax.ejb.Stateless;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
public class LobTesterBean implements org.jboss.ejb3.test.lob.LobTester
{

   @Inject EntityManager manager;

   public long create()
   {
      BlobEntity blob = new BlobEntity();

      HashMap map = new HashMap();
      map.put("hello", "world");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(map);
         blob.setBlobby(Hibernate.createBlob(baos.toByteArray()));
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }


      String clobby = "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
      "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work";
      blob.setClobby(Hibernate.createClob(clobby));
      manager.create(blob);
      return blob.getId();
   }

   public HashMap findBlob(long id) throws Exception
   {
      BlobEntity blob = manager.find(BlobEntity.class, id);
      ObjectInputStream ois = new ObjectInputStream(blob.getBlobby().getBinaryStream());
      return (HashMap) ois.readObject();
   }

   public String findClob(long id) throws Exception
   {
      BlobEntity blob = manager.find(BlobEntity.class, id);
      return blob.getClobby().getSubString(1, 31);
   }
}
