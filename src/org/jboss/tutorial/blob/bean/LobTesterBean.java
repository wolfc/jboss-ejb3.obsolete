/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.blob.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import org.hibernate.Hibernate;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(LobTester.class)
public class LobTesterBean implements LobTester
{

   @Resource EntityManager manager;

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
      manager.persist(blob);
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

   public long create2()
   {
      BlobEntity2 blob = new BlobEntity2();

      HashMap map = new HashMap();
      map.put("hello", "world");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(map);
         blob.setBlobby(baos.toByteArray());
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
      blob.setClobby(clobby);
      manager.persist(blob);
      return blob.getId();
   }

   public BlobEntity2 findBlob2(long id) throws Exception
   {
      return manager.find(BlobEntity2.class, id);
   }

}
