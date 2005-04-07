/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.blob.client;

import java.util.HashMap;
import javax.naming.InitialContext;
import org.jboss.tutorial.blob.bean.LobTester;
import org.jboss.tutorial.blob.bean.BlobEntity2;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      LobTester test = (LobTester) ctx.lookup(LobTester.class.getName());
      long blobId = test.create();
      HashMap map = test.findBlob(blobId);
      System.out.println("is hello in map: " + map.get("hello"));
      System.out.println(test.findClob(blobId));
      System.out.println("creating and getting a BlobEntity2 that uses byte[] and String instead of Clob/Blob");
      blobId = test.create2();
      BlobEntity2 entity = test.findBlob2(blobId);

   }
}
